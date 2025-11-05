package airlinemanagementsystem.hhmm;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility helpers dedicated to HH:MM based time calculations.
 */
public final class TimeUtil {
    private static final Pattern HH_MM_PATTERN = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final DateTimeFormatter HH_MM_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private TimeUtil() {
    }

    public static boolean isValidIsoDate(String value) {
        if (value == null) {
            return false;
        }
        try {
            LocalDate.parse(value, ISO_DATE);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    public static LocalDate parseIsoDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value, ISO_DATE);
    }

    public static boolean isValidTime(String value) {
        return value != null && HH_MM_PATTERN.matcher(value.trim()).matches();
    }

    public static LocalTime parseTime(String value) {
        if (!isValidTime(value)) {
            throw new DateTimeException("Invalid HH:MM value: " + value);
        }
        return LocalTime.parse(value, HH_MM_FORMATTER);
    }

    public static Optional<LocalTime> tryParseTime(String value) {
        if (!isValidTime(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalTime.parse(value, HH_MM_FORMATTER));
        } catch (DateTimeParseException ex) {
            return Optional.empty();
        }
    }

    public static int toMinutes(String value) {
        LocalTime time = parseTime(value);
        return time.getHour() * 60 + time.getMinute();
    }

    public static String formatMinutes(int minutes) {
        int normalized = Math.abs(minutes);
        int hours = normalized / 60;
        int mins = normalized % 60;
        String formatted = String.format("%02d:%02d", hours, mins);
        return minutes < 0 ? "-" + formatted : formatted;
    }

    public static int durationMinutes(String start, String end) {
        Objects.requireNonNull(start, "start time");
        Objects.requireNonNull(end, "end time");
        int startMinutes = toMinutes(start);
        int endMinutes = toMinutes(end);
        int duration = endMinutes - startMinutes;
        if (duration < 0) {
            duration += 24 * 60;
        }
        return duration;
    }

    public static LocalDateTime combine(LocalDate date, String time) {
        if (date == null || !isValidTime(time)) {
            throw new DateTimeException("Unable to combine invalid values");
        }
        return LocalDateTime.of(date, parseTime(time));
    }

    public static LocalDateTime arrivalDateTime(Flight flight) {
        LocalDate flightDate = flight.getFlightLocalDate();
        LocalDateTime std = combine(flightDate, flight.getStd());
        LocalDateTime sta = combine(flightDate, flight.getSta());
        if (toMinutes(flight.getSta()) < toMinutes(flight.getStd())) {
            sta = sta.plusDays(1);
        }
        return sta;
    }

    public static LocalDateTime departureDateTime(Flight flight) {
        return combine(flight.getFlightLocalDate(), flight.getStd());
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return Duration.between(start, end).toMinutes();
    }

    public static Optional<Integer> tryToMinutes(String value) {
        if (!isValidTime(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(toMinutes(value));
        } catch (DateTimeException ex) {
            return Optional.empty();
        }
    }
}
