package cluster.management;

import org.apache.zookeeper.KeeperException;

/**
 * 리더 선출을 위한 콜백
 * */
public interface OnElectionCallback {

    void onElectedToBeLeader() throws InterruptedException, KeeperException;
    void onWorker();

}
