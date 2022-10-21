import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class Application {

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        if (args.length != 2) {
            System.out.println("Expecting parameters <number of workers> <path to worker jar file>");
            System.exit(1);
        }

        int numberOfWorkers = Integer.parseInt(args[0]);
        System.out.println("numberOfWorkers = " + numberOfWorkers);
        String pathToWorkerProgram = args[1];
        System.out.println("pathToWorkerProgram = " + pathToWorkerProgram);
        Autohealer autohealer = new Autohealer(numberOfWorkers, pathToWorkerProgram);
        autohealer.connectToZookeeper();
        autohealer.startWatchingWorkers();
        autohealer.run();
        autohealer.close();
    }

}
