/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.prefs.Preferences;
import util.DatabaseConnection;
/**
 *
 * @author tshiy
 */
public class logIN extends javax.swing.JFrame {

    private JCheckBox chkRememberMe;
    private JDialog loadingDialog;
    
    /**
     * Creates new form logIN
     */
    public logIN() {
        initComponents();
        styleComponents();
        lblStatus.setText("");
        lblUserError.setText("");
        lblPassError.setText("");
        lblRoleError.setText("");
        setupRememberMe();
        setupLoadingSpinner();
        applyFadeInEffect();
        addRealtimeValidation();
        addPasswordToggle();
    }
    
    private boolean sendOtpByEmail(String toEmail, String otp) {

        final String fromEmail = "tshiyeya@gmail.com";      // your email
        final String password = "abcd efgh ijkl mnop";      // your Gmail app password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Your Redstone Clinic OTP Code");
            message.setText(
                    "Hello,\n\n" +
                    "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                    "This code expires in 5 minutes.\n\n" +
                    "Redstone Health Center\n" +
                    "Community Care System"
            );

            Transport.send(message);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    
    private void setupRememberMe() {
        chkRememberMe = new JCheckBox("Remember Me");
        chkRememberMe.setOpaque(false);
        chkRememberMe.setForeground(Color.DARK_GRAY);

        // Add checkbox under password field
        jPanel1.add(chkRememberMe, 
            new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 330, -1, -1));

        // Load saved details
        Preferences pref = Preferences.userRoot().node("clinic-login");
        String savedUser = pref.get("username", "");
        String savedRole = pref.get("role", "");

        if (!savedUser.isEmpty()) {
            txtUsername.setText(savedUser);
            cmbRole.setSelectedItem(savedRole);
            chkRememberMe.setSelected(true);
        }
    }

    // Call this after successful login
    private void saveRememberMe() {
        Preferences pref = Preferences.userRoot().node("clinic-login");

        if (chkRememberMe.isSelected()) {
            pref.put("username", txtUsername.getText());
            pref.put("role", cmbRole.getSelectedItem().toString());
        } else {
            pref.remove("username");
            pref.remove("role");
        }
    }

    private void setupLoadingSpinner() {
        loadingDialog = new JDialog(this, false);
        loadingDialog.setSize(150, 100);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setUndecorated(true);
        loadingDialog.setLocationRelativeTo(this);

        JLabel lbl = new JLabel("Authenticating...", SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JProgressBar spinner = new JProgressBar();
        spinner.setIndeterminate(true);

        loadingDialog.add(lbl, BorderLayout.NORTH);
        loadingDialog.add(spinner, BorderLayout.CENTER);
    }

    
    // ------------------------------------------------------------
    // UI UPGRADE (Top-notch appearance WITHOUT changing NetBeans layout)
    // ------------------------------------------------------------
    private void styleComponents() {
        setTitle("Clinic Appointment System - Login");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png")));

        txtUsername.setBorder(new LineBorder(new Color(0, 121, 151), 2, true));
        txtPassword.setBorder(new LineBorder(new Color(0, 121, 151), 2, true));
        cmbRole.setBorder(new LineBorder(new Color(0, 121, 151), 2, true));

        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        BtnCreateAccount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Modern button look
        btnLogin.setBackground(new Color(0, 121, 151));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
    }

    // ------------------------------------------------------------
    // PASSWORD SHOW/HIDE FEATURE
    // ------------------------------------------------------------
    private void addPasswordToggle() {
        JButton toggle = new JButton("Show");
        toggle.setBounds(txtPassword.getX() + txtPassword.getWidth() - 60, txtPassword.getY() + 5, 55, 30);
        toggle.setFocusable(false);
        toggle.setContentAreaFilled(false);
        toggle.setBorder(null);
        toggle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        toggle.addActionListener(e -> {
            if (txtPassword.getEchoChar() == '*') {
                txtPassword.setEchoChar((char) 0);
                toggle.setText("Hide");
            } else {
                txtPassword.setEchoChar('*');
                toggle.setText("Show");
            }
        });

        jPanel1.add(toggle);
        jPanel1.repaint();
    }
    
    // ------------------------------------------------------------
    // REALTIME VALIDATION
    // ------------------------------------------------------------
    private void addRealtimeValidation() {
        txtUsername.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validateUsername(); }
        });

        txtPassword.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override public void update() { validatePassword(); }
        });
    }

    private boolean validateUsername() {
        String user = txtUsername.getText().trim();
        if (user.isEmpty()) {
            lblUserError.setForeground(Color.RED);
            lblUserError.setText("Username required");
            return false;
        }
        lblUserError.setText("");
        return true;
    }

    private boolean validatePassword() {
        String pass = String.valueOf(txtPassword.getPassword()).trim();
        if (pass.isEmpty()) {
            lblPassError.setForeground(Color.RED);
            lblPassError.setText("Password required");
            return false;
        }
        lblPassError.setText("");
        return true;
    }

    private boolean validateRole() {
        if (cmbRole.getSelectedIndex() == 0) {
            lblRoleError.setForeground(Color.RED);
            lblRoleError.setText("Select a role");
            return false;
        }
        lblRoleError.setText("");
        return true;
    }

    // ------------------------------------------------------------
    // Document Listener Template
    // ------------------------------------------------------------
    private interface SimpleDocumentListener extends DocumentListener {
        void update();
        @Override default void insertUpdate(DocumentEvent e) { update(); }
        @Override default void removeUpdate(DocumentEvent e) { update(); }
        @Override default void changedUpdate(DocumentEvent e) { update(); }
    }
    
    private void applyFadeInEffect() {
        Timer timer = new Timer(20, null);
        final float[] opacity = {0f};
        this.setOpacity(0f);

        timer.addActionListener(e -> {
            opacity[0] += 0.05f;
            if (opacity[0] >= 1f) {
                opacity[0] = 1f;
                timer.stop();
            }
            setOpacity(opacity[0]);
        });

        timer.start();
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
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        txtUsername = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txtPassword = new javax.swing.JPasswordField();
        cmbRole = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        BtnSignUp = new javax.swing.JLabel();
        BtnCreateAccount = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        btnLogin = new javax.swing.JButton();
        lblStatus = new javax.swing.JLabel();
        lblUserError = new javax.swing.JLabel();
        lblPassError = new javax.swing.JLabel();
        lblRoleError = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(255, 0, 0));
        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/1762982393546.jpg"))); // NOI18N
        jPanel3.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 550, 580));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        jLabel1.setText("LOGIN");

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel2.setText("Username:");

        txtUsername.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Password:");

        txtPassword.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));

        cmbRole.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select Role", "Patient", "Doctor", "Admin", " " }));
        cmbRole.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 121, 151), 2, true));
        cmbRole.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRoleActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Account Type:");

        BtnSignUp.setText("Don't have any account?  ");
        BtnSignUp.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                BtnSignUpMouseClicked(evt);
            }
        });

        BtnCreateAccount.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        BtnCreateAccount.setText("Sign Up");
        BtnCreateAccount.setBorderPainted(false);
        BtnCreateAccount.setContentAreaFilled(false);
        BtnCreateAccount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCreateAccountActionPerformed(evt);
            }
        });

        jLabel7.setText("________________________");

        jLabel8.setText("OR");

        jLabel9.setText("__________________________");

        btnLogin.setBackground(new java.awt.Color(0, 204, 51));
        btnLogin.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLogin.setForeground(new java.awt.Color(255, 255, 255));
        btnLogin.setText("Login");
        btnLogin.setBorderPainted(false);
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });

        lblStatus.setText("jLabel10");

        lblUserError.setText("jLabel10");

        lblPassError.setText("jLabel10");

        lblRoleError.setText("jLabel10");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(489, 489, 489)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(227, 227, 227)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(66, 66, 66)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblUserError)
                            .addComponent(jLabel2)
                            .addComponent(jLabel4)
                            .addComponent(lblPassError)
                            .addComponent(txtPassword, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE)
                            .addComponent(txtUsername)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblRoleError)
                                .addComponent(jLabel5))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(260, 260, 260)
                        .addComponent(lblStatus))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(110, 110, 110)
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel8))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(BtnSignUp)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(6, 6, 6)
                                .addComponent(BtnCreateAccount))
                            .addComponent(jLabel9)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 369, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(45, 45, 45)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(jLabel1)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(90, 90, 90)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(43, 43, 43)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(5, 5, 5)
                .addComponent(lblUserError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(2, 2, 2)
                .addComponent(lblPassError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(cmbRole, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(7, 7, 7)
                .addComponent(lblRoleError)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(5, 5, 5)
                .addComponent(lblStatus)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel9)
                    .addComponent(jLabel8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BtnSignUp)
                    .addComponent(BtnCreateAccount))
                .addGap(54, 54, 54))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
        // TODO add your handling code here:
        lblStatus.setForeground(Color.RED);
        lblStatus.setText("");

        boolean valid =
            validateUsername() &
            validatePassword() &
            validateRole();

        if (!valid) {
            lblStatus.setText("Fix the errors above.");
            return;
        }

        final String username = txtUsername.getText().trim();
        final String password = String.valueOf(txtPassword.getPassword()).trim();
        final String role = cmbRole.getSelectedItem().toString();

        btnLogin.setEnabled(false);
        loadingDialog.setVisible(true);


        SwingWorker<LoginResult, Void> worker = new SwingWorker<>() {

            class LoginResult {
                boolean success;
                int id;
                String user;
                String error;
            }

            @Override
            protected LoginResult doInBackground() {
                LoginResult result = new LoginResult();
                String sql;

                switch (role) {
                    case "Patient" -> sql = "SELECT patientId, username FROM patients WHERE username=? AND password=?";
                    case "Doctor"  -> sql = "SELECT doctorId, username FROM doctors WHERE username=? AND password=?";
                    case "Admin"   -> sql = "SELECT adminId, username FROM admin WHERE username=? AND password=?";
                    default -> {
                        result.error = "Unknown role selected.";
                        return result;
                    }
                }

                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pst = conn.prepareStatement(sql)) {

                    pst.setString(1, username);
                    pst.setString(2, password);

                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            result.success = true;
                            result.id = rs.getInt(1);
                            result.user = rs.getString("username");
                        } else {
                            result.error = "Invalid username or password.";
                        }
                    }

                } catch (Exception e) {
                    result.error = "Database Error: " + e.getMessage();
                }

                return result;
            }

            @Override
            protected void done() {
                try {
                    loadingDialog.setVisible(false);
                    saveRememberMe();

                    LoginResult res = get();

                    if (res.success) {
                        lblStatus.setForeground(new Color(0, 153, 51));
                        lblStatus.setText("Login successful!");
                        JOptionPane.showMessageDialog(logIN.this, "Welcome " + res.user + "!");

                        EventQueue.invokeLater(() -> {
                            switch (role) {
                                case "Patient" -> new patientsDash(res.id, res.user).setVisible(true);
                                case "Doctor"  -> new DoctorDash(res.id, res.user).setVisible(true);
                                case "Admin"   -> new AdminDash(res.id, res.user).setVisible(true);
                            }
                            dispose();
                        });

                    } else {
                        lblStatus.setText(res.error);
                    }

                } catch (Exception e) {
                    lblStatus.setText("Unexpected error: " + e.getMessage());
                } finally {
                    btnLogin.setEnabled(true);
                }
            }
        };

        worker.execute();
    }//GEN-LAST:event_btnLoginActionPerformed

    private void BtnCreateAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCreateAccountActionPerformed
        // TODO add your handling code here:
        new RegisteR().setVisible(true);
        dispose();
    }//GEN-LAST:event_BtnCreateAccountActionPerformed

    private void BtnSignUpMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_BtnSignUpMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnSignUpMouseClicked

    private void cmbRoleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRoleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbRoleActionPerformed

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
            java.util.logging.Logger.getLogger(logIN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(logIN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(logIN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(logIN.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        EventQueue.invokeLater(() -> new logIN().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BtnCreateAccount;
    private javax.swing.JLabel BtnSignUp;
    private javax.swing.JButton btnLogin;
    private javax.swing.JComboBox<String> cmbRole;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel lblPassError;
    private javax.swing.JLabel lblRoleError;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblUserError;
    private javax.swing.JPasswordField txtPassword;
    private javax.swing.JTextField txtUsername;
    // End of variables declaration//GEN-END:variables
}
