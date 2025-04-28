import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class Main {
    private static JFrame frame;
    private static JPanel homePanel;
    private static JTabbedPane tabs;
    private static ArrayList<Patient> patientList = new ArrayList<>();
    private static ArrayList<Doctor> doctorList = new ArrayList<>();
    private static ArrayList<Appointment> appointmentList = new ArrayList<>();
    
    public static void main(String[] args) {
        // Load existing data
        loadData();
        
        // Create the main frame
        frame = new JFrame("🏥 Hospital Management System");
        frame.setSize(900, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        // Create home panel and tabs
        createHomePanel();
        createTabs();
        
        // Initially show home panel
        frame.add(homePanel, BorderLayout.CENTER);
        
        // Add window closing event to save data
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveData();
            }
        });
        
        // Show the frame
        frame.setVisible(true);
    }
    
    private static void createHomePanel() {
        homePanel = new JPanel();
        homePanel.setLayout(new BorderLayout());
        
        // Top panel with large hospital image
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.setBackground(Color.WHITE);
        
        ImageIcon hospitalIcon = new ImageIcon(Main.class.getResource("/resources/hospital.png"));
        Image hospitalImage = hospitalIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        JLabel hospitalLabel = new JLabel(new ImageIcon(hospitalImage));
        topPanel.add(hospitalLabel);
        
        // Navigation buttons panel
        JPanel navPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        navPanel.setBackground(Color.WHITE);
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        
        // Patients Button with Image
        JPanel patientPanel = createNavButton("/resources/patient.png", "Patients", 0);
        navPanel.add(patientPanel);
        
        // Doctors Button with Image
        JPanel doctorPanel = createNavButton("/resources/doctor.png", "Doctors", 1);
        navPanel.add(doctorPanel);
        
        // Appointments Button with Image
        JPanel appointmentPanel = createNavButton("/resources/appoinment.png", "Appointments", 2); // Change to appointment icon when available
        navPanel.add(appointmentPanel);
        
        // Add components to home panel
        homePanel.add(topPanel, BorderLayout.NORTH);
        homePanel.add(navPanel, BorderLayout.CENTER);
        
        // Status panel at bottom showing count of records
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel statusLabel = new JLabel(String.format("Patients: %d | Doctors: %d | Appointments: %d", 
                                        patientList.size(), doctorList.size(), appointmentList.size()));
        statusPanel.add(statusLabel);
        homePanel.add(statusPanel, BorderLayout.SOUTH);
    }
    
    private static JPanel createNavButton(String imagePath, String buttonText, final int tabIndex) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Image above button
        ImageIcon icon = new ImageIcon(Main.class.getResource(imagePath));
        Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(img));
        imgLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(imgLabel, BorderLayout.CENTER);
        
        // Button below image
        JButton button = new JButton(buttonText);
        button.setPreferredSize(new Dimension(120, 40));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToTab(tabIndex);
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(button);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private static void createTabs() {
        tabs = new JTabbedPane();
        tabs.addTab("Patients", new PatientPanel());
        tabs.addTab("Doctors", new DoctorPanel());
        tabs.addTab("Appointments", new AppointmentPanel());
    }
    
    private static void switchToTab(int tabIndex) {
        // Remove current panel
        frame.getContentPane().removeAll();
        
        // Add tabbed pane and select the requested tab
        frame.add(tabs, BorderLayout.CENTER);
        tabs.setSelectedIndex(tabIndex);
        
        // Refresh frame
        frame.revalidate();
        frame.repaint();
    }
    
    // Method to switch back to home panel
    public static void switchToHome() {
        frame.getContentPane().removeAll();
        
        // Recreate home panel to refresh statistics
        createHomePanel();
        
        frame.add(homePanel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }
    
    // Data model classes
    static class Patient implements Serializable {
        private String name;
        private int age;
        private String gender;
        private String contact;
        
        public Patient(String name, int age, String gender, String contact) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.contact = contact;
        }
        
        public String getName() { return name; }
        public int getAge() { return age; }
        public String getGender() { return gender; }
        public String getContact() { return contact; }
    }
    
    static class Doctor implements Serializable {
        private String name;
        private String specialization;
        private String experience;
        private String contact;
        private String availableHours;
        
        public Doctor(String name, String specialization, String experience, String contact, String availableHours) {
            this.name = name;
            this.specialization = specialization;
            this.experience = experience;
            this.contact = contact;
            this.availableHours = availableHours;
        }
        
        public String getName() { return name; }
        public String getSpecialization() { return specialization; }
        public String getExperience() { return experience; }
        public String getContact() { return contact; }
        public String getAvailableHours() { return availableHours; }
    }
    
    static class Appointment implements Serializable {
        private String patientName;
        private String doctorName;
        private String date;
        private String time;
        private String reason;
        
        public Appointment(String patientName, String doctorName, String date, String time, String reason) {
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.date = date;
            this.time = time;
            this.reason = reason;
        }
        
        public String getPatientName() { return patientName; }
        public String getDoctorName() { return doctorName; }
        public String getDate() { return date; }
        public String getTime() { return time; }
        public String getReason() { return reason; }
    }
    
    // Data persistence methods
    private static void saveData() {
        try {
            FileOutputStream fos = new FileOutputStream("hospital_data.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            
            oos.writeObject(patientList);
            oos.writeObject(doctorList);
            oos.writeObject(appointmentList);
            
            oos.close();
            fos.close();
            
            System.out.println("Data saved successfully");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error saving data: " + e.getMessage(),
                                         "Save Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadData() {
        try {
            File file = new File("hospital_data.ser");
            if (!file.exists()) {
                System.out.println("No existing data file found. Starting with empty lists.");
                return;
            }
            
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            
            patientList = (ArrayList<Patient>) ois.readObject();
            doctorList = (ArrayList<Doctor>) ois.readObject();
            appointmentList = (ArrayList<Appointment>) ois.readObject();
            
            ois.close();
            fis.close();
            
            System.out.println("Data loaded successfully");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data: " + e.getMessage());
            patientList = new ArrayList<>();
            doctorList = new ArrayList<>();
            appointmentList = new ArrayList<>();
        }
    }
    
    // Utility method to add patient
    public static void addPatient(Patient patient) {
        patientList.add(patient);
        saveData();
    }
    
    // Utility method to add doctor
    public static void addDoctor(Doctor doctor) {
        doctorList.add(doctor);
        saveData();
    }
    
    // Utility method to add appointment
    public static void addAppointment(Appointment appointment) {
        appointmentList.add(appointment);
        saveData();
    }
    
    // Getters for the lists
    public static ArrayList<Patient> getPatientList() { return patientList; }
    public static ArrayList<Doctor> getDoctorList() { return doctorList; }
    public static ArrayList<Appointment> getAppointmentList() { return appointmentList; }
}