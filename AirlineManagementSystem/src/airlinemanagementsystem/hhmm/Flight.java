package airlinemanagementsystem.hhmm;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Immutable representation of a scheduled flight stored in the HH:MM timetable.
 */
public class Flight {
    private final Integer id;
    private final String flightDate; // YYYY-MM-DD
    private final String flightNo;
    private final String type;
    private final String reg;
    private final String acType;
    private final String dep;
    private final String arr;
    private final String std; // HH:MM
    private final String sta; // HH:MM
    private final String block; // HH:MM optional
    private final String flThr; // HH:MM optional
    private final Integer distance;
    private final String acConfig;
    private final Integer seats;

    private Flight(Builder builder) {
        this.id = builder.id;
        this.flightDate = builder.flightDate;
        this.flightNo = builder.flightNo;
        this.type = builder.type;
        this.reg = builder.reg;
        this.acType = builder.acType;
        this.dep = builder.dep;
        this.arr = builder.arr;
        this.std = builder.std;
        this.sta = builder.sta;
        this.block = builder.block;
        this.flThr = builder.flThr;
        this.distance = builder.distance;
        this.acConfig = builder.acConfig;
        this.seats = builder.seats;
    }

    public Integer getId() {
        return id;
    }

    public String getFlightDate() {
        return flightDate;
    }

    public LocalDate getFlightLocalDate() {
        return TimeUtil.parseIsoDate(flightDate);
    }

    public String getFlightNo() {
        return flightNo;
    }

    public String getType() {
        return type;
    }

    public String getReg() {
        return reg;
    }

    public String getAcType() {
        return acType;
    }

    public String getDep() {
        return dep;
    }

    public String getArr() {
        return arr;
    }

    public String getStd() {
        return std;
    }

    public String getSta() {
        return sta;
    }

    public String getBlock() {
        return block;
    }

    public String getFlThr() {
        return flThr;
    }

    public Integer getDistance() {
        return distance;
    }

    public String getAcConfig() {
        return acConfig;
    }

    public Integer getSeats() {
        return seats;
    }

    public Builder toBuilder() {
        return new Builder()
            .withId(id)
            .withFlightDate(flightDate)
            .withFlightNo(flightNo)
            .withType(type)
            .withReg(reg)
            .withAcType(acType)
            .withDep(dep)
            .withArr(arr)
            .withStd(std)
            .withSta(sta)
            .withBlock(block)
            .withFlThr(flThr)
            .withDistance(distance)
            .withAcConfig(acConfig)
            .withSeats(seats);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Flight)) return false;
        Flight flight = (Flight) o;
        return Objects.equals(id, flight.id) &&
            Objects.equals(flightDate, flight.flightDate) &&
            Objects.equals(flightNo, flight.flightNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, flightDate, flightNo);
    }

    @Override
    public String toString() {
        return "Flight{" +
            "id=" + id +
            ", flightDate='" + flightDate + '\'' +
            ", flightNo='" + flightNo + '\'' +
            ", dep='" + dep + '\'' +
            ", arr='" + arr + '\'' +
            ", std='" + std + '\'' +
            ", sta='" + sta + '\'' +
            '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private String flightDate;
        private String flightNo;
        private String type;
        private String reg;
        private String acType;
        private String dep;
        private String arr;
        private String std;
        private String sta;
        private String block;
        private String flThr;
        private Integer distance;
        private String acConfig;
        private Integer seats;

        public Builder withId(Integer id) {
            this.id = id;
            return this;
        }

        public Builder withFlightDate(String flightDate) {
            this.flightDate = flightDate;
            return this;
        }

        public Builder withFlightNo(String flightNo) {
            this.flightNo = flightNo;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withReg(String reg) {
            this.reg = reg;
            return this;
        }

        public Builder withAcType(String acType) {
            this.acType = acType;
            return this;
        }

        public Builder withDep(String dep) {
            this.dep = dep;
            return this;
        }

        public Builder withArr(String arr) {
            this.arr = arr;
            return this;
        }

        public Builder withStd(String std) {
            this.std = std;
            return this;
        }

        public Builder withSta(String sta) {
            this.sta = sta;
            return this;
        }

        public Builder withBlock(String block) {
            this.block = block;
            return this;
        }

        public Builder withFlThr(String flThr) {
            this.flThr = flThr;
            return this;
        }

        public Builder withDistance(Integer distance) {
            this.distance = distance;
            return this;
        }

        public Builder withAcConfig(String acConfig) {
            this.acConfig = acConfig;
            return this;
        }

        public Builder withSeats(Integer seats) {
            this.seats = seats;
            return this;
        }

        public Flight build() {
            return new Flight(this);
        }
    }
}
