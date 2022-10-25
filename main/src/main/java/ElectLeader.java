import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ElectLeader implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String ELECTION_NAMESPACE = "/election";
    private ZooKeeper zooKeeper;
    private String currentZNodeName;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ElectLeader leaderElection = new ElectLeader();
        leaderElection.connectionZookeeper();
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
        leaderElection.run();
        leaderElection.close();
        System.out.println("Disconnected from Zookeeper, exiting application");
    }

    public void volunteerForLeadership() throws InterruptedException, KeeperException {
        String zNodePrefix = ELECTION_NAMESPACE + "/c_";
        // EPHEMERAL_SEQUENTIAL : 주키퍼와 연결이 끊어지면 해당 Z노드 삭제
        String zNodeFullPath = zooKeeper.create(zNodePrefix, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

        System.out.println("zNodeFullPath = " + zNodeFullPath);
        this.currentZNodeName = zNodeFullPath.replace(ELECTION_NAMESPACE + "/election/", "");
    }

    public void electLeader() throws InterruptedException, KeeperException {
        List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
        Collections.sort(children);
        String smallestChild = children.get(0);

        if (smallestChild.equals(currentZNodeName)) {
            System.out.println("I am the leader");
            return;
        }
        System.out.println("I am not the leader, " + smallestChild + " is  the leader.");
    }

    public void connectionZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

    // 즉시 종료되는 현상을 방지하기위해 메인스레드 대
    public void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    public void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        switch (watchedEvent.getType()) {
            case None:
                Watcher.Event.KeeperState state = watchedEvent.getState();
                System.out.println("state = " + state);
                if (state == Watcher.Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
        }
    }

}
