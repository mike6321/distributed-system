package cluster;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public class WatchersDemo implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    private static final String TARGET_Z_NODE = "/target_znode";

    public static void main(String[] args) throws IOException, InterruptedException {
        WatchersDemo watchersDemo = new WatchersDemo();
        watchersDemo.connectionZooKeeper();
        watchersDemo.run();
        watchersDemo.close();
    }

    public void watchTargetZNode() throws InterruptedException, KeeperException {
        Stat stat = zooKeeper.exists(TARGET_Z_NODE, this);
        if (stat == null) {
            return;
        }
        byte[] data = zooKeeper.getData(TARGET_Z_NODE, this, stat);
        List<String> children = zooKeeper.getChildren(TARGET_Z_NODE, this);
        System.out.println("Data : " + new String(data) + " children : " + children);
    }

    public void connectionZooKeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
    }

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
                Event.KeeperState state = watchedEvent.getState();
                System.out.println("state = " + state);
                if (state == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeDeleted:
                System.out.println(TARGET_Z_NODE + " was deleted");
                break;
            case NodeCreated:
                System.out.println(TARGET_Z_NODE + " was created");
                break;
            case NodeDataChanged:
                System.out.println(TARGET_Z_NODE + " data changed ");
                break;
            case NodeChildrenChanged:
                System.out.println(TARGET_Z_NODE + " children changed ");
                break;
        }
        try {
            watchTargetZNode();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
