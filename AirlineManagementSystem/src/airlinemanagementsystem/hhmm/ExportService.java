package airlinemanagementsystem.hhmm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Writes the currently displayed flights to a CSV file.
 */
public class ExportService {
    public void exportToCsv(List<Flight> flights, Path targetFile) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(targetFile)) {
            writer.write("FlightDate,FlightNo,Type,Reg,ACType,Dep,Arr,STD,STA,BLOCK,FLThr,Distance,AC_Config,Seats");
            writer.newLine();
            for (Flight flight : flights) {
                writer.write(String.join(",",
                    escape(flight.getFlightDate()),
                    escape(flight.getFlightNo()),
                    escape(flight.getType()),
                    escape(flight.getReg()),
                    escape(flight.getAcType()),
                    escape(flight.getDep()),
                    escape(flight.getArr()),
                    escape(flight.getStd()),
                    escape(flight.getSta()),
                    escape(flight.getBlock()),
                    escape(flight.getFlThr()),
                    escape(valueOrEmpty(flight.getDistance())),
                    escape(flight.getAcConfig()),
                    escape(valueOrEmpty(flight.getSeats()))));
                writer.newLine();
            }
        }
    }

    private String valueOrEmpty(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
