package airlinemanagementsystem.hhmm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Service responsible for loading HH:MM timetable flights from CSV files.
 */
public class ImportService {
    private final FlightDAO flightDAO;
    private final FlightValidator validator;

    public ImportService(FlightDAO flightDAO, FlightValidator validator) {
        this.flightDAO = flightDAO;
        this.validator = validator;
    }

    public ImportResult importCsv(Path csvFile) throws IOException {
        List<String> errors = new ArrayList<>();
        List<Flight> imported = new ArrayList<>();

        try (Stream<String> lines = Files.lines(csvFile)) {
            int[] lineNumber = {0};
            lines.forEach(line -> {
                lineNumber[0]++;
                if (lineNumber[0] == 1 && looksLikeHeader(line)) {
                    return; // skip header
                }
                if (line.trim().isEmpty()) {
                    return;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 14) {
                    errors.add("Line " + lineNumber[0] + ": expected 14 columns but found " + columns.length);
                    return;
                }
                try {
                    Flight flight = mapColumns(columns);
                    List<String> validationErrors = validator.validate(flight);
                    if (!validationErrors.isEmpty()) {
                        errors.add("Line " + lineNumber[0] + ": " + String.join("; ", validationErrors));
                        return;
                    }
                    try {
                        Flight persisted = flightDAO.insert(flight);
                        imported.add(persisted);
                    } catch (SQLException ex) {
                        errors.add("Line " + lineNumber[0] + ": " + ex.getMessage());
                    }
                } catch (IllegalArgumentException ex) {
                    errors.add("Line " + lineNumber[0] + ": " + ex.getMessage());
                }
            });
        }
        return new ImportResult(imported, errors);
    }

    private boolean looksLikeHeader(String line) {
        String normalized = line.toLowerCase(Locale.ROOT);
        return normalized.contains("flightdate") && normalized.contains("flightno");
    }

    private Flight mapColumns(String[] c) {
        return Flight.builder()
            .withFlightDate(trim(c[0]))
            .withFlightNo(trim(c[1]))
            .withType(trim(c[2]))
            .withReg(trim(c[3]))
            .withAcType(trim(c[4]))
            .withDep(trim(c[5]))
            .withArr(trim(c[6]))
            .withStd(trim(c[7]))
            .withSta(trim(c[8]))
            .withBlock(trim(c[9]))
            .withFlThr(trim(c[10]))
            .withDistance(parseInteger(trim(c[11]), "Distance"))
            .withAcConfig(trim(c[12]))
            .withSeats(parseInteger(trim(c[13]), "Seats"))
            .build();
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private Integer parseInteger(String value, String label) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be numeric");
        }
    }

    public static class ImportResult {
        private final List<Flight> imported;
        private final List<String> errors;

        public ImportResult(List<Flight> imported, List<String> errors) {
            this.imported = imported;
            this.errors = errors;
        }

        public List<Flight> getImported() {
            return imported;
        }

        public List<String> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}
