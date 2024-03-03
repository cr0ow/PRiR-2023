import java.util.List;

public class TestResult implements Result {

    private final int orderId;
    private final LocationType locationType;
    private final List<Direction> allowedDirections;

    public TestResult(int orderId, LocationType locationType, List<Direction> allowedDirections) {
        this.orderId = orderId;
        this.locationType = locationType;
        this.allowedDirections = allowedDirections;
    }

    @Override
    public int orderID() {
        return orderId;
    }

    @Override
    public LocationType type() {
        return locationType;
    }

    @Override
    public List<Direction> allowedDirections() {
        return allowedDirections;
    }

}
