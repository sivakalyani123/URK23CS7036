import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;

class DoctorPanel extends JPanel {
    private JTextField nameField, specializationField, experienceField, contactField, hoursField;
    private JTable doctorTable;
    private JScrollPane tableScrollPane;
    
    public DoctorPanel() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Information"));
        
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Specialization:"));
        specializationField = new JTextField();
        formPanel.add(specializationField);
        
        formPanel.add(new JLabel("Experience (years):"));
        experienceField = new JTextField();
        formPanel.add(experienceField);
        
        formPanel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        formPanel.add(contactField);
        
        formPanel.add(new JLabel("Available Hours:"));
        hoursField = new JTextField();
        formPanel.add(hoursField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Home");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveDoctor();
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
        
        buttonPanel.add(saveButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(backButton);
        
        // Table to display doctors
        createDoctorTable();
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }
    
    private void createDoctorTable() {
        String[] columnNames = {"Name", "Specialization", "Experience", "Contact", "Available Hours"};
        Object[][] data = new Object[Main.getDoctorList().size()][5];
        
        for(int i = 0; i < Main.getDoctorList().size(); i++) {
            Main.Doctor doctor = Main.getDoctorList().get(i);
            data[i][0] = doctor.getName();
            data[i][1] = doctor.getSpecialization();
            data[i][2] = doctor.getExperience();
            data[i][3] = doctor.getContact();
            data[i][4] = doctor.getAvailableHours();
        }
        
        doctorTable = new JTable(data, columnNames);
        tableScrollPane = new JScrollPane(doctorTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Doctor Records"));
    }

    private void saveDoctor() {
        try {
            String name = nameField.getText().trim();
            String specialization = specializationField.getText().trim();
            String experience = experienceField.getText().trim();
            String contact = contactField.getText().trim();
            String hours = hoursField.getText().trim();
            
            if (name.isEmpty() || specialization.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name, Specialization and Contact cannot be empty",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Database connection
            Connection conn = null;
            PreparedStatement pstmt = null;
            
            try {
                // Load JDBC driver and connect to database
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/hospital_db", "root", "123sivasekar@");
                
                // SQL query to insert doctor
                String sql = "INSERT INTO doctors (name, specialization, experience, contact, available_hours) VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setString(2, specialization);
                pstmt.setString(3, experience);
                pstmt.setString(4, contact);
                pstmt.setString(5, hours);
                
                int affected = pstmt.executeUpdate();
                
                if (affected > 0) {
                    // Still add to the in-memory list for table display
                    Main.Doctor newDoctor = new Main.Doctor(name, specialization, experience, contact, hours);
                    Main.addDoctor(newDoctor);
                    
                    JOptionPane.showMessageDialog(this, "Doctor saved successfully", 
                                                 "Success", JOptionPane.INFORMATION_MESSAGE);
                    clearFields();
                    refreshDoctorTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save doctor", 
                                                 "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                                             "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                JOptionPane.showMessageDialog(this, "JDBC Driver not found: " + e.getMessage(),
                                             "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {
                // Close database resources
                try {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(),
                                         "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add this method to refresh the doctor table
    private void refreshDoctorTable() {
        // Remove and recreate table
        remove(tableScrollPane);
        createDoctorTable();
        add(tableScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Update this method to load doctors from database
    private void createDoctorTable1() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Specialization", "Experience", "Contact", "Available Hours"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // Load JDBC driver and connect to database
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/hospital_db", "root", "123sivasekar@");
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT id, name, specialization, experience, contact, available_hours FROM doctors");
            
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("specialization"),
                    rs.getString("experience"),
                    rs.getString("contact"),
                    rs.getString("available_hours")
                });
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(),
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "JDBC Driver not found: " + e.getMessage(),
                                         "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } finally {
            // Close database resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        doctorTable = new JTable(model);
        tableScrollPane = new JScrollPane(doctorTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Doctor Records"));
    }
    
    private void clearFields() {
        nameField.setText("");
        specializationField.setText("");
        experienceField.setText("");
        contactField.setText("");
        hoursField.setText("");
        nameField.requestFocus();
    }
}