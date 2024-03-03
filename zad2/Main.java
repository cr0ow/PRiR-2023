import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        var orderInterface = new TestOrderInterface();
        var employer = new ParallelEmployer();
        employer.setOrderInterface(orderInterface);
        var startLocation = new Location(5, 2);
        var allowedDirections = List.of(Direction.EAST, Direction.NORTH);

        var start = LocalDateTime.now();
        var result = employer.findExit(startLocation, allowedDirections);
        var end = LocalDateTime.now();
        var time = end.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC);

        System.out.println("\n-----------------");
        System.out.println("Poprawne wyjście  : " + new Location(5 ,8));
        System.out.println("Znalezione wyjście: " + result);
        System.out.println("Czas pracy        : " + time + " sekund");
        System.out.println("-----------------");
    }

}
