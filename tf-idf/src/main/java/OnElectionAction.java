import cluster.management.OnElectionCallback;
import cluster.management.ServiceRegistry;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {

    private final ServiceRegistry serviceRegistry;
    private final int port;
    private WebServer webServer;

    public  OnElectionAction(ServiceRegistry serviceRegistry, int port) {
        this.serviceRegistry = serviceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() throws InterruptedException, KeeperException {
        serviceRegistry.unregisterFromCluster();
        serviceRegistry.registerForUpdates();
    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        webServer = new WebServer(port, searchWorker);
        webServer.startServer();
        try {
            String currentServiceAddress =
                    String.format("http://%s:%d", InetAddress.getLocalHost().getCanonicalHostName(), port, searchWorker.getEndPoint());
            serviceRegistry.registerToCluster(currentServiceAddress);
        } catch (UnknownHostException | InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

}
