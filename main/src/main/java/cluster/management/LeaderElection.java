package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {

    private static final String ELECTION_NAMESPACE = "/election";
    private final ZooKeeper zooKeeper;
    private String currentZNodeName;
    private final OnElectionCallback onElectionCallback;

    public LeaderElection(ZooKeeper zooKeeper, OnElectionCallback onElectionCallback) {
        this.zooKeeper = zooKeeper;
        this.onElectionCallback = onElectionCallback;
    }

    public void volunteerForLeadership() throws InterruptedException, KeeperException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        String zNodeFullPath = zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("zNodeFullPath = " + zNodeFullPath);
        this.currentZNodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
    }

    public void reelectLeader() throws InterruptedException, KeeperException {
        String predecessorZNodeName = "";
        Stat predecessorStat = null;
        while (predecessorStat == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);

            if (smallestChild.equals(currentZNodeName)) {
                System.out.println("I am the leader");
                onElectionCallback.onElectedToBeLeader();
                return;
            } else {
                System.out.println("I am not the leader");
                int predecessorIndex = Collections.binarySearch(children, currentZNodeName) - 1;
                System.out.println(predecessorIndex);
                predecessorZNodeName = children.get(predecessorIndex);
                predecessorStat = zooKeeper.exists(ELECTION_NAMESPACE + "/" + predecessorZNodeName, this);
            }
        }

        onElectionCallback.onWorker();

        System.out.println("Watching ZNode " + predecessorZNodeName);
        System.out.println();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case NodeDeleted:
                try {
                    reelectLeader();
                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
        }
    }

}
