
import org.apache.zookeeper.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Autohealer implements Watcher {

    private static final String ZOOKEEPER_ADDRESS = "127.0.0.1:2181";
    private static final int SESSION_TIMEOUT = 3000;

    // Parent Znode where each worker stores an ephemeral child to indicate it is alive
    private static final String AUTO_HEALER_Z_NODES_PATH = "/workers";

    // Path to the worker jar
    private final String pathToProgram;

    // The number of worker instances we need to maintain at all times
    private final int numberOfWorkers;
    private ZooKeeper zooKeeper;

    public Autohealer(int numberOfWorkers, String pathToProgram) {
        this.numberOfWorkers = numberOfWorkers;
        this.pathToProgram = pathToProgram;
    }

    public void startWatchingWorkers() throws KeeperException, InterruptedException {
        if (zooKeeper.exists(AUTO_HEALER_Z_NODES_PATH, false) == null) {
            zooKeeper.create(AUTO_HEALER_Z_NODES_PATH, new byte[]{}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
        launchWorkersIfNecessary();
    }

    public void connectToZookeeper() throws IOException {
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
    public void process(WatchedEvent event) {
        switch (event.getType()) {
            case None:
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    System.out.println("Successfully connected to Zookeeper");
                } else {
                    synchronized (zooKeeper) {
                        System.out.println("Disconnected from Zookeeper event");
                        zooKeeper.notifyAll();
                    }
                }
                break;
            case NodeChildrenChanged:
                launchWorkersIfNecessary();
        }
    }

    private void launchWorkersIfNecessary() {
        try {
            List<String> children = zooKeeper.getChildren(AUTO_HEALER_Z_NODES_PATH, this);
            System.out.println(String.format("Currently there are %d workers", children.size()));

            if (children.size() < numberOfWorkers) {
                startNewWorker();
            }
        } catch (InterruptedException | KeeperException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void startNewWorker() throws IOException {
        File file = new File(pathToProgram);
        String canonicalPath = file.getCanonicalPath();
        System.out.println("canonicalPath = " + canonicalPath);
        String command = "java -jar " + canonicalPath;
        System.out.println(String.format("Launching worker instance : %s ", command));
        Runtime.getRuntime().exec(command, null, file.getParentFile());
    }

}
