package airlinemanagementsystem.hhmm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * Swing based manager window for the HH:MM timetable flights.
 */
public class FlightManagerFrame extends JFrame {
    private static final int MIN_TURNAROUND_MINUTES = 45;
    private final FlightDAO flightDAO;
    private final FlightValidator validator;
    private final ImportService importService;
    private final ExportService exportService;
    private final KpiService kpiService;
    private final TurnaroundAnalyzer turnaroundAnalyzer;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final List<Flight> currentFlights = new ArrayList<>();

    private final JTextField dateField = new JTextField(10);
    private final JTextField flightNoField = new JTextField(10);
    private final JTextField typeField = new JTextField(10);
    private final JTextField regField = new JTextField(10);
    private final JTextField acTypeField = new JTextField(10);
    private final JTextField depField = new JTextField(5);
    private final JTextField arrField = new JTextField(5);
    private final JTextField stdField = new JTextField(5);
    private final JTextField staField = new JTextField(5);
    private final JTextField blockField = new JTextField(5);
    private final JTextField flThrField = new JTextField(5);
    private final JTextField distanceField = new JTextField(5);
    private final JTextField acConfigField = new JTextField(10);
    private final JTextField seatsField = new JTextField(5);

    private final JTextField filterDateField = new JTextField(8);
    private final JTextField filterDepField = new JTextField(4);
    private final JTextField filterArrField = new JTextField(4);
    private final JTextField filterAcTypeField = new JTextField(6);
    private final JTextField filterRegField = new JTextField(6);
    private final JTextField filterStdFromField = new JTextField(5);
    private final JTextField filterStdToField = new JTextField(5);

    private final JLabel totalFlightsLabel = new JLabel("0");
    private final JLabel totalSeatsLabel = new JLabel("0");
    private final JLabel avgBlockLabel = new JLabel("--");
    private final JLabel avgFlThrLabel = new JLabel("--");
    private final JTextArea topRoutesArea = new JTextArea(3, 20);
    private final JTextArea turnaroundArea = new JTextArea(4, 20);

    private Integer selectedFlightId;

    public FlightManagerFrame() {
        super("Flight Manager HH:MM");
        this.flightDAO = new FlightDAO();
        this.validator = new FlightValidator();
        this.importService = new ImportService(flightDAO, validator);
        this.exportService = new ExportService();
        this.kpiService = new KpiService();
        this.turnaroundAnalyzer = new TurnaroundAnalyzer(flightDAO);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        this.tableModel = new DefaultTableModel(new Object[]{
            "Id", "Date", "Flight", "Type", "Reg", "AC Type", "Dep", "Arr", "STD", "STA", "BLOCK", "FLThr", "Distance", "Config", "Seats"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        this.table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                populateFormFromSelection();
            }
        });

        add(createFormPanel(), BorderLayout.WEST);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createKpiPanel(), BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1300, 700));
        pack();
        setLocationRelativeTo(null);

        refreshTable();
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Flight Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        row = addField(panel, gbc, row, "Date (YYYY-MM-DD)", dateField);
        row = addField(panel, gbc, row, "Flight No", flightNoField);
        row = addField(panel, gbc, row, "Type", typeField);
        row = addField(panel, gbc, row, "Reg", regField);
        row = addField(panel, gbc, row, "AC Type", acTypeField);
        row = addField(panel, gbc, row, "Dep", depField);
        row = addField(panel, gbc, row, "Arr", arrField);
        row = addField(panel, gbc, row, "STD (HH:MM)", stdField);
        row = addField(panel, gbc, row, "STA (HH:MM)", staField);
        row = addField(panel, gbc, row, "BLOCK", blockField);
        row = addField(panel, gbc, row, "FLThr", flThrField);
        row = addField(panel, gbc, row, "Distance", distanceField);
        row = addField(panel, gbc, row, "AC Config", acConfigField);
        row = addField(panel, gbc, row, "Seats", seatsField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add");
        addButton.addActionListener(this::onAdd);
        JButton updateButton = new JButton("Update");
        updateButton.addActionListener(this::onUpdate);
        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this::onDelete);
        JButton clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        panel.add(buttonPanel, gbc);
        return panel;
    }

    private int addField(JPanel panel, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
        return row + 1;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(createFilterPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(createImportExportPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder("Filters"));
        panel.add(new JLabel("Date"));
        panel.add(filterDateField);
        panel.add(new JLabel("Dep"));
        panel.add(filterDepField);
        panel.add(new JLabel("Arr"));
        panel.add(filterArrField);
        panel.add(new JLabel("AC Type"));
        panel.add(filterAcTypeField);
        panel.add(new JLabel("Reg"));
        panel.add(filterRegField);
        panel.add(new JLabel("STD From"));
        panel.add(filterStdFromField);
        panel.add(new JLabel("To"));
        panel.add(filterStdToField);
        JButton applyButton = new JButton("Apply");
        applyButton.addActionListener(e -> applyFilters());
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> clearFilters());
        panel.add(applyButton);
        panel.add(resetButton);
        return panel;
    }

    private JPanel createImportExportPanel() {
        JPanel panel = new JPanel();
        JButton importButton = new JButton("Import CSV");
        importButton.addActionListener(e -> importCsv());
        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> exportCsv());
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshTable());
        panel.add(importButton);
        panel.add(exportButton);
        panel.add(refreshButton);
        return panel;
    }

    private JPanel createKpiPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("KPI & Turnaround"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 8, 2, 8);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Total Flights:"), gbc);
        gbc.gridx = 1;
        panel.add(totalFlightsLabel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Total Seats:"), gbc);
        gbc.gridx = 1;
        panel.add(totalSeatsLabel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Average BLOCK:"), gbc);
        gbc.gridx = 1;
        panel.add(avgBlockLabel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Average FLThr:"), gbc);
        gbc.gridx = 1;
        panel.add(avgFlThrLabel, gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Top Routes:"), gbc);
        gbc.gridx = 1;
        topRoutesArea.setLineWrap(true);
        topRoutesArea.setWrapStyleWord(true);
        topRoutesArea.setEditable(false);
        panel.add(new JScrollPane(topRoutesArea), gbc);

        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Turnaround Alerts:"), gbc);
        gbc.gridx = 1;
        turnaroundArea.setLineWrap(true);
        turnaroundArea.setWrapStyleWord(true);
        turnaroundArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(turnaroundArea);
        scrollPane.setPreferredSize(new Dimension(400, 80));
        panel.add(scrollPane, gbc);
        return panel;
    }

    private void onAdd(ActionEvent event) {
        Flight flight = readFlightFromForm(null);
        if (flight == null) {
            return;
        }
        try {
            Flight persisted = flightDAO.insert(flight);
            selectedFlightId = persisted.getId();
            refreshTable();
            JOptionPane.showMessageDialog(this, "Flight added successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Unable to add flight", ex);
        }
    }

    private void onUpdate(ActionEvent event) {
        if (selectedFlightId == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight to update", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Flight flight = readFlightFromForm(selectedFlightId);
        if (flight == null) {
            return;
        }
        try {
            flightDAO.update(flight);
            refreshTable();
            JOptionPane.showMessageDialog(this, "Flight updated", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            showError("Unable to update flight", ex);
        }
    }

    private void onDelete(ActionEvent event) {
        if (selectedFlightId == null) {
            JOptionPane.showMessageDialog(this, "Please select a flight to delete", "No selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected flight?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            flightDAO.delete(selectedFlightId);
            selectedFlightId = null;
            refreshTable();
        } catch (SQLException ex) {
            showError("Unable to delete flight", ex);
        }
    }

    private Flight readFlightFromForm(Integer id) {
        Integer distance;
        Integer seats;
        try {
            distance = parseInteger(distanceField.getText().trim(), "Distance");
            seats = parseInteger(seatsField.getText().trim(), "Seats");
        } catch (NumberFormatException ex) {
            return null;
        }

        Flight flight = Flight.builder()
            .withId(id)
            .withFlightDate(dateField.getText().trim())
            .withFlightNo(flightNoField.getText().trim())
            .withType(typeField.getText().trim())
            .withReg(regField.getText().trim())
            .withAcType(acTypeField.getText().trim())
            .withDep(depField.getText().trim())
            .withArr(arrField.getText().trim())
            .withStd(stdField.getText().trim())
            .withSta(staField.getText().trim())
            .withBlock(blockField.getText().trim())
            .withFlThr(flThrField.getText().trim())
            .withDistance(distance)
            .withAcConfig(acConfigField.getText().trim())
            .withSeats(seats)
            .build();

        List<String> errors = validator.validate(flight);
        if (!errors.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errors), "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (!analyseTurnaroundRisk(flight)) {
            return null;
        }
        return flight;
    }

    private boolean analyseTurnaroundRisk(Flight candidate) {
        if (candidate.getReg() == null || candidate.getReg().isBlank()) {
            return true;
        }
        if (!TimeUtil.isValidIsoDate(candidate.getFlightDate())) {
            return true;
        }
        try {
            LocalDate focusDate = TimeUtil.parseIsoDate(candidate.getFlightDate());
            if (focusDate == null) {
                return true;
            }
            List<Flight> related = flightDAO.findFlightsForRegBetween(
                candidate.getReg(), focusDate.minusDays(1), focusDate.plusDays(1));
            if (candidate.getId() != null) {
                related.removeIf(f -> candidate.getId().equals(f.getId()));
            }
            related.add(candidate);
            List<String> warnings = turnaroundAnalyzer.detectConflictsInMemory(related, MIN_TURNAROUND_MINUTES);
            if (!warnings.isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Potential turnaround issue:\n" + String.join("\n", warnings) + "\nContinue?",
                    "Turnaround warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                return choice == JOptionPane.YES_OPTION;
            }
        } catch (SQLException ex) {
            showError("Unable to validate turnaround", ex);
        }
        return true;
    }

    private void populateFormFromSelection() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentFlights.size()) {
            return;
        }
        Flight flight = currentFlights.get(selectedRow);
        selectedFlightId = flight.getId();
        dateField.setText(flight.getFlightDate());
        flightNoField.setText(flight.getFlightNo());
        typeField.setText(flight.getType());
        regField.setText(flight.getReg());
        acTypeField.setText(flight.getAcType());
        depField.setText(flight.getDep());
        arrField.setText(flight.getArr());
        stdField.setText(flight.getStd());
        staField.setText(flight.getSta());
        blockField.setText(flight.getBlock());
        flThrField.setText(flight.getFlThr());
        distanceField.setText(flight.getDistance() == null ? "" : flight.getDistance().toString());
        acConfigField.setText(flight.getAcConfig());
        seatsField.setText(flight.getSeats() == null ? "" : flight.getSeats().toString());
    }

    private void clearForm() {
        selectedFlightId = null;
        dateField.setText("");
        flightNoField.setText("");
        typeField.setText("");
        regField.setText("");
        acTypeField.setText("");
        depField.setText("");
        arrField.setText("");
        stdField.setText("");
        staField.setText("");
        blockField.setText("");
        flThrField.setText("");
        distanceField.setText("");
        acConfigField.setText("");
        seatsField.setText("");
        table.clearSelection();
    }

    private void clearFilters() {
        filterDateField.setText("");
        filterDepField.setText("");
        filterArrField.setText("");
        filterAcTypeField.setText("");
        filterRegField.setText("");
        filterStdFromField.setText("");
        filterStdToField.setText("");
        refreshTable();
    }

    private void applyFilters() {
        String dateFilter = filterDateField.getText();
        if (!dateFilter.isBlank() && !TimeUtil.isValidIsoDate(dateFilter.trim())) {
            JOptionPane.showMessageDialog(this, "Date filter must be YYYY-MM-DD", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!filterStdFromField.getText().isBlank() && !TimeUtil.isValidTime(filterStdFromField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "STD From must be HH:MM", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!filterStdToField.getText().isBlank() && !TimeUtil.isValidTime(filterStdToField.getText().trim())) {
            JOptionPane.showMessageDialog(this, "STD To must be HH:MM", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FlightFilter filter = FlightFilter.builder()
            .withFlightDate(filterDateField.getText())
            .withDep(filterDepField.getText())
            .withArr(filterArrField.getText())
            .withAcType(filterAcTypeField.getText())
            .withReg(filterRegField.getText())
            .withStdFrom(filterStdFromField.getText())
            .withStdTo(filterStdToField.getText())
            .build();
        try {
            List<Flight> flights = flightDAO.findByFilter(filter);
            updateTable(flights);
        } catch (SQLException ex) {
            showError("Unable to load flights", ex);
        }
    }

    private void refreshTable() {
        try {
            List<Flight> flights = flightDAO.findAll();
            updateTable(flights);
        } catch (SQLException ex) {
            showError("Unable to load flights", ex);
        }
    }

    private void updateTable(List<Flight> flights) {
        currentFlights.clear();
        currentFlights.addAll(flights);
        tableModel.setRowCount(0);
        for (Flight flight : flights) {
            tableModel.addRow(new Object[]{
                flight.getId(),
                flight.getFlightDate(),
                flight.getFlightNo(),
                flight.getType(),
                flight.getReg(),
                flight.getAcType(),
                flight.getDep(),
                flight.getArr(),
                flight.getStd(),
                flight.getSta(),
                flight.getBlock(),
                flight.getFlThr(),
                flight.getDistance(),
                flight.getAcConfig(),
                flight.getSeats()
            });
        }
        refreshKpis();
        updateTurnaroundWarnings();
        if (selectedFlightId != null) {
            for (int i = 0; i < currentFlights.size(); i++) {
                if (selectedFlightId.equals(currentFlights.get(i).getId())) {
                    table.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void refreshKpis() {
        KpiService.KpiResult result = kpiService.calculate(currentFlights);
        totalFlightsLabel.setText(String.valueOf(result.getTotalFlights()));
        totalSeatsLabel.setText(String.valueOf(result.getTotalSeats()));
        avgBlockLabel.setText(result.getAverageBlock());
        avgFlThrLabel.setText(result.getAverageFlThr());
        topRoutesArea.setText(result.formatTopRoutes());
    }

    private void updateTurnaroundWarnings() {
        List<String> warnings = turnaroundAnalyzer.detectConflictsInMemory(currentFlights, MIN_TURNAROUND_MINUTES);
        if (warnings.isEmpty()) {
            turnaroundArea.setText("No turnaround conflicts detected");
        } else {
            turnaroundArea.setText(String.join("\n", warnings));
        }
    }

    private void importCsv() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            ImportService.ImportResult importResult = importService.importCsv(file.toPath());
            refreshTable();
            if (importResult.hasErrors()) {
                JOptionPane.showMessageDialog(this,
                    "Imported " + importResult.getImported().size() + " flights with warnings:\n" + String.join("\n", importResult.getErrors()),
                    "Import completed", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Imported " + importResult.getImported().size() + " flights",
                    "Import completed", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException ex) {
            showError("Unable to read file", ex);
        }
    }

    private void exportCsv() {
        if (currentFlights.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to export", "Export", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("flights_export.csv"));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            exportService.exportToCsv(currentFlights, file.toPath());
            JOptionPane.showMessageDialog(this, "Exported to " + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            showError("Unable to export", ex);
        }
    }

    private Integer parseInteger(String text, String label) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(text);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, label + " must be a number", "Validation", JOptionPane.WARNING_MESSAGE);
            throw ex;
        }
    }

    private void showError(String title, Exception ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightManagerFrame().setVisible(true));
    }
}
