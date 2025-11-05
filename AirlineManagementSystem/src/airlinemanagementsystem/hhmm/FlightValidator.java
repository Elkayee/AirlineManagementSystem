package airlinemanagementsystem.hhmm;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs validation rules required by the specification before persisting flights.
 */
public class FlightValidator {

    public List<String> validate(Flight flight) {
        List<String> errors = new ArrayList<>();
        if (flight.getFlightDate() == null || !TimeUtil.isValidIsoDate(flight.getFlightDate())) {
            errors.add("Flight date must be in format YYYY-MM-DD");
        }
        if (isBlank(flight.getFlightNo())) {
            errors.add("Flight number is required");
        }
        if (isBlank(flight.getDep())) {
            errors.add("Departure airport is required");
        }
        if (isBlank(flight.getArr())) {
            errors.add("Arrival airport is required");
        }
        if (!isBlank(flight.getDep()) && flight.getDep().equalsIgnoreCase(flight.getArr())) {
            errors.add("Departure and arrival airports must be different");
        }
        validateTime("STD", flight.getStd(), errors);
        validateTime("STA", flight.getSta(), errors);
        if (flight.getBlock() != null && !flight.getBlock().isBlank()) {
            validateTime("BLOCK", flight.getBlock(), errors);
        }
        if (flight.getFlThr() != null && !flight.getFlThr().isBlank()) {
            validateTime("FLThr", flight.getFlThr(), errors);
        }
        if (flight.getSeats() != null && flight.getSeats() < 0) {
            errors.add("Seats must be greater than or equal to 0");
        }
        return errors;
    }

    private void validateTime(String label, String value, List<String> errors) {
        if (!TimeUtil.isValidTime(value)) {
            errors.add(label + " must follow HH:MM");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
