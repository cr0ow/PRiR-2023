import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws RemoteException {
        LocateRegistry.createRegistry(1099);
        var rmiHistogram = new RMIHistogram();
        rmiHistogram.bind("RemoteHistogram");

        int numberOfClients = 14;
        int numberOfHistograms = 5;
        int numberOfBeans = 5;
        List<Thread> threads = new ArrayList<>();
        List<Client> clients = new ArrayList<>();

        for(int i = 0; i < numberOfHistograms; i++) {
            rmiHistogram.createHistogram(numberOfBeans);
        }

        for(int i = 0; i < numberOfClients; i++) {
            var client = new Client(i % numberOfHistograms, numberOfBeans);
            clients.add(client);
            threads.add(new Thread(client));
        }

        threads.forEach(Thread::start);
        threads.forEach(client -> {
            try {
                client.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        for(int i = 0; i < numberOfHistograms; i++) {
            var result = rmiHistogram.getHistogram(i);
            var sum = 0;
            System.out.println("\n-----------------\nid = " + i);
            for (int j : result) {
                sum += j;
                System.out.print(j + " ");
            }
            var finalI = i;
            var num = clients.stream().filter(client -> client.getHistogramId() == finalI).count();
            System.out.println("\nsuma       = " + sum);
            System.out.println("oczekiwana = " + num * Client.REPEATS);
            System.out.println("-----------------");
        }

        exit(0);
    }

}
