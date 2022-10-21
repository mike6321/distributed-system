import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class Application {
    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        Worker worker = new Worker();
        worker.connectToZookeeper();
        worker.work();
    }
}
