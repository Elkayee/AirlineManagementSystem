package airlinemanagementsystem.hhmm;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Calculates on-the-fly KPI metrics required by the dashboard.
 */
public class KpiService {

    public KpiResult calculate(List<Flight> flights) {
        int totalFlights = flights.size();
        int totalSeats = flights.stream()
            .map(Flight::getSeats)
            .filter(v -> v != null)
            .mapToInt(Integer::intValue)
            .sum();

        Optional<String> avgBlock = averageTime(flights.stream()
            .map(Flight::getBlock)
            .collect(Collectors.toList()));
        Optional<String> avgFlThr = averageTime(flights.stream()
            .map(Flight::getFlThr)
            .collect(Collectors.toList()));

        Map<String, Long> routeCounts = flights.stream()
            .collect(Collectors.groupingBy(
                flight -> flight.getDep() + "-" + flight.getArr(),
                Collectors.counting()));

        List<String> topRoutes = routeCounts.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                .thenComparing(Map.Entry.comparingByKey()))
            .limit(5)
            .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
            .collect(Collectors.toList());

        return new KpiResult(totalFlights, totalSeats, avgBlock.orElse("--"), avgFlThr.orElse("--"), topRoutes);
    }

    private Optional<String> averageTime(List<String> times) {
        List<Integer> minutes = new ArrayList<>();
        for (String time : times) {
            TimeUtil.tryToMinutes(time).ifPresent(minutes::add);
        }
        if (minutes.isEmpty()) {
            return Optional.empty();
        }
        int avg = (int) Math.round(minutes.stream().mapToInt(Integer::intValue).average().orElse(0));
        return Optional.of(TimeUtil.formatMinutes(avg));
    }

    public static class KpiResult {
        private final int totalFlights;
        private final int totalSeats;
        private final String averageBlock;
        private final String averageFlThr;
        private final List<String> topRoutes;

        public KpiResult(int totalFlights, int totalSeats, String averageBlock, String averageFlThr, List<String> topRoutes) {
            this.totalFlights = totalFlights;
            this.totalSeats = totalSeats;
            this.averageBlock = averageBlock;
            this.averageFlThr = averageFlThr;
            this.topRoutes = new ArrayList<>(topRoutes);
        }

        public int getTotalFlights() {
            return totalFlights;
        }

        public int getTotalSeats() {
            return totalSeats;
        }

        public String getAverageBlock() {
            return averageBlock;
        }

        public String getAverageFlThr() {
            return averageFlThr;
        }

        public List<String> getTopRoutes() {
            return new ArrayList<>(topRoutes);
        }

        public String formatTopRoutes() {
            if (topRoutes.isEmpty()) {
                return "--";
            }
            return String.join(", ", topRoutes);
        }
    }
}
