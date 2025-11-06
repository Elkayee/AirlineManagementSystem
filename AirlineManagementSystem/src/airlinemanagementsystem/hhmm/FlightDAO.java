package airlinemanagementsystem.hhmm;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access object responsible for CRUD operations on the FlightsHHMM table.
 */
public class FlightDAO {
    private static final String DB_URL = "jdbc:sqlite:flights.db";

    public FlightDAO() {
        initialise();
    }

    private void initialise() {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS FlightsHHMM (" +
                        " Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " FlightDate TEXT NOT NULL," +
                        " FlightNo TEXT NOT NULL," +
                        " Type TEXT," +
                        " Reg TEXT," +
                        " ACType TEXT," +
                        " Dep TEXT NOT NULL," +
                        " Arr TEXT NOT NULL," +
                        " STD TEXT NOT NULL," +
                        " STA TEXT NOT NULL," +
                        " BLOCK TEXT," +
                        " FLThr TEXT," +
                        " Distance INTEGER," +
                        " AC_Config TEXT," +
                        " Seats INTEGER" +
                    ")");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_FlightsHHMM_date ON FlightsHHMM(FlightDate)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_FlightsHHMM_route ON FlightsHHMM(Dep, Arr)");
                statement.executeUpdate("CREATE INDEX IF NOT EXISTS idx_FlightsHHMM_reg ON FlightsHHMM(Reg)");
            }
            seedIfEmpty(connection);
        } catch (SQLException ex) {
            throw new IllegalStateException("Unable to initialise database", ex);
        }
    }

    private void seedIfEmpty(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM FlightsHHMM")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String sql = "INSERT INTO FlightsHHMM (FlightDate, FlightNo, Type, Reg, ACType, Dep, Arr, STD, STA, BLOCK, FLThr, Distance, AC_Config, Seats) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    for (Flight flight : FlightSeedData.flights()) {
                        bindFlight(ps, flight);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public Flight insert(Flight flight) throws SQLException {
        String sql = "INSERT INTO FlightsHHMM (FlightDate, FlightNo, Type, Reg, ACType, Dep, Arr, STD, STA, BLOCK, FLThr, Distance, AC_Config, Seats) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindFlight(ps, flight);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return flight.toBuilder().withId(keys.getInt(1)).build();
                }
            }
        }
        return flight;
    }

    public void update(Flight flight) throws SQLException {
        if (flight.getId() == null) {
            throw new IllegalArgumentException("Flight ID is required for update");
        }
        String sql = "UPDATE FlightsHHMM SET FlightDate=?, FlightNo=?, Type=?, Reg=?, ACType=?, Dep=?, Arr=?, STD=?, STA=?, BLOCK=?, FLThr=?, Distance=?, AC_Config=?, Seats=? WHERE Id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            bindFlight(ps, flight);
            ps.setInt(15, flight.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM FlightsHHMM WHERE Id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Optional<Flight> findById(int id) throws SQLException {
        String sql = "SELECT * FROM FlightsHHMM WHERE Id=?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Flight> findAll() throws SQLException {
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM FlightsHHMM ORDER BY FlightDate, STD")) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Flight> flights = new ArrayList<>();
                while (rs.next()) {
                    flights.add(mapRow(rs));
                }
                return flights;
            }
        }
    }

    public List<Flight> findByFilter(FlightFilter filter) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM FlightsHHMM WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        filter.getFlightDate().ifPresent(date -> {
            sql.append(" AND FlightDate = ?");
            parameters.add(date);
        });
        filter.getDep().ifPresent(dep -> {
            sql.append(" AND Dep = ?");
            parameters.add(dep);
        });
        filter.getArr().ifPresent(arr -> {
            sql.append(" AND Arr = ?");
            parameters.add(arr);
        });
        filter.getAcType().ifPresent(acType -> {
            sql.append(" AND ACType LIKE ?");
            parameters.add('%' + acType + '%');
        });
        filter.getReg().ifPresent(reg -> {
            sql.append(" AND Reg LIKE ?");
            parameters.add('%' + reg + '%');
        });
        filter.getStdFrom().ifPresent(stdFrom -> {
            sql.append(" AND STD >= ?");
            parameters.add(stdFrom);
        });
        filter.getStdTo().ifPresent(stdTo -> {
            sql.append(" AND STD <= ?");
            parameters.add(stdTo);
        });
        sql.append(" ORDER BY FlightDate, STD");

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Flight> flights = new ArrayList<>();
                while (rs.next()) {
                    flights.add(mapRow(rs));
                }
                return flights;
            }
        }
    }

    public List<Flight> findFlightsForRegBetween(String reg, LocalDate fromInclusive, LocalDate toInclusive) throws SQLException {
        String sql = "SELECT * FROM FlightsHHMM WHERE Reg = ? AND FlightDate BETWEEN ? AND ? ORDER BY FlightDate, STD";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, reg);
            ps.setString(2, fromInclusive.toString());
            ps.setString(3, toInclusive.toString());
            try (ResultSet rs = ps.executeQuery()) {
                List<Flight> flights = new ArrayList<>();
                while (rs.next()) {
                    flights.add(mapRow(rs));
                }
                return flights;
            }
        }
    }

    private void bindFlight(PreparedStatement ps, Flight flight) throws SQLException {
        ps.setString(1, flight.getFlightDate());
        ps.setString(2, flight.getFlightNo());
        ps.setString(3, flight.getType());
        ps.setString(4, flight.getReg());
        ps.setString(5, flight.getAcType());
        ps.setString(6, flight.getDep());
        ps.setString(7, flight.getArr());
        ps.setString(8, flight.getStd());
        ps.setString(9, flight.getSta());
        ps.setString(10, flight.getBlock());
        ps.setString(11, flight.getFlThr());
        if (flight.getDistance() == null) {
            ps.setNull(12, java.sql.Types.INTEGER);
        } else {
            ps.setInt(12, flight.getDistance());
        }
        ps.setString(13, flight.getAcConfig());
        if (flight.getSeats() == null) {
            ps.setNull(14, java.sql.Types.INTEGER);
        } else {
            ps.setInt(14, flight.getSeats());
        }
    }

    private Flight mapRow(ResultSet rs) throws SQLException {
        return Flight.builder()
            .withId(rs.getInt("Id"))
            .withFlightDate(rs.getString("FlightDate"))
            .withFlightNo(rs.getString("FlightNo"))
            .withType(rs.getString("Type"))
            .withReg(rs.getString("Reg"))
            .withAcType(rs.getString("ACType"))
            .withDep(rs.getString("Dep"))
            .withArr(rs.getString("Arr"))
            .withStd(rs.getString("STD"))
            .withSta(rs.getString("STA"))
            .withBlock(rs.getString("BLOCK"))
            .withFlThr(rs.getString("FLThr"))
            .withDistance(getNullableInt(rs, "Distance"))
            .withAcConfig(rs.getString("AC_Config"))
            .withSeats(getNullableInt(rs, "Seats"))
            .build();
    }

    private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }
}
