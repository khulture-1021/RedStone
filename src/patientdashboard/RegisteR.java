/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Random;

/**
 *
 * @author bompe
 */
public class RegisteR extends javax.swing.JFrame {
    
    // Additional runtime UI & state fields
    private JPasswordField pwdPassword;           // runtime replacement for txtPassword
    private JPasswordField pwdConfirmPassword;    // runtime replacement for txtConfirmPassword
    private JLabel lblPasswordStrength;
    private JDialog loadingDialog;
    
    /**
     * Creates new form RegisteR
     */
    public RegisteR() {
        initComponents();
        postInitComponents();           // our added setup (keeps generated block unchanged)

        addRealtimeValidation();
        setupDOBListener();
        applyFadeInEffect();
    }
    
    // Post init: swap password fields, add toggles, setup spinner, etc.
    private void postInitComponents() {
        swapTextFieldWithPasswordField();

        lblPasswordStrength = new JLabel(" ");
        lblPasswordStrength.setFont(lblPasswordStrength.getFont().deriveFont(Font.PLAIN, 12f));
        lblPasswordStrength.setForeground(Color.DARK_GRAY);
        jPanel1.add(lblPasswordStrength);
        lblPasswordStrength.setBounds(txtPassword.getX(), txtPassword.getY() + txtPassword.getHeight() + 4, 300, 18);

        setupLoadingSpinner();
        addPasswordShowHideToggles();

        txtPhoneNumber.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                txtPhoneNumber.setText(formatPhoneNumber(txtPhoneNumber.getText()));
            }
        });

        txtIDNumber.getDocument().addDocumentListener(new SimpleDocListener(() -> {
            autoDetectGenderFromID();
            validateID();
        }));

        if (pwdPassword != null) {
            pwdPassword.getDocument().addDocumentListener(new SimpleDocListener(() -> {
                String pwd = new String(pwdPassword.getPassword());
                lblPasswordStrength.setText("Strength: " + passwordStrengthLabel(pwd));
            }));
        }

        applyModernBorders();

        // Clear/correct label defaults
        lblFirstNameError.setText("");
        lblLastNameError.setText("");
        lblEmailError.setText("");
        lblDOBError.setText("");
        lblPhoneError.setText("");
        lblIDError.setText("");
        lblUsernameError.setText("");
        lblPasswordError.setText("");
        lblConfirmError.setText("");
        lblStatus.setText("");
    }

    // Replace generated JTextField password fields with JPasswordField instances at runtime
    private void swapTextFieldWithPasswordField() {
        try {
            pwdPassword = new JPasswordField();
            pwdConfirmPassword = new JPasswordField();

            copyTextFieldProperties(txtPassword, pwdPassword);
            copyTextFieldProperties(txtConfirmPassword, pwdConfirmPassword);

            Container parent1 = txtPassword.getParent();
            if (parent1 != null) {
                parent1.remove(txtPassword);
                parent1.add(pwdPassword);
            }

            Container parent2 = txtConfirmPassword.getParent();
            if (parent2 != null) {
                parent2.remove(txtConfirmPassword);
                parent2.add(pwdConfirmPassword);
            }

            revalidate();
            repaint();

            // Keep original txtPassword synced if any generated code reads it
            pwdPassword.getDocument().addDocumentListener(new SimpleDocListener(() ->
                    txtPassword.setText(new String(pwdPassword.getPassword()))
            ));
            pwdConfirmPassword.getDocument().addDocumentListener(new SimpleDocListener(() ->
                    txtConfirmPassword.setText(new String(pwdConfirmPassword.getPassword()))
            ));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void copyTextFieldProperties(JTextField src, JPasswordField dest) {
        dest.setColumns(src.getColumns());
        dest.setFont(src.getFont());
        dest.setBorder(src.getBorder());
        dest.setBounds(src.getBounds());
        dest.setPreferredSize(src.getPreferredSize());
    }

    private int findComponentIndex(Container parent, Component comp) {
        Component[] children = parent.getComponents();
        for (int i = 0; i < children.length; i++) if (children[i] == comp) return i;
        return -1;
    }

    // Add show/hide toggles positioned relative to the original fields
    private void addPasswordShowHideToggles() {
        JButton toggle1 = new JButton("Show");
        toggle1.setFocusable(false);
        toggle1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle1.addActionListener(e -> {
            if (pwdPassword == null) return;
            if (pwdPassword.getEchoChar() == '\0') {
                pwdPassword.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));
                toggle1.setText("Show");
            } else {
                pwdPassword.setEchoChar((char) 0);
                toggle1.setText("Hide");
            }
        });
        jPanel1.add(toggle1);

        JButton toggle2 = new JButton("Show");
        toggle2.setFocusable(false);
        toggle2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        toggle2.addActionListener(e -> {
            if (pwdConfirmPassword == null) return;
            if (pwdConfirmPassword.getEchoChar() == '\0') {
                pwdConfirmPassword.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));
                toggle2.setText("Show");
            } else {
                pwdConfirmPassword.setEchoChar((char) 0);
                toggle2.setText("Hide");
            }
        });
        jPanel1.add(toggle2);

        int buttonWidth = 60;
        int buttonHeight = 30;

        SwingUtilities.invokeLater(() -> {
            try {
                int px1 = txtPassword.getX() + txtPassword.getWidth() - buttonWidth - 8;
                int py1 = txtPassword.getY() + (txtPassword.getHeight() - buttonHeight) / 2;
                toggle1.setBounds(px1, py1, buttonWidth, buttonHeight);

                int px2 = txtConfirmPassword.getX() + txtConfirmPassword.getWidth() - buttonWidth - 8;
                int py2 = txtConfirmPassword.getY() + (txtConfirmPassword.getHeight() - buttonHeight) / 2;
                toggle2.setBounds(px2, py2, buttonWidth, buttonHeight);
            } catch (Exception ex) {
                // fallback approximate positions
                toggle1.setBounds(350, txtPassword.getY() + 5, buttonWidth, buttonHeight);
                toggle2.setBounds(350, txtConfirmPassword.getY() + 5, buttonWidth, buttonHeight);
            }
            jPanel1.repaint();
        });
    }

    // Loading spinner dialog
    private void setupLoadingSpinner() {
        loadingDialog = new JDialog(this, "Please wait...", true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(220, 100);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setLocationRelativeTo(this);

        JLabel lbl = new JLabel("Processing...", SwingConstants.CENTER);
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 4, 8));
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        loadingDialog.add(lbl, BorderLayout.NORTH);
        loadingDialog.add(bar, BorderLayout.CENTER);
    }

    // Fade-in animation
    private void applyFadeInEffect() {
        try {
            setOpacity(0f);
            Timer t = new Timer(25, null);
            final float[] op = {0f};
            t.addActionListener(e -> {
                op[0] += 0.07f;
                if (op[0] >= 1f) {
                    op[0] = 1f;
                    setOpacity(1f);
                    ((Timer)e.getSource()).stop();
                } else {
                    setOpacity(Math.min(1f, op[0]));
                }
            });
            t.start();
        } catch (Exception ignored) { }
    }

    // DOB listener & age calculation
    private void setupDOBListener() {
        dateChooserDOB.addPropertyChangeListener("date", evt -> {
            int age = calculateAgeFromDOB();
            lblDOBError.setText(age >= 0 ? ("Age: " + age) : "");
        });
    }

    private int calculateAgeFromDOB() {
        if (dateChooserDOB.getDate() == null) return -1;
        java.util.Date dob = dateChooserDOB.getDate();
        LocalDate birth = dob.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int age = Period.between(birth, LocalDate.now()).getYears();
        if (age < 0 || age > 120) return -1;
        return age;
    }

    // Realtime validation wiring
    private void addRealtimeValidation() {
        txtUsername.getDocument().addDocumentListener(new SimpleDocListener(this::validateUsername));
        txtPassword.getDocument().addDocumentListener(new SimpleDocListener(this::validatePassword));
        txtConfirmPassword.getDocument().addDocumentListener(new SimpleDocListener(this::validateConfirmPassword));
        txtEmail.getDocument().addDocumentListener(new SimpleDocListener(this::validateEmail));
        txtPhoneNumber.getDocument().addDocumentListener(new SimpleDocListener(this::validatePhone));
        txtIDNumber.getDocument().addDocumentListener(new SimpleDocListener(this::validateID));
    }

    // Validation methods (strict email)
    private boolean validateUsername() {
        String u = txtUsername.getText().trim();
        if (u.isEmpty()) { lblUsernameError.setText("Username required"); setFieldErrorVisual(txtUsername, true); return false; }
        lblUsernameError.setText(""); setFieldErrorVisual(txtUsername, false); return true;
    }

    private boolean validatePassword() {
        String pass = pwdPassword != null ? new String(pwdPassword.getPassword()) : txtPassword.getText();
        if (pass.trim().length() < 6) { lblPasswordError.setText("Min 6 characters"); setFieldErrorVisual(getPasswordFieldVisual(), true); return false; }
        lblPasswordError.setText(""); setFieldErrorVisual(getPasswordFieldVisual(), false); return true;
    }

    private boolean validateConfirmPassword() {
        String pass = pwdPassword != null ? new String(pwdPassword.getPassword()) : txtPassword.getText();
        String conf = pwdConfirmPassword != null ? new String(pwdConfirmPassword.getPassword()) : txtConfirmPassword.getText();
        if (!pass.equals(conf)) { lblConfirmError.setText("Passwords do not match"); setFieldErrorVisual(getConfirmPasswordFieldVisual(), true); return false; }
        lblConfirmError.setText(""); setFieldErrorVisual(getConfirmPasswordFieldVisual(), false); return true;
    }

    // Strict email validation (choice 3)
    private boolean validateEmail() {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) { lblEmailError.setText(""); setFieldErrorVisual(txtEmail, false); return true; } // optional but validate if present
        String regex = "^(?![.-])[A-Za-z0-9._%+-]{2,64}@(?!-)[A-Za-z0-9.-]+\\.[A-Za-z]{2,10}$";
        if (!email.matches(regex)) {
            lblEmailError.setText("Invalid email");
            setFieldErrorVisual(txtEmail, true);
            return false;
        }
        lblEmailError.setText("");
        setFieldErrorVisual(txtEmail, false);
        return true;
    }

    private boolean validatePhone() {
        String ph = txtPhoneNumber.getText().trim();
        if (!ph.matches("\\d{9,13}|(\\d{3}[- ]?\\d{3}[- ]?\\d{3,4})")) { lblPhoneError.setText("Invalid phone"); setFieldErrorVisual(txtPhoneNumber, true); return false; }
        lblPhoneError.setText(""); setFieldErrorVisual(txtPhoneNumber, false); return true;
    }

    private boolean validateID() {
        String id = txtIDNumber.getText().trim();
        if (id.length() < 6) { lblIDError.setText("Invalid ID"); setFieldErrorVisual(txtIDNumber, true); return false; }
        lblIDError.setText(""); setFieldErrorVisual(txtIDNumber, false); return true;
    }

    private void setFieldErrorVisual(Component comp, boolean error) {
        if (!(comp instanceof JComponent)) return;
        JComponent jc = (JComponent) comp;
        if (error) jc.setBorder(new LineBorder(Color.RED, 2));
        else jc.setBorder(new LineBorder(new Color(0, 121, 151), 2, true));
    }

    private Component getPasswordFieldVisual() { return pwdPassword != null ? pwdPassword : txtPassword; }
    private Component getConfirmPasswordFieldVisual() { return pwdConfirmPassword != null ? pwdConfirmPassword : txtConfirmPassword; }

    // Password strength
    private String passwordStrengthLabel(String pwd) {
        if (pwd == null || pwd.isEmpty()) return "Empty";
        int score = 0;
        if (pwd.length() >= 8) score++;
        if (pwd.matches("(?=.*[0-9]).*")) score++;
        if (pwd.matches("(?=.*[A-Z]).*")) score++;
        if (pwd.matches("(?=.*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).*")) score++;
        switch (score) {
            case 0: case 1: return "Weak";
            case 2: return "Fair";
            case 3: return "Good";
            default: return "Strong";
        }
    }

    // Phone formatting helper
    private String formatPhoneNumber(String raw) {
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() <= 3) return digits;
        if (digits.length() <= 6) return digits.substring(0, 3) + "-" + digits.substring(3);
        if (digits.length() <= 10) return digits.substring(0, 3) + "-" + digits.substring(3, 6) + "-" + digits.substring(6);
        return digits;
    }

    // Auto detect gender from SA ID number
    private void autoDetectGenderFromID() {
        String id = txtIDNumber.getText().trim();
        if (id.length() >= 7 && id.matches("\\d+")) {
            char c = id.charAt(6);
            int digit = Character.getNumericValue(c);
            if (digit >= 5) cmbGender.setSelectedItem("Male");
            else cmbGender.setSelectedItem("Female");
        }
    }

    // Duplicate checks
    private boolean isDuplicate(String column, String value) {
        String sql = "SELECT 1 FROM patients WHERE " + column + " = ? LIMIT 1";
        try (Connection conn = getConnectionSafe();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, value);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    // Register (called by generated button)
    private void registerUser() {
        boolean ok = validateUsername() & validatePassword() & validateConfirmPassword()
                & validateEmail() & validatePhone() & validateID();
        if (!ok) { lblStatus.setText("Fix errors before submitting"); return; }

        if (dateChooserDOB.getDate() == null) { lblStatus.setText("Select a valid Date of Birth"); return; }

        int age = calculateAgeFromDOB();
        if (age < 0) { lblStatus.setText("Invalid Date of Birth"); return; }

        final String username = txtUsername.getText().trim();
        final String email = txtEmail.getText().trim();
        final String idNumber = txtIDNumber.getText().trim();
        final String genderSelected = (cmbGender.getSelectedItem() == null) ? "MALE" : cmbGender.getSelectedItem().toString().toUpperCase();
        final String genderForDb = genderSelected.equals("MALE") ? "MALE" : "FEMALE";
        final int finalAge = age;
        final String phone = txtPhoneNumber.getText().trim();
        final String password = pwdPassword != null ? new String(pwdPassword.getPassword()) : txtPassword.getText();

        // Duplicate checks
        if (isDuplicate("username", username)) { lblStatus.setText("Username already exists."); return; }
        if (!email.isEmpty() && isDuplicate("email", email)) { lblStatus.setText("Email already registered."); return; }
        if (!idNumber.isEmpty() && isDuplicate("idNumber", idNumber)) { lblStatus.setText("ID/Passport already registered."); return; }

        // Insert in DB in background thread
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                SwingUtilities.invokeLater(() -> loadingDialog.setVisible(true));
                int newId = -1;
                String sql = "INSERT INTO patients (username, password, firstName, lastName, gender, age, email, phone, dateOfBirth, idNumber) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (Connection conn = getConnectionSafe();
                     PreparedStatement pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    pst.setString(1, username);
                    pst.setString(2, password); // plaintext as requested
                    pst.setString(3, txtFirstName.getText().trim());
                    pst.setString(4, txtLastName.getText().trim());
                    pst.setString(5, genderForDb);
                    pst.setInt(6, finalAge);
                    pst.setString(7, email);
                    pst.setString(8, phone);
                    java.util.Date dob = dateChooserDOB.getDate();
                    pst.setDate(9, new java.sql.Date(dob.getTime()));
                    pst.setString(10, idNumber);

                    pst.executeUpdate();
                    try (ResultSet rs = pst.getGeneratedKeys()) {
                        if (rs.next()) newId = rs.getInt(1);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    return -2;
                } finally {
                    SwingUtilities.invokeLater(() -> loadingDialog.setVisible(false));
                }
                return newId;
            }

            @Override
            protected void done() {
                try {
                    int res = get();
                    if (res == -2) {
                        lblStatus.setText("Database error during registration.");
                    } else if (res > 0) {
                        lblStatus.setText("Registered Successfully!");
                        JOptionPane.showMessageDialog(RegisteR.this, "Registration successful. Welcome " + txtFirstName.getText() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        try {
                            patientsDash dash = new patientsDash(res, txtUsername.getText().trim());
                            dash.setLocationRelativeTo(null);
                            dash.setVisible(true);
                            dispose();
                        } catch (Exception ex) {
                            new patientsDash().setVisible(true);
                            dispose();
                        }
                    } else {
                        lblStatus.setText("Unknown error occurred.");
                    }
                } catch (Exception ex) {
                    lblStatus.setText("Unexpected error: " + ex.getMessage());
                }
            }
        };

        worker.execute();
    }

    // DB connection helper
    private Connection getConnectionSafe() throws SQLException {
        try {
            return DatabaseConnection.getConnection();
        } catch (Throwable t) {
            String url = "jdbc:mysql://localhost:3306/redstone";
            String user = "root";
            String pass = "";
            return DriverManager.getConnection(url, user, pass);
        }
    }

    // Apply nice borders
    private void applyModernBorders() {
        LineBorder b = new LineBorder(new Color(0, 121, 151), 2, true);
        txtFirstName.setBorder(b);
        txtLastName.setBorder(b);
        txtEmail.setBorder(b);
        txtPhoneNumber.setBorder(b);
        txtIDNumber.setBorder(b);
        txtUsername.setBorder(b);
        txtPassword.setBorder(b);
        txtConfirmPassword.setBorder(b);
    }

    // Document listener template
    private interface SimpleDocListener extends DocumentListener {
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

        cmbGender.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Select gender", "Male", "Female" }));
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
