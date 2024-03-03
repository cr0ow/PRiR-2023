import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.util.Objects.isNull;

class ParallelEmployer implements Employer {

    private OrderInterface orderInterface;
    private final ResultListener resultListener = new DefaultResultListener(this);
    private final Map<Integer, Location> orders = new ConcurrentHashMap<>();
    private final Set<Location> visited = new ConcurrentSkipListSet<>(Comparator.comparing(Location::toString));
    private final Queue<Location> toExplore = new ConcurrentLinkedQueue<>();
    private Location destination;

    @Override
    public void setOrderInterface(OrderInterface order) {
        this.orderInterface = order;
    }

    @Override
    public Location findExit(Location startLocation, List<Direction> allowedDirections) {
        orderInterface.setResultListener(resultListener);
        toExplore.add(startLocation);
        visited.add(startLocation);
        queueUpNewLocations(startLocation, allowedDirections);

        while (isNull(destination)) {
            while(!toExplore.isEmpty()) {
                var location = getNextLocation();

                orders.put(orderInterface.order(location), location);
            }
            try {
                synchronized (this) {
                    this.wait();
                }
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        }

        return destination;
    }

    private void newResult(Result result) {
        var location = orders.remove(result.orderID());
        if (result.type().equals(LocationType.EXIT)) {
            destination = location;
            toExplore.clear();
            synchronized (this) {
                this.notify();
            }
            return;
        }

        queueUpNewLocations(location, result.allowedDirections());

        synchronized (this) {
            this.notify();
        }
    }

    private void queueUpNewLocations(Location location, List<Direction> directions) {
        directions.forEach(direction -> {
            var newLocation = direction.step(location);
            synchronized (visited) {
                if (!visited.contains(newLocation)) {
                    toExplore.add(newLocation);
                    visited.add(newLocation);
                }
            }
        });
    }

    private Location getNextLocation() {
        var location = toExplore.remove();
        visited.add(location);
        return location;
    }

    private record DefaultResultListener(ParallelEmployer parallelEmployer) implements ResultListener {

        @Override
        public void result(Result result) {
            parallelEmployer.newResult(result);
        }

    }

}
