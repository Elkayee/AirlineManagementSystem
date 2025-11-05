package airlinemanagementsystem.hhmm;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Value object used to filter flights in the timetable.
 */
public class FlightFilter {
    private final String flightDate; // YYYY-MM-DD
    private final String dep;
    private final String arr;
    private final String acType;
    private final String reg;
    private final String stdFrom;
    private final String stdTo;

    private FlightFilter(Builder builder) {
        this.flightDate = builder.flightDate;
        this.dep = builder.dep;
        this.arr = builder.arr;
        this.acType = builder.acType;
        this.reg = builder.reg;
        this.stdFrom = builder.stdFrom;
        this.stdTo = builder.stdTo;
    }

    public Optional<String> getFlightDate() {
        return Optional.ofNullable(flightDate);
    }

    public Optional<LocalDate> getFlightLocalDate() {
        return getFlightDate().map(TimeUtil::parseIsoDate);
    }

    public Optional<String> getDep() {
        return Optional.ofNullable(dep);
    }

    public Optional<String> getArr() {
        return Optional.ofNullable(arr);
    }

    public Optional<String> getAcType() {
        return Optional.ofNullable(acType);
    }

    public Optional<String> getReg() {
        return Optional.ofNullable(reg);
    }

    public Optional<String> getStdFrom() {
        return Optional.ofNullable(stdFrom);
    }

    public Optional<String> getStdTo() {
        return Optional.ofNullable(stdTo);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String flightDate;
        private String dep;
        private String arr;
        private String acType;
        private String reg;
        private String stdFrom;
        private String stdTo;

        public Builder withFlightDate(String flightDate) {
            this.flightDate = emptyToNull(flightDate);
            return this;
        }

        public Builder withDep(String dep) {
            this.dep = emptyToNull(dep);
            return this;
        }

        public Builder withArr(String arr) {
            this.arr = emptyToNull(arr);
            return this;
        }

        public Builder withAcType(String acType) {
            this.acType = emptyToNull(acType);
            return this;
        }

        public Builder withReg(String reg) {
            this.reg = emptyToNull(reg);
            return this;
        }

        public Builder withStdFrom(String stdFrom) {
            this.stdFrom = emptyToNull(stdFrom);
            return this;
        }

        public Builder withStdTo(String stdTo) {
            this.stdTo = emptyToNull(stdTo);
            return this;
        }

        public FlightFilter build() {
            return new FlightFilter(this);
        }

        private static String emptyToNull(String value) {
            if (value == null) {
                return null;
            }
            String trimmed = value.trim();
            return trimmed.isEmpty() ? null : trimmed;
        }
    }
}
