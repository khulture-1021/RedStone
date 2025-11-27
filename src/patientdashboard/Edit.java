/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

/**
 *
 * @author bompe
 */
public class Edit extends javax.swing.JFrame {

    private int patientId;
    private String username;
    /**
     * Creates new form Edit
     */
    public Edit(int patientId, String username) {
        this.patientId = patientId;
        this.username = username;
        initComponents();
        setup();
        loadAllData();
    }

    private void setup() {
        // fill combos
        cmbGender.removeAllItems();
        cmbGender.addItem("MALE");
        cmbGender.addItem("FEMALE");

        cmbBloodGroup.removeAllItems();
        cmbBloodGroup.addItem("Unknown");
        cmbBloodGroup.addItem("A+");
        cmbBloodGroup.addItem("A-");
        cmbBloodGroup.addItem("B+");
        cmbBloodGroup.addItem("B-");
        cmbBloodGroup.addItem("AB+");
        cmbBloodGroup.addItem("AB-");
        cmbBloodGroup.addItem("O+");
        cmbBloodGroup.addItem("O-");

        // listeners
        btnSave.addActionListener(e -> saveChanges());
        btnDashboard.addActionListener(e -> {
            patientsDash dash = new patientsDash(patientId, username);
            dash.setVisible(true);
            this.dispose();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    patientsDash dash = new patientsDash(patientId, username);
                    dash.setVisible(true);
                } catch (Exception ex) { /* ignore */ }
            }
        });
    }

    // Calculate age from DOB
    private int calculateAgeFromDOB() {
        if (dateChooserDOB.getDate() == null) return -1;
        LocalDate birth = dateChooserDOB.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Period.between(birth, LocalDate.now()).getYears();
    }

    // Load patient data into form and top table (HORIZONTAL TABLE LIKE YOUR SCREENSHOT)
    private void loadAllData() {
        DefaultTableModel tableModel = (DefaultTableModel) tblPatients.getModel();

        // Make sure the table has COLUMN HEADERS (horizontal layout)
        tableModel.setColumnCount(0);
        tableModel.setRowCount(0);

        // Set columns exactly as your screenshot
        tableModel.addColumn("Username");
        tableModel.addColumn("First Name");
        tableModel.addColumn("Last Name");
        tableModel.addColumn("Gender");
        tableModel.addColumn("Email");
        tableModel.addColumn("Phone");
        tableModel.addColumn("DOB");
        tableModel.addColumn("Address");
        tableModel.addColumn("Blood Type");
        tableModel.addColumn("Allergies");

        // Prepare row data
        Object[] rowData = new Object[10];

        try (Connection conn = DatabaseConnection.getConnection()) {

            // ------------ LOAD PATIENT BASIC INFO ------------
            String sql = "SELECT * FROM patients WHERE username = ? OR patientId = ?";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, username);
                pst.setInt(2, patientId);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        this.patientId = rs.getInt("patientId");

                        txtUsername.setText(rs.getString("username"));
                        txtFirstName.setText(rs.getString("firstName"));
                        txtLastName.setText(rs.getString("lastName"));
                        cmbGender.setSelectedItem(rs.getString("gender"));
                        txtEmail.setText(rs.getString("email"));
                        txtPhone.setText(rs.getString("phone"));

                        Date dob = rs.getDate("dateOfBirth");
                        if (dob != null) dateChooserDOB.setDate(new java.util.Date(dob.getTime()));

                        txtAddress.setText(rs.getString("address"));

                        // Fill table row array
                        rowData[0] = rs.getString("username");
                        rowData[1] = rs.getString("firstName");
                        rowData[2] = rs.getString("lastName");
                        rowData[3] = rs.getString("gender");
                        rowData[4] = rs.getString("email");
                        rowData[5] = rs.getString("phone");
                        rowData[6] = rs.getString("dateOfBirth");
                        rowData[7] = rs.getString("address");
                    }
                }
            }

            // ------------ LOAD MEDICAL INFO (only bloodType + allergies) ------------
            String sql2 = "SELECT bloodType, allergies FROM patient_medical_info WHERE patientId = ?";
            try (PreparedStatement pst2 = conn.prepareStatement(sql2)) {
                pst2.setInt(1, this.patientId);
                try (ResultSet rs2 = pst2.executeQuery()) {
                    if (rs2.next()) {
                        cmbBloodGroup.setSelectedItem(rs2.getString("bloodType"));
                        txtAllergies.setText(rs2.getString("allergies"));

                        rowData[8] = rs2.getString("bloodType");
                        rowData[9] = rs2.getString("allergies");
                    } else {
                        // No medical record yet → default blood group + empty allergies
                        cmbBloodGroup.setSelectedIndex(0);
                        rowData[8] = "Unknown";
                        rowData[9] = "";
                    }
                }
            }

            // ADD THE ONE SINGLE ROW TO MATCH SCREENSHOT
            tableModel.addRow(rowData);

        } catch (Exception e) {
            lblStatus.setText("Load error: " + e.getMessage());
        }
    }


    // Validate fields
    private boolean validateFields() {
        boolean ok = true;
        lblFirstNameError.setText("");
        lblLastNameError.setText("");
        lblEmailError.setText("");
        lblPhoneError.setText("");
        lblDOBError.setText("");
        lblAddressError.setText("");
        lblBloodError.setText("");
        lblStatus.setText("");

        if (txtFirstName.getText().trim().isEmpty()) {
            lblFirstNameError.setText("Required");
            ok = false;
        }
        if (txtLastName.getText().trim().isEmpty()) {
            lblLastNameError.setText("Required");
            ok = false;
        }
        String email = txtEmail.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblEmailError.setText("Invalid");
            ok = false;
        }
        String phone = txtPhone.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{7,15}")) {
            lblPhoneError.setText("Invalid");
            ok = false;
        }
        if (dateChooserDOB.getDate() == null) {
            lblDOBError.setText("Select DOB");
            ok = false;
        } else {
            int age = calculateAgeFromDOB();
            if (age < 0 || age > 120) {
                lblDOBError.setText("Invalid DOB");
                ok = false;
            }
        }
        if (txtAddress.getText().trim().isEmpty()) {
            lblAddressError.setText("Required");
            ok = false;
        }
        if (cmbBloodGroup.getSelectedIndex() == -1 || cmbBloodGroup.getSelectedItem() == null) {
            lblBloodError.setText("Select");
            ok = false;
        }
        return ok;
    }

    // Save updates to DB
    private void saveChanges() {
        if (!validateFields()) {
            lblStatus.setText("Fix errors and try again.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Update patients
            String updatePatients = "UPDATE patients SET firstName=?, lastName=?, gender=?, age=?, email=?, phone=?, dateOfBirth=?, address=? WHERE patientId=?";
            try (PreparedStatement pst = conn.prepareStatement(updatePatients)) {
                pst.setString(1, txtFirstName.getText().trim());
                pst.setString(2, txtLastName.getText().trim());
                pst.setString(3, cmbGender.getSelectedItem() != null ? cmbGender.getSelectedItem().toString() : null);
                pst.setInt(4, calculateAgeFromDOB());
                pst.setString(5, txtEmail.getText().trim());
                pst.setString(6, txtPhone.getText().trim());
                java.util.Date dob = dateChooserDOB.getDate();
                pst.setDate(7, new java.sql.Date(dob.getTime()));
                pst.setString(8, txtAddress.getText().trim());
                pst.setInt(9, this.patientId);
                pst.executeUpdate();
            }

            // Update or insert medical info (bloodType, allergies)
            String check = "SELECT patientId FROM patient_medical_info WHERE patientId = ?";
            try (PreparedStatement chk = conn.prepareStatement(check)) {
                chk.setInt(1, this.patientId);
                try (ResultSet rs = chk.executeQuery()) {
                    if (rs.next()) {
                        String updMed = "UPDATE patient_medical_info SET bloodType=?, allergies=? WHERE patientId=?";
                        try (PreparedStatement up = conn.prepareStatement(updMed)) {
                            up.setString(1, cmbBloodGroup.getSelectedItem().toString());
                            up.setString(2, txtAllergies.getText().trim());
                            up.setInt(3, this.patientId);
                            up.executeUpdate();
                        }
                    } else {
                        String ins = "INSERT INTO patient_medical_info (patientId, bloodType, allergies) VALUES (?, ?, ?)";
                        try (PreparedStatement insP = conn.prepareStatement(ins)) {
                            insP.setInt(1, this.patientId);
                            insP.setString(2, cmbBloodGroup.getSelectedItem().toString());
                            insP.setString(3, txtAllergies.getText().trim());
                            insP.executeUpdate();
                        }
                    }
                }
            }

            lblStatus.setForeground(java.awt.Color.GREEN.darker());
            lblStatus.setText("Saved successfully.");

            // refresh top table
            loadAllData();

        } catch (Exception e) {
            lblStatus.setForeground(java.awt.Color.red);
            lblStatus.setText("Save error: " + e.getMessage());
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        NavBtnBookAppointment = new javax.swing.JButton();
        NavBtnCancelAppointment = new javax.swing.JButton();
        NavBtnMedicalHistory = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblPatients = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        txtLastName = new javax.swing.JTextField();
        txtFirstName = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        txtPhone = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        cmbGender = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtAddress = new javax.swing.JTextField();
        cmbBloodGroup = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        btnSave = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        dateChooserDOB = new com.toedter.calendar.JDateChooser();
        txtUsername = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAllergies = new javax.swing.JTextArea();
        lblStatus = new javax.swing.JLabel();
        lblFirstNameError = new javax.swing.JLabel();
        lblLastNameError = new javax.swing.JLabel();
        lblEmailError = new javax.swing.JLabel();
        lblPhoneError = new javax.swing.JLabel();
        lblDOBError = new javax.swing.JLabel();
        lblAddressError = new javax.swing.JLabel();
        lblBloodError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(0, 121, 151));

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/2.png"))); // NOI18N
        jLabel8.setText("Redstone Health Center");

        btnDashboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("Dash Board");
        btnDashboard.setBorder(null);
        btnDashboard.setBorderPainted(false);
        btnDashboard.setContentAreaFilled(false);
        btnDashboard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDashboardActionPerformed(evt);
            }
        });

        NavBtnBookAppointment.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnBookAppointment.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnBookAppointment.setText("Book Appointment");
        NavBtnBookAppointment.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        NavBtnBookAppointment.setBorderPainted(false);
        NavBtnBookAppointment.setContentAreaFilled(false);
        NavBtnBookAppointment.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnBookAppointmentActionPerformed(evt);
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
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NavBtnBookAppointment)
                            .addComponent(btnDashboard)
                            .addComponent(NavBtnCancelAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NavBtnMedicalHistory)
                            .addComponent(jButton7))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(68, 68, 68)
                .addComponent(btnDashboard)
                .addGap(18, 18, 18)
                .addComponent(NavBtnBookAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnCancelAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnMedicalHistory)
                .addGap(18, 18, 18)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tblPatients.setBackground(new java.awt.Color(0, 153, 153));
        tblPatients.setForeground(new java.awt.Color(255, 255, 255));
        tblPatients.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Name", "Last Name", "Email", "Contact No", "D.O.B", "Blood Group", "Address", "Gender", "Username", "Password"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblPatients);

        jLabel1.setText("Name");

        txtLastName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        txtFirstName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));
        txtFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFirstNameActionPerformed(evt);
            }
        });

        jLabel2.setText(" Last Name");

        jLabel3.setText("Email");

        txtPhone.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        jLabel4.setText("Phone");

        txtEmail.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        cmbGender.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        jLabel5.setText("Gender");

        jLabel6.setText("Date of birth");

        jLabel7.setText("Address");

        txtAddress.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        cmbBloodGroup.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-" }));
        cmbBloodGroup.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        jLabel9.setText("Blood Group");

        btnSave.setBackground(new java.awt.Color(0, 204, 0));
        btnSave.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Save");
        btnSave.setBorder(null);

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setForeground(new java.awt.Color(102, 102, 102));
        jLabel10.setText("Edit only the field you want changed");

        txtUsername.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 153, 153), 2, true));

        jLabel11.setText("Username");

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel13.setText("Edit Profile");

        jLabel14.setText("Allergies");
        jLabel14.setToolTipText("");

        txtAllergies.setColumns(20);
        txtAllergies.setRows(5);
        jScrollPane2.setViewportView(txtAllergies);

        lblStatus.setText("jLabel12");

        lblFirstNameError.setText("jLabel12");

        lblLastNameError.setText("jLabel12");

        lblEmailError.setText("jLabel12");

        lblPhoneError.setText("jLabel12");

        lblDOBError.setText("jLabel12");

        lblAddressError.setText("jLabel12");

        lblBloodError.setText("jLabel12");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(46, 46, 46)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel7)
                                        .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(jLabel3)
                                                .addComponent(jLabel1))
                                            .addGap(211, 211, 211)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jLabel4)
                                                .addComponent(jLabel2)
                                                .addComponent(lblLastNameError)))
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                            .addComponent(lblEmailError)
                                            .addGap(200, 200, 200)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(lblPhoneError)
                                                .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(lblFirstNameError)
                                    .addComponent(lblAddressError))
                                .addGap(48, 48, 48)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel6)
                                            .addComponent(jLabel5)
                                            .addComponent(cmbBloodGroup, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(cmbGender, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(dateChooserDOB, javax.swing.GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                                            .addComponent(jLabel9))
                                        .addGap(27, 27, 27)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(txtUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                                    .addComponent(lblDOBError)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(6, 6, 6)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 867, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel10)
                                    .addComponent(jLabel13)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(343, 343, 343)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(lblBloodError)
                                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addContainerGap(25, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(lblStatus)
                        .addGap(411, 411, 411))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel13)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5)
                        .addComponent(jLabel11))
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblFirstNameError)
                    .addComponent(lblLastNameError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(14, 14, 14)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(dateChooserDOB, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(txtPhone, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmailError)
                    .addComponent(lblPhoneError)
                    .addComponent(lblDOBError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jLabel7)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cmbBloodGroup, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblAddressError))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(18, 18, Short.MAX_VALUE)
                                .addComponent(lblBloodError))))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblStatus)
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void txtFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtFirstNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtFirstNameActionPerformed

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        // TODO add your handling code here:
        new patientsDash(patientId, username).setVisible(true);
        dispose();
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void NavBtnBookAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnBookAppointmentActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnBookAppointmentActionPerformed

    private void NavBtnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnCancelAppointmentActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnCancelAppointmentActionPerformed

    private void NavBtnMedicalHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnMedicalHistoryActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnMedicalHistoryActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton NavBtnBookAppointment;
    private javax.swing.JButton NavBtnCancelAppointment;
    private javax.swing.JButton NavBtnMedicalHistory;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbBloodGroup;
    private javax.swing.JComboBox<String> cmbGender;
    private com.toedter.calendar.JDateChooser dateChooserDOB;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAddressError;
    private javax.swing.JLabel lblBloodError;
    private javax.swing.JLabel lblDOBError;
    private javax.swing.JLabel lblEmailError;
    private javax.swing.JLabel lblFirstNameError;
    private javax.swing.JLabel lblLastNameError;
    private javax.swing.JLabel lblPhoneError;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JTable tblPatients;
    private javax.swing.JTextField txtAddress;
    private javax.swing.JTextArea txtAllergies;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtPhone;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
