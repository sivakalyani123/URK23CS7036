import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;

class AppointmentPanel extends JPanel {
    private JComboBox<String> patientCombo, doctorCombo;
    private JTextField dateField, timeField, reasonField;
    private JTable appointmentTable;
    private JScrollPane tableScrollPane;
    private Connection conn;
    private Statement stmt;
    
    public AppointmentPanel() {
        setLayout(new BorderLayout());
        
        // Initialize database connection
        initializeDB();
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Appointment Information"));
        
        formPanel.add(new JLabel("Patient:"));
        patientCombo = new JComboBox<>();
        updatePatientCombo();
        formPanel.add(patientCombo);
        
        formPanel.add(new JLabel("Doctor:"));
        doctorCombo = new JComboBox<>();
        updateDoctorCombo();
        formPanel.add(doctorCombo);
        
        formPanel.add(new JLabel("Date (MM/DD/YYYY):"));
        dateField = new JTextField();
        formPanel.add(dateField);
        
        formPanel.add(new JLabel("Time:"));
        timeField = new JTextField();
        formPanel.add(timeField);
        
        formPanel.add(new JLabel("Reason:"));
        reasonField = new JTextField();
        formPanel.add(reasonField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Home");
        JButton deleteButton = new JButton("Delete");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAppointment();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Main.switchToHome();
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedAppointment();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        
        // Table to display appointments
        createAppointmentTable();
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        
        // Add listener for table selection
        appointmentTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentTable.getSelectedRow() != -1) {
                int row = appointmentTable.getSelectedRow();
                populateFieldsFromSelection(row);
            }
        });
    }
    
    private void initializeDB() {
        try {
            // Load the JDBC driver - modify this for your specific database
            // For MySQL: "com.mysql.jdbc.Driver"
            // For PostgreSQL: "org.postgresql.Driver"
            // For SQLite: "org.sqlite.JDBC"
            Class.forName("com.mysql.jdbc.Driver");
            
            // Establish connection - modify URL, username, and password
            // For MySQL: "jdbc:mysql://localhost:3306/hospital_db"
            // For PostgreSQL: "jdbc:postgresql://localhost:5432/hospital_db"
            // For SQLite: "jdbc:sqlite:hospital.db"
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/hospital_db", "root", "123sivasekar@");
            
            stmt = conn.createStatement();
            
            // Check if tables exist, if not create them
            createTablesIfNotExist();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Database connection error: " + e.getMessage(), 
                "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void createTablesIfNotExist() {
        try {
            // Check if patients table exists
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet patientTable = meta.getTables(null, null, "patients", null);
            if (!patientTable.next()) {
                String createPatientTable = "CREATE TABLE patients (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "age INT," +
                    "gender VARCHAR(10)," +
                    "contact VARCHAR(50)" +
                    ")";
                stmt.executeUpdate(createPatientTable);
            }
            
            // Check if doctors table exists
            ResultSet doctorTable = meta.getTables(null, null, "doctors", null);
            if (!doctorTable.next()) {
                String createDoctorTable = "CREATE TABLE doctors (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "name VARCHAR(100) NOT NULL," +
                    "specialization VARCHAR(100)," +
                    "experience VARCHAR(50)," +
                    "contact VARCHAR(50)," +
                    "available_hours VARCHAR(100)" +
                    ")";
                stmt.executeUpdate(createDoctorTable);
            }
            
            // Check if appointments table exists
            ResultSet appointmentTable = meta.getTables(null, null, "appointments", null);
            if (!appointmentTable.next()) {
                String createAppointmentTable = "CREATE TABLE appointments (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY," +
                    "patient_name VARCHAR(100) NOT NULL," +
                    "doctor_name VARCHAR(100) NOT NULL," +
                    "appointment_date VARCHAR(20)," +
                    "appointment_time VARCHAR(20)," +
                    "reason TEXT" +
                    ")";
                stmt.executeUpdate(createAppointmentTable);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updatePatientCombo() {
        patientCombo.removeAllItems();
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT name FROM patients");
            while(rs.next()) {
                patientCombo.addItem(rs.getString("name"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Add option for new patient
        patientCombo.addItem("-- Add New Patient --");
    }
    
    private void updateDoctorCombo() {
        doctorCombo.removeAllItems();
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT name FROM doctors");
            while(rs.next()) {
                doctorCombo.addItem(rs.getString("name"));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Add option for new doctor
        doctorCombo.addItem("-- Add New Doctor --");
    }
    
    private void createAppointmentTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Patient", "Doctor", "Date", "Time", "Reason"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT id, patient_name, doctor_name, appointment_date, appointment_time, reason FROM appointments");
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("reason")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        appointmentTable = new JTable(model);
        appointmentTable.getColumnModel().getColumn(0).setPreferredWidth(30); // ID column width
        tableScrollPane = new JScrollPane(appointmentTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Appointment Records"));
    }

    private void saveAppointment() {
        try {
            String patientName = (String) patientCombo.getSelectedItem();
            String doctorName = (String) doctorCombo.getSelectedItem();
            String date = dateField.getText().trim();
            String time = timeField.getText().trim();
            String reason = reasonField.getText().trim();
            
            if (patientName == null || doctorName == null || patientName.equals("-- Add New Patient --") || 
                doctorName.equals("-- Add New Doctor --")) {
                JOptionPane.showMessageDialog(this, "Please select valid patient and doctor",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (date.isEmpty() || time.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Date and Time cannot be empty",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Check if we're updating an existing appointment or creating a new one
            int selectedRow = appointmentTable.getSelectedRow();
            
            if (selectedRow != -1) {
                // Update existing appointment
                int id = (int) appointmentTable.getValueAt(selectedRow, 0);
                String updateQuery = "UPDATE appointments SET patient_name = ?, doctor_name = ?, " +
                                    "appointment_date = ?, appointment_time = ?, reason = ? WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, patientName);
                    pstmt.setString(2, doctorName);
                    pstmt.setString(3, date);
                    pstmt.setString(4, time);
                    pstmt.setString(5, reason);
                    pstmt.setInt(6, id);
                    
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment updated successfully", 
                                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            } else {
                // Insert new appointment
                String insertQuery = "INSERT INTO appointments (patient_name, doctor_name, appointment_date, " +
                                    "appointment_time, reason) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, patientName);
                    pstmt.setString(2, doctorName);
                    pstmt.setString(3, date);
                    pstmt.setString(4, time);
                    pstmt.setString(5, reason);
                    
                    int affected = pstmt.executeUpdate();
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment saved successfully", 
                                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
            
            clearFields();
            refreshTable();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void deleteSelectedAppointment() {
        int row = appointmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to delete",
                                         "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (int) appointmentTable.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
                        "Are you sure you want to delete this appointment?", 
                        "Confirm Delete", JOptionPane.YES_NO_OPTION);
                        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String deleteQuery = "DELETE FROM appointments WHERE id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {
                    pstmt.setInt(1, id);
                    int affected = pstmt.executeUpdate();
                    
                    if (affected > 0) {
                        JOptionPane.showMessageDialog(this, "Appointment deleted successfully",
                                                     "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearFields();
                        refreshTable();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                                             "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
    
    private void populateFieldsFromSelection(int row) {
        String patient = appointmentTable.getValueAt(row, 1).toString();
        String doctor = appointmentTable.getValueAt(row, 2).toString();
        String date = appointmentTable.getValueAt(row, 3).toString();
        String time = appointmentTable.getValueAt(row, 4).toString();
        String reason = appointmentTable.getValueAt(row, 5).toString();
        
        patientCombo.setSelectedItem(patient);
        doctorCombo.setSelectedItem(doctor);
        dateField.setText(date);
        timeField.setText(time);
        reasonField.setText(reason);
    }
    
    private void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) appointmentTable.getModel();
        model.setRowCount(0);
        
        try {
            ResultSet rs = stmt.executeQuery("SELECT id, patient_name, doctor_name, appointment_date, appointment_time, reason FROM appointments");
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("patient_name"),
                    rs.getString("doctor_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("reason")
                });
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void clearFields() {
        appointmentTable.clearSelection();
        
        if (patientCombo.getItemCount() > 0) {
            patientCombo.setSelectedIndex(0);
        }
        if (doctorCombo.getItemCount() > 0) {
            doctorCombo.setSelectedIndex(0);
        }
        dateField.setText("");
        timeField.setText("");
        reasonField.setText("");
    }
    
    // Method to close database connection when panel is disposed
    public void closeConnection() {
        try {
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}