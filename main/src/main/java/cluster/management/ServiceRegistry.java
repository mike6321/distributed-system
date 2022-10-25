package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServiceRegistry implements Watcher {

    private static final String REGISTRY_Z_NODE = "/service_registry";
    private final ZooKeeper zooKeeper;
    private String currentZNode = null;
    private List<String> allServiceAddress = null; // for cache (To avoid calling the getChildren method)

    public ServiceRegistry(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        createServiceRegistryZNode();
    }

    /**
     * node 생성
     * */
    private void createServiceRegistryZNode() {
        try {
            if (zooKeeper.exists(REGISTRY_Z_NODE, false) == null) {
                // 생성된 zNode 영구보관을 위해 PERSIST 설정
                System.out.println("create registry zNode : " + REGISTRY_Z_NODE);
                zooKeeper.create(REGISTRY_Z_NODE, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) { // 동시에 두번 요청 시
            e.printStackTrace();
        }
    }

    /**
     * cluster 생성
     * */
    public void registerToCluster(String metaData) throws InterruptedException, KeeperException {
        this.currentZNode = zooKeeper.create(
                REGISTRY_Z_NODE + "/n_",
                metaData.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("Registered to service registry : " + this.currentZNode);
    }

    public void registerForUpdates() {
        try {
            updateAddress();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    /**
     * cluster 삭제
     * */
    public void unregisterFromCluster() throws InterruptedException, KeeperException {
        if (currentZNode != null && zooKeeper.exists(currentZNode, false) != null) {
            zooKeeper.delete(currentZNode, -1);
        }
    }

    public synchronized List<String> getAllServiceAddress() throws InterruptedException, KeeperException {
        if (this.allServiceAddress == null) {
            updateAddress();
        }
        return this.allServiceAddress;
    }

    private synchronized void updateAddress() throws InterruptedException, KeeperException {
        List<String> workerZNodes = zooKeeper.getChildren(REGISTRY_Z_NODE, this);
        List<String> addresses = new ArrayList<>(workerZNodes.size());
        for (String workerZNode : workerZNodes) {
            String workerZNodeFullPath = REGISTRY_Z_NODE + "/" + workerZNode;
            Stat stat = zooKeeper.exists(workerZNodeFullPath, false);

            // race condition : getChildren, exists 사이에 노드가 없어짐을 대비하여 null check
            if (stat == null) {
                continue;
            }

            byte[] addressBytes = zooKeeper.getData(workerZNodeFullPath, false, stat);
            String address = new String(addressBytes);
            addresses.add(address);
        }
        this.allServiceAddress = Collections.unmodifiableList(addresses);
        System.out.println("The Cluster addresses are : " + this.allServiceAddress);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try {
            updateAddress();
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

}
