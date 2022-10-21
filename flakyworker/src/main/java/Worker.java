
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class Worker {
    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;

    // Parent Znode where each worker stores an ephemeral child to indicate it is alive
    private static final String AUTOHEALER_ZNODES_PATH = "/workers";

    private static final float CHANCE_TO_FAIL = 0.1F;

    private final Random random = new Random();
    private ZooKeeper zooKeeper;

    public void connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, event -> {
        });
    }

    public void work() throws KeeperException, InterruptedException {
        addChildZnode();

        while (true) {
            System.out.println("Working...");
            LockSupport.parkNanos(1000);
            if (random.nextFloat() < CHANCE_TO_FAIL) {
                System.out.println("Critical error happened");
                throw new RuntimeException("Oops");
            }
        }
    }

    private void addChildZnode() throws KeeperException, InterruptedException {
        zooKeeper.create(AUTOHEALER_ZNODES_PATH + "/worker_",
                new byte[]{},
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }

}
