import java.util.ArrayList;
import java.util.Random;

public class TestOrderInterface implements OrderInterface {

    private ResultListener resultListener;
    private static int NEXT_ID = 0;
    private final LocationType[][] map;

    public TestOrderInterface() {
        map = new LocationType[][] {
                {LocationType.WALL, LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.WALL,    LocationType.PASSAGE, LocationType.PASSAGE, LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.WALL},
                {LocationType.WALL, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.PASSAGE, LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.PASSAGE, LocationType.WALL,    LocationType.WALL},
                {LocationType.WALL, LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.EXIT,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL,    LocationType.WALL}
        };
    }

    @Override
    public void setResultListener(ResultListener listener) {
        this.resultListener = listener;
    }

    @Override
    public int order(Location location) {
        var id = ++NEXT_ID;
        new Thread(new OrderProcessor(resultListener, map, location, id)).start();
        return id;
    }

    private record OrderProcessor(ResultListener resultListener, LocationType[][] map, Location location, int id) implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(new Random().nextInt(5000));
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            }

            var resultType = map[location.row()][location.col()];
            var allowedDirections = new ArrayList<Direction>();

            if(resultType == LocationType.EXIT) {
                resultListener.result(new TestResult(id, resultType, allowedDirections));
                return;
            }

            if(map[Direction.SOUTH.step(location).col()][Direction.SOUTH.step(location).row()] != LocationType.WALL) {
                allowedDirections.add(Direction.SOUTH);
            }
            if(map[Direction.NORTH.step(location).col()][Direction.NORTH.step(location).row()] != LocationType.WALL) {
                allowedDirections.add(Direction.NORTH);
            }
            if(map[Direction.EAST.step(location).col()][Direction.EAST.step(location).row()] != LocationType.WALL) {
                allowedDirections.add(Direction.EAST);
            }
            if(map[Direction.WEST.step(location).col()][Direction.WEST.step(location).row()] != LocationType.WALL) {
                allowedDirections.add(Direction.WEST);
            }

            resultListener.result(new TestResult(id, resultType, allowedDirections));
        }

    }
}
