import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RMIHistogram implements RemoteHistogram, Binder {

    private static final AtomicInteger NEXT_ID = new AtomicInteger();
    private final Map<Integer, Integer[]> histograms = new ConcurrentHashMap<>();

    @Override
    public void bind(String serviceName) {
        try {
            var stub = (RemoteHistogram) UnicastRemoteObject.exportObject(this, 0);
            var registry = LocateRegistry.getRegistry();
            registry.rebind(serviceName, stub);
        } catch (RemoteException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public int createHistogram(int bins) throws RemoteException {
        var values = new Integer[bins];
        Arrays.fill(values, 0);
        int id = NEXT_ID.getAndIncrement();
        histograms.put(id, values);
        return id;
    }

    @Override
    public synchronized void addToHistogram(int histogramID, int value) throws RemoteException {
        var values = histograms.get(histogramID);
        values[value]++;
        histograms.replace(histogramID, values);
    }

    @Override
    public int[] getHistogram(int histogramID) throws RemoteException {
        var values = histograms.get(histogramID);
        var result = new int[values.length];
        for(int i = 0; i < values.length; i++) {
            result[i] = values[i];
        }
        return result;
    }

}
