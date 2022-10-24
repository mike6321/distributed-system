package cluster.management;

import org.apache.zookeeper.KeeperException;

public interface OnElectionCallback {

    void onElectedToBeLeader() throws InterruptedException, KeeperException;
    void onWorker();

}
