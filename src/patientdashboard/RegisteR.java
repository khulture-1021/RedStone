/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.sql.DriverManager;



/**
 *
 * @author bompe
 */
public class RegisteR extends javax.swing.JFrame {
    /**
     * Creates new form RegisteR
     */
    public RegisteR() {
        initComponents();
        addRealtimeValidation();
        setupDOBListener();
    }
    
    // ------------------------------
    // REALTIME VALIDATION
    // ------------------------------
    private void addRealtimeValidation() {

        txtUsername.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validateUsername(); }
        });

        txtPassword.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validatePassword(); }
        });

        txtConfirmPassword.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validateConfirmPassword(); }
        });

        txtEmail.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validateEmail(); }
        });

        txtPhoneNumber.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validatePhone(); }
        });

        txtIDNumber.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validateID(); }
        });
    }

    // ------------------------------
    // AUTO CALCULATE AGE FROM DOB
    // ------------------------------
    private void setupDOBListener() {
        dateChooserDOB.addPropertyChangeListener("date", evt -> calculateAgeFromDOB());
    }

    private int calculateAgeFromDOB() {
        if (dateChooserDOB.getDate() == null) {
            return -1;
        }

        java.util.Date dob = dateChooserDOB.getDate();
        LocalDate birthDate = dob.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return Period.between(birthDate, LocalDate.now()).getYears();
    }


    // ------------------------------
    // VALIDATION RULES
    // ------------------------------
    private boolean validateUsername() {
        if (txtUsername.getText().trim().isEmpty()) {
            lblUsernameError.setText("Username required");
            return false;
        }
        lblUsernameError.setText("");
        return true;
    }

    private boolean validatePassword() {
        if (txtPassword.getText().trim().length() < 6) {
            lblPasswordError.setText("Min 6 characters");
            return false;
        }
        lblPasswordError.setText("");
        return true;
    }

    private boolean validateConfirmPassword() {
        if (!txtPassword.getText().equals(txtConfirmPassword.getText())) {
            lblConfirmError.setText("Passwords do not match");
            return false;
        }
        lblConfirmError.setText("");
        return true;
    }

    private boolean validateEmail() {
        if (!txtEmail.getText().trim().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            lblEmailError.setText("Invalid email");
            return false;
        }
        lblEmailError.setText("");
        return true;
    }

    private boolean validatePhone() {
        if (!txtPhoneNumber.getText().trim().matches("\\d{10,13}")) {
            lblPhoneError.setText("Invalid phone");
            return false;
        }
        lblPhoneError.setText("");
        return true;
    }

    private boolean validateID() {
        if (txtIDNumber.getText().trim().length() < 6) {
            lblIDError.setText("Invalid ID");
            return false;
        }
        lblIDError.setText("");
        return true;
    }


    // ------------------------------
    // SUBMIT BUTTON — SAVE TO MYSQL
    // ------------------------------
    private void registerUser() {
        if (!validateUsername() || !validatePassword() || !validateConfirmPassword()
                || !validateEmail() || !validatePhone() || !validateID()) {
            lblStatus.setText("Fix errors before submitting");
            return;
        }

        if (dateChooserDOB.getDate() == null) {
            lblStatus.setText("Select a valid Date of Birth");
            return;
        }

        int age = calculateAgeFromDOB();
        if (age < 0 || age > 120) {
            lblStatus.setText("Invalid Date of Birth");
            return;
        }

        String sql = "INSERT INTO patients "
                + "(username, password, firstName, lastName, gender, age, email, phone, dateOfBirth, idNumber) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/redstone", "root", "");
             PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pst.setString(1, txtUsername.getText());
            pst.setString(2, txtPassword.getText());
            pst.setString(3, txtFirstName.getText());
            pst.setString(4, txtLastName.getText());
            pst.setString(5, cmbGender.getSelectedItem().toString());
            pst.setInt(6, age);
            pst.setString(7, txtEmail.getText());
            pst.setString(8, txtPhoneNumber.getText());

            java.util.Date dob = dateChooserDOB.getDate();
            pst.setDate(9, new java.sql.Date(dob.getTime()));

            pst.setString(10, txtIDNumber.getText());

            pst.executeUpdate();

            // Get auto-generated patient ID
            ResultSet rs = pst.getGeneratedKeys();
            int newId = -1;
            if (rs.next()) {
                newId = rs.getInt(1);
            }

            lblStatus.setText("Registered Successfully!");

            // OPEN patient dashboard
            patientsDash dash = new patientsDash(newId, txtUsername.getText());
            dash.setVisible(true);
            this.dispose();

        } catch (Exception e) {
            lblStatus.setText("Error: " + e.getMessage());
        }
    }

    
    // ------------------------------
    // Simple Listener Class
    // ------------------------------
    private interface SimpleDocumentListener extends DocumentListener {
        void update();
        @Override default void insertUpdate(DocumentEvent e) { update(); }
        @Override default void removeUpdate(DocumentEvent e) { update(); }
        @Override default void changedUpdate(DocumentEvent e) { update(); }
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
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtFirstName = new javax.swing.JTextField();
        txtLastName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtEmail = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtPhoneNumber = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        dateChooserDOB = new com.toedter.calendar.JDateChooser();
        jLabel9 = new javax.swing.JLabel();
        txtIDNumber = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JTextField();
        txtUsername = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        cmbGender = new javax.swing.JComboBox<>();
        lblFirstNameError = new javax.swing.JLabel();
        lblLastNameError = new javax.swing.JLabel();
        lblEmailError = new javax.swing.JLabel();
        lblDOBError = new javax.swing.JLabel();
        lblPhoneError = new javax.swing.JLabel();
        lblIDError = new javax.swing.JLabel();
        lbl = new javax.swing.JLabel();
        lblUsernameError = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        txtConfirmPassword = new javax.swing.JTextField();
        lblPasswordError = new javax.swing.JLabel();
        lblConfirmError = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/1762982264048.jpg"))); // NOI18N
        jPanel2.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 600));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel2.setText("Sign Up");

        jLabel3.setText("First Name:");

        jLabel4.setText("Last Name:");

        txtFirstName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));
        txtFirstName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtFirstNameActionPerformed(evt);
            }
        });

        txtLastName.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        jLabel5.setText("Email:");

        txtEmail.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        jLabel7.setText("Phone Number:");

        txtPhoneNumber.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        jLabel8.setText("Date Of Birth:");

        dateChooserDOB.setBackground(new java.awt.Color(0, 121, 151));

        jLabel9.setText("ID / Passport number:");

        txtIDNumber.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        jLabel10.setText("Gender:");

        jLabel11.setText("Username:");

        txtPassword.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        txtUsername.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));
        txtUsername.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtUsernameActionPerformed(evt);
            }
        });

        jLabel12.setText("Password:");

        jButton1.setBackground(new java.awt.Color(0, 204, 51));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Submit");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Male", "Female" }));
        cmbGender.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        lblFirstNameError.setText("Password:");

        lblLastNameError.setText("Password:");

        lblEmailError.setText("Password:");

        lblDOBError.setText("Password:");

        lblPhoneError.setText("Password:");

        lblIDError.setText("Password:");

        lbl.setText("Password:");

        lblUsernameError.setText("Password:");

        jLabel21.setText("Confirm Password");

        txtConfirmPassword.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        lblPasswordError.setText("Password:");

        lblConfirmError.setText("Password:");

        lblStatus.setText("jLabel6");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(29, 29, 29)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel12)
                                .addGap(108, 514, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(txtPassword)
                                    .addComponent(txtFirstName)
                                    .addComponent(txtEmail)
                                    .addComponent(txtPhoneNumber)
                                    .addComponent(cmbGender, 0, 264, Short.MAX_VALUE))
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(jLabel4)
                                                    .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addComponent(dateChooserDOB, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(txtIDNumber, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE))
                                            .addComponent(jLabel8)
                                            .addComponent(jLabel9)
                                            .addComponent(lblDOBError)
                                            .addComponent(lblLastNameError)
                                            .addComponent(lblUsernameError)
                                            .addComponent(lblIDError)
                                            .addComponent(jLabel11)))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(63, 63, 63)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txtConfirmPassword)
                                            .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(lblConfirmError)
                                                    .addComponent(jLabel21))
                                                .addGap(0, 0, Short.MAX_VALUE))))))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel10)
                                    .addComponent(lblFirstNameError)
                                    .addComponent(lblEmailError)
                                    .addComponent(lblPhoneError)
                                    .addComponent(lbl)
                                    .addComponent(lblPasswordError))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(28, 28, 28))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 171, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 294, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(159, 159, 159))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(lblStatus)
                                .addGap(285, 285, 285))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addGap(253, 253, 253))))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txtLastName, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lblFirstNameError)
                            .addComponent(lblLastNameError))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jLabel8))
                        .addGap(9, 9, 9)
                        .addComponent(txtEmail, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(dateChooserDOB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEmailError)
                    .addComponent(lblDOBError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPhoneNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtIDNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPhoneError)
                    .addComponent(lblIDError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbGender, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl)
                    .addComponent(lblUsernameError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jLabel21))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtConfirmPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblPasswordError)
                    .addComponent(lblConfirmError))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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

    private void txtUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtUsernameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtUsernameActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        registerUser();
    }//GEN-LAST:event_jButton1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RegisteR.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RegisteR.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RegisteR.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RegisteR.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RegisteR().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> cmbGender;
    private com.toedter.calendar.JDateChooser dateChooserDOB;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbl;
    private javax.swing.JLabel lblConfirmError;
    private javax.swing.JLabel lblDOBError;
    private javax.swing.JLabel lblEmailError;
    private javax.swing.JLabel lblFirstNameError;
    private javax.swing.JLabel lblIDError;
    private javax.swing.JLabel lblLastNameError;
    private javax.swing.JLabel lblPasswordError;
    private javax.swing.JLabel lblPhoneError;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblUsernameError;
    private javax.swing.JTextField txtConfirmPassword;
    private javax.swing.JTextField txtEmail;
    private javax.swing.JTextField txtFirstName;
    private javax.swing.JTextField txtIDNumber;
    private javax.swing.JTextField txtLastName;
    private javax.swing.JTextField txtPassword;
    private javax.swing.JTextField txtPhoneNumber;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
