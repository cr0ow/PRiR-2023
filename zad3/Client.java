import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Random;


public class Client implements Runnable {

    public static final int REPEATS = 10;
    private static int NEXT_ID = 0;
    private final int id;
    private final int histogramId;
    private final int bins;

    public Client(int histogramId, int bins) {
        this.id = NEXT_ID++;
        this.histogramId = histogramId;
        this.bins = bins;
    }

    public int getHistogramId() {
        return histogramId;
    }

    @Override
    public void run() {
        var random = new Random();
        RemoteHistogram server;
        try {
            var registry = LocateRegistry.getRegistry();
            server = (RemoteHistogram) registry.lookup("RemoteHistogram");
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }

        try {
            for(int i = 0; i < REPEATS; i++) {
                var value = random.nextInt(bins);
                server.addToHistogram(histogramId, value);
                System.out.println("wątek: " + id + "\thistogram: " + histogramId + "\twartość: " + value);
            }
        } catch (RemoteException exception) {
            throw new RuntimeException(exception);
        }
    }

}
