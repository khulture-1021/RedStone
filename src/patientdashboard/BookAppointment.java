/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import com.toedter.calendar.JDateChooser;
import util.DatabaseConnection;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 *
 * @author bompe
 */
public class BookAppointment extends javax.swing.JFrame {

    private int patientId;
    private String username;
    
    // Static map of doctors by department (easy to extend)
    private static final Map<String, List<String>> DOCTORS_BY_DEPT = new LinkedHashMap<>();
    static {
        DOCTORS_BY_DEPT.put("Cardiology", Arrays.asList("Dr. A. Heart", "Dr. B. Pulse"));
        DOCTORS_BY_DEPT.put("Neurology", Arrays.asList("Dr. N. Brain", "Dr. C. Nerve"));
        DOCTORS_BY_DEPT.put("Pediatrics", Arrays.asList("Dr. P. Child", "Dr. K. Kids"));
        DOCTORS_BY_DEPT.put("Orthopedics", Arrays.asList("Dr. O. Bone", "Dr. S. Joint"));
        DOCTORS_BY_DEPT.put("Ophthalmology", Arrays.asList("Dr. V. Eye", "Dr. L. Sight"));
        DOCTORS_BY_DEPT.put("Dentistry", Arrays.asList("Dr. T. Tooth", "Dr. M. Smile"));
    }

    // Standard time slots (30-minute increments) - adjust as needed
    private static final List<String> ALL_TIME_SLOTS = Arrays.asList(
            "08:00","08:30","09:00","09:30","10:00","10:30",
            "11:00","11:30","12:00","12:30","13:00","13:30",
            "14:00","14:30","15:00","15:30","16:00"
    );

    /**
     * Creates new form BookAppointment
     */
    public BookAppointment(int patientId, String username) {
        this.patientId = patientId;
        this.username = username;
        initComponents();
        postInit();
    }
    
    // Common post-initialization
    private void postInit() {
        populateDepartments();
        setupListeners();
        // Close behavior: go back to dashboard if window closed (optional)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // optional: return to patient dashboard
                // patientsDash dash = new patientsDash(patientId, username);
                // dash.setVisible(true);
            }
        });
    }

    // Populate departments combo box
    private void populateDepartments() {
        cmbDepartment.removeAllItems();
        cmbDepartment.addItem("Select Department");
        for (String d : DOCTORS_BY_DEPT.keySet()) {
            cmbDepartment.addItem(d);
        }
    }

    private void setupListeners() {
        cmbDepartment.addActionListener(e -> {
            String dept = (String) cmbDepartment.getSelectedItem();
            if (dept != null && DOCTORS_BY_DEPT.containsKey(dept)) {
                populateDoctors(dept);
            } else {
                cmbDoctor.removeAllItems();
                cmbDoctor.addItem("Select Doctor");
            }
            lblStatus.setText("");
        });

        // When doctor or date changes -> refresh times
        cmbDoctor.addActionListener(e -> refreshAvailableTimes());
        jDateChooser1.addPropertyChangeListener("date", evt -> refreshAvailableTimes());

        btnBook.addActionListener(e -> handleBookAction());
        btnBack.addActionListener(e -> {
            // Return to patientsDash
            patientsDash dash = new patientsDash(patientId, username);
            dash.setVisible(true);
            this.dispose();
        });
    }

    private void populateDoctors(String department) {
        cmbDoctor.removeAllItems();
        cmbDoctor.addItem("Select Doctor");
        List<String> docs = DOCTORS_BY_DEPT.getOrDefault(department, Collections.emptyList());
        for (String d : docs) cmbDoctor.addItem(d);
    }

    // Refresh times by removing already-booked slots for selected doctor & date
    private void refreshAvailableTimes() {
        lblStatus.setText("");
        cmbTime.removeAllItems();
        cmbTime.addItem("Select Time");

        String doctor = (String) cmbDoctor.getSelectedItem();
        java.util.Date selectedDate = jDateChooser1.getDate();

        if (doctor == null || doctor.equals("Select Doctor") || selectedDate == null) {
            return;
        }

        // Format date for SQL
        java.sql.Date sqlDate = new java.sql.Date(selectedDate.getTime());


        // Get booked times from DB
        Set<String> booked = new HashSet<>();
        String sql = "SELECT appointmentTime FROM appointments WHERE doctor = ? AND appointmentDate = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, doctor);
            pst.setDate(2, new java.sql.Date(sqlDate.getTime()));
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    booked.add(rs.getString("appointmentTime"));
                }
            }
        } catch (Exception ex) {
            lblStatus.setText("Error loading booked times: " + ex.getMessage());
            return;
        }

        // Fill remaining time slots
        for (String slot : ALL_TIME_SLOTS) {
            if (!booked.contains(slot)) {
                cmbTime.addItem(slot);
            }
        }
    }

    // Validate inputs before booking
    private boolean validateAppointmentForm() {
        boolean ok = true;
        lblDeptError.setText("");
        lblDoctorError.setText("");
        lblTimeError.setText("");
        lblTimeError.setText("");
        lblReasonError.setText("");
        lblStatus.setText("");

        if (cmbDepartment.getSelectedIndex() <= 0) {
            lblDeptError.setText("Choose department");
            ok = false;
        }
        if (cmbDoctor.getSelectedIndex() <= 0) {
            lblDoctorError.setText("Choose doctor");
            ok = false;
        }
        if (jDateChooser1.getDate() == null) {
            lblTimeError.setText("Select date");
            ok = false;
        } else {
            // no past dates
            LocalDate chosen = jDateChooser1.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (chosen.isBefore(LocalDate.now())) {
                lblTimeError.setText("Cannot choose past date");
                ok = false;
            }
        }
        if (cmbTime.getSelectedIndex() <= 0) {
            lblTimeError.setText("Choose time");
            ok = false;
        }
        if (txtReason.getText().trim().isEmpty()) {
            lblReasonError.setText("Please describe reason");
            ok = false;
        }
        return ok;
    }

    // Booking action: insert appointment and show success
    private void handleBookAction() {
        if (!validateAppointmentForm()) return;

        String department = (String) cmbDepartment.getSelectedItem();
        String doctor = (String) cmbDoctor.getSelectedItem();
        String time = (String) cmbTime.getSelectedItem();
        java.util.Date date = jDateChooser1.getDate();
        String reason = txtReason.getText().trim();

        String sql = "INSERT INTO appointments (patientId, department, doctor, appointmentDate, appointmentTime, reason) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setInt(1, patientId);
            pst.setString(2, department);
            pst.setString(3, doctor);
            pst.setDate(4, new java.sql.Date(date.getTime())); // appointmentDate
            pst.setString(5, time); // appointmentTime as HH:mm string
            pst.setString(6, reason);

            pst.executeUpdate();

            // get generated appointment id if needed
            try (ResultSet gen = pst.getGeneratedKeys()) {
                if (gen.next()) {
                    int appointmentId = gen.getInt(1);
                    // student-style: we can print or use this id
                    System.out.println("New appointmentId = " + appointmentId);
                }
            }

            JOptionPane.showMessageDialog(this, "Appointment booked successfully!");
            // go back to dashboard or clear form
            patientsDash dash = new patientsDash(patientId, username);
            dash.setVisible(true);
            this.dispose();

        } catch (SQLIntegrityConstraintViolationException dup) {
            lblStatus.setText("Duplicate appointment or constraint error.");
        } catch (Exception ex) {
            lblStatus.setText("Booking error: " + ex.getMessage());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        btnBack = new javax.swing.JButton();
        NavBtnCancelAppointment = new javax.swing.JButton();
        NavBtnEditProfile = new javax.swing.JButton();
        NavBtnMedicalHistory = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtReason = new javax.swing.JTextField();
        cmbDepartment = new javax.swing.JComboBox<>();
        cmbDoctor = new javax.swing.JComboBox<>();
        cmbTime = new javax.swing.JComboBox<>();
        jDateChooser2 = new com.toedter.calendar.JDateChooser();
        jLabel6 = new javax.swing.JLabel();
        btnBook = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();
        lblDeptError = new javax.swing.JLabel();
        lblDoctorError = new javax.swing.JLabel();
        lblTimeError = new javax.swing.JLabel();
        lblReasonError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(0, 121, 151));

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/2.png"))); // NOI18N
        jLabel8.setText("Redstone Health Center");

        btnBack.setBackground(new java.awt.Color(0, 121, 151));
        btnBack.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnBack.setForeground(new java.awt.Color(255, 255, 255));
        btnBack.setText("Dash board");
        btnBack.setBorder(null);
        btnBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBackActionPerformed(evt);
            }
        });

        NavBtnCancelAppointment.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnCancelAppointment.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnCancelAppointment.setText("Cancel Appointment");
        NavBtnCancelAppointment.setBorder(null);
        NavBtnCancelAppointment.setBorderPainted(false);
        NavBtnCancelAppointment.setContentAreaFilled(false);
        NavBtnCancelAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnCancelAppointmentActionPerformed(evt);
            }
        });

        NavBtnEditProfile.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnEditProfile.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnEditProfile.setText("Edit Profile");
        NavBtnEditProfile.setBorder(null);
        NavBtnEditProfile.setBorderPainted(false);
        NavBtnEditProfile.setContentAreaFilled(false);
        NavBtnEditProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnEditProfileActionPerformed(evt);
            }
        });

        NavBtnMedicalHistory.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnMedicalHistory.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnMedicalHistory.setText("Medical History");
        NavBtnMedicalHistory.setBorder(null);
        NavBtnMedicalHistory.setBorderPainted(false);
        NavBtnMedicalHistory.setContentAreaFilled(false);
        NavBtnMedicalHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnMedicalHistoryActionPerformed(evt);
            }
        });

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setText("Help");
        jButton7.setBorder(null);
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NavBtnCancelAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnBack)
                            .addComponent(NavBtnEditProfile)
                            .addComponent(NavBtnMedicalHistory)
                            .addComponent(jButton7))
                        .addGap(63, 63, 63))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(79, 79, 79)
                .addComponent(btnBack)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(NavBtnCancelAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnEditProfile)
                .addGap(18, 18, 18)
                .addComponent(NavBtnMedicalHistory)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel1.setText("Select Department");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Select Doctor");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Select Date");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Select time");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Reason");

        txtReason.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        cmbDepartment.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Cardiology", "Neurology", "Pediatrics", "Orthopedics", "Ophthalmology", "Dentistry", " " }));
        cmbDepartment.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));
        cmbDepartment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbDepartmentActionPerformed(evt);
            }
        });

        cmbDoctor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbDoctor.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        cmbTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00" }));
        cmbTime.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Appointment Booking");

        btnBook.setBackground(new java.awt.Color(0, 204, 0));
        btnBook.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnBook.setForeground(new java.awt.Color(255, 255, 255));
        btnBook.setText("Submit");
        btnBook.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(92, 184, 92), 2, true));
        btnBook.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBookActionPerformed(evt);
            }
        });

        lblStatus.setText("jLabel7");

        lblDeptError.setText("jLabel7");

        lblDoctorError.setText("jLabel7");

        lblTimeError.setText("jLabel7");

        lblReasonError.setText("jLabel7");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(167, 167, 167)
                        .addComponent(lblStatus)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 291, Short.MAX_VALUE)
                        .addComponent(lblTimeError)
                        .addGap(401, 401, 401))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(24, 24, 24)
                                    .addComponent(jLabel6))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addGap(40, 40, 40)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(txtReason, javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel5)
                                                .addGroup(jPanel1Layout.createSequentialGroup()
                                                    .addGap(20, 20, 20)
                                                    .addComponent(lblReasonError)))
                                            .addGap(0, 0, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(jLabel3)
                                                    .addComponent(cmbDepartment, 0, 356, Short.MAX_VALUE)
                                                    .addComponent(jDateChooser2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 67, Short.MAX_VALUE)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel2)
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(cmbDoctor, 0, 400, Short.MAX_VALUE)
                                                    .addComponent(cmbTime, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(jLabel4)))))))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(58, 58, 58)
                                .addComponent(lblDeptError)
                                .addGap(393, 393, 393)
                                .addComponent(lblDoctorError))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addGap(271, 271, 271)
                                .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 332, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel6)
                .addGap(57, 57, 57)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbDoctor, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbDepartment, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblDeptError)
                    .addComponent(lblDoctorError))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(37, 37, 37)
                        .addComponent(jLabel3)
                        .addGap(60, 60, 60)
                        .addComponent(lblStatus)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(40, 40, 40)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jDateChooser2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbTime, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTimeError)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 47, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addComponent(txtReason, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblReasonError)
                        .addGap(53, 53, 53)
                        .addComponent(btnBook, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(78, 78, 78))))
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void cmbDepartmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbDepartmentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbDepartmentActionPerformed

    private void btnBookActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBookActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBookActionPerformed

    private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
        // TODO add your handling code here:
        
    }//GEN-LAST:event_btnBackActionPerformed

    private void NavBtnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnCancelAppointmentActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnCancelAppointmentActionPerformed

    private void NavBtnEditProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnEditProfileActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnEditProfileActionPerformed

    private void NavBtnMedicalHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnMedicalHistoryActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnMedicalHistoryActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton NavBtnCancelAppointment;
    private javax.swing.JButton NavBtnEditProfile;
    private javax.swing.JButton NavBtnMedicalHistory;
    private javax.swing.JButton btnBack;
    private javax.swing.JButton btnBook;
    private javax.swing.JComboBox<String> cmbDepartment;
    private javax.swing.JComboBox<String> cmbDoctor;
    private javax.swing.JComboBox<String> cmbTime;
    private javax.swing.JButton jButton7;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDateChooser jDateChooser2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblDeptError;
    private javax.swing.JLabel lblDoctorError;
    private javax.swing.JLabel lblReasonError;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTimeError;
    private javax.swing.JTextField txtReason;
    // End of variables declaration//GEN-END:variables
}
