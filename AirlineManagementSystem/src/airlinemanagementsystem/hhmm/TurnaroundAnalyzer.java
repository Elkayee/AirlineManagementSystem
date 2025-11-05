package airlinemanagementsystem.hhmm;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Detects tight turnaround situations for the same aircraft registration.
 */
public class TurnaroundAnalyzer {
    private final FlightDAO flightDAO;

    public TurnaroundAnalyzer(FlightDAO flightDAO) {
        this.flightDAO = flightDAO;
    }

    public List<String> detectConflicts(String reg, LocalDate focusDate, long minimumMinutes) {
        if (reg == null || reg.isBlank()) {
            return List.of();
        }
        try {
            LocalDate from = focusDate.minusDays(1);
            LocalDate to = focusDate.plusDays(1);
            List<Flight> flights = flightDAO.findFlightsForRegBetween(reg, from, to);
            return detectConflictsInMemory(flights, minimumMinutes);
        } catch (Exception ex) {
            return List.of("Unable to analyse turnaround: " + ex.getMessage());
        }
    }

    public List<String> detectConflictsInMemory(List<Flight> flights, long minimumMinutes) {
        Map<String, List<Flight>> byReg = flights.stream()
            .filter(flight -> flight.getReg() != null && !flight.getReg().isBlank())
            .collect(Collectors.groupingBy(Flight::getReg));

        List<String> warnings = new ArrayList<>();
        for (Map.Entry<String, List<Flight>> entry : byReg.entrySet()) {
            List<Flight> sorted = entry.getValue().stream()
                .sorted(Comparator.comparing(TimeUtil::departureDateTime))
                .collect(Collectors.toList());
            for (int i = 0; i < sorted.size() - 1; i++) {
                Flight current = sorted.get(i);
                Flight next = sorted.get(i + 1);
                LocalDateTime arrival = TimeUtil.arrivalDateTime(current);
                LocalDateTime nextDeparture = TimeUtil.departureDateTime(next);
                long minutes = TimeUtil.minutesBetween(arrival, nextDeparture);
                if (minutes < minimumMinutes) {
                    warnings.add(String.format(
                        "%s conflict: %s %s-%s STA %s -> %s %s-%s STD %s (%d min)",
                        entry.getKey(),
                        current.getFlightNo(), current.getDep(), current.getArr(), current.getSta(),
                        next.getFlightNo(), next.getDep(), next.getArr(), next.getStd(),
                        minutes));
                }
            }
        }
        return warnings;
    }
}
