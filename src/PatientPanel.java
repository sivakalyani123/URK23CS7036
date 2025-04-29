import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.mysql.cj.xdevapi.Statement;

class PatientPanel extends JPanel {
    private JTextField nameField, ageField, contactField;
    private JComboBox<String> genderCombo;
    private JTable patientTable;
    private JScrollPane tableScrollPane;
    
    public PatientPanel() {
        setLayout(new BorderLayout());
        
        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Information"));
        
        formPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        
        formPanel.add(new JLabel("Age:"));
        ageField = new JTextField();
        formPanel.add(ageField);
        
        formPanel.add(new JLabel("Gender:"));
        genderCombo = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        formPanel.add(genderCombo);
        
        formPanel.add(new JLabel("Contact:"));
        contactField = new JTextField();
        formPanel.add(contactField);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton saveButton = new JButton("Save");
        JButton clearButton = new JButton("Clear");
        JButton backButton = new JButton("Back to Home");
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                savePatient();
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
        
        // Table to display patients
        createPatientTable();
        
        // Add panels to main panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
    }
    
    
    private void savePatient() {
        try {
            String name = nameField.getText().trim();
            String ageText = ageField.getText().trim();
            String gender = (String) genderCombo.getSelectedItem();
            String contact = contactField.getText().trim();
            
            if (name.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Contact cannot be empty",
                                            "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validate age
            int age;
            try {
                age = Integer.parseInt(ageText);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid age (numbers only)",
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
                
                // SQL query to insert patient
                String sql = "INSERT INTO patients (name, age, gender, contact) VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, gender);
                pstmt.setString(4, contact);
                
                int affected = pstmt.executeUpdate();
                
                if (affected > 0) {
                    // Still add to the in-memory list for table display
                    Main.Patient newPatient = new Main.Patient(name, age, gender, contact);
                    Main.addPatient(newPatient);
                    
                    JOptionPane.showMessageDialog(this, "Patient saved successfully", 
                                                 "Success", JOptionPane.INFORMATION_MESSAGE);
                    createPatientTable();
                    refreshPatientTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to save patient", 
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

    // Add this method to refresh the patient table
    private void refreshPatientTable() {
        // Remove and recreate table
        remove(tableScrollPane);
        createPatientTable();
        add(tableScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // Update this method to load patients from database
    private void createPatientTable() {
        DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Name", "Age", "Gender", "Contact"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        
        Connection conn = null;
        java.sql.Statement stmt = null;
        ResultSet rs = null;
        
        try {
            // Load JDBC driver and connect to database
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/hospital_db", "root", "123sivasekar@");
            
            stmt = conn.createStatement();
            rs = ((java.sql.Statement) stmt).executeQuery("SELECT id, name, age, gender, contact FROM patients");
            
            while(rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("age"),
                    rs.getString("gender"),
                    rs.getString("contact")
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
        
        patientTable = new JTable(model);
        tableScrollPane = new JScrollPane(patientTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Patient Records"));
    }
}
