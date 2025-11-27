/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 *
 * @author bompe
 */
public class Cancel extends javax.swing.JFrame {

    private int patientId;
    private String username;
    /**
     * Creates new form Cancel
     */
    public Cancel(int patientId, String username) {
        initComponents();
        
        this.patientId = patientId;
        this.username = username;
        loadAppointments();
        applyStatusColorRenderer();
        setupButtons();
        
        tblAppointments.getColumnModel().getColumn(0).setMinWidth(0);
        tblAppointments.getColumnModel().getColumn(0).setMaxWidth(0);
    }
    
    private void setupButtons() {
        btnCancel.addActionListener(e -> cancelAppointment());

        btnDashboard.addActionListener(e -> {
            patientsDash dash = new patientsDash(patientId, username);
            dash.setVisible(true);
            this.dispose();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                patientsDash dash = new patientsDash(patientId, username);
                dash.setVisible(true);
            }
        });
    }
    
    private void applyStatusColorRenderer() {
        tblAppointments.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {

            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                java.awt.Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                String status = table.getValueAt(row, 3).toString().toUpperCase();

                // default colors
                c.setForeground(java.awt.Color.BLACK);
                c.setBackground(java.awt.Color.WHITE);

                switch (status) {
                    case "SCHEDULED" -> c.setBackground(new java.awt.Color(200, 255, 200)); // green
                    case "CANCELLED" -> c.setBackground(new java.awt.Color(255, 200, 200)); // red
                    case "PAST" -> c.setBackground(new java.awt.Color(230, 230, 230)); // gray
                }

                if (isSelected) {
                    c.setBackground(new java.awt.Color(100, 150, 255)); // highlight on select
                }

                return c;
            }
        });
    }


    /** -----------------------------
     * LOAD ALL APPOINTMENTS
     * ----------------------------- */
    private void loadAppointments() {
        DefaultTableModel model = (DefaultTableModel) tblAppointments.getModel();
        model.setRowCount(0);

        try (Connection conn = DatabaseConnection.getConnection()) {

            String filter = cmbFilter.getSelectedItem().toString();
            String sql = """
                SELECT a.appointmentId, a.appointmentDate, a.reason, 
                       d.fullName AS doctorName, a.status 
                FROM appointments a
                JOIN doctors d ON a.doctorId = d.doctorId
                WHERE a.patientId = ?
                ORDER BY a.appointmentDate DESC
            """;

            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, patientId);
            ResultSet rs = pst.executeQuery();

            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

            while (rs.next()) {
                String status = rs.getString("status");
                Date appDate = rs.getDate("appointmentDate");

                // Determine Past / Upcoming automatically if not cancelled
                String computedStatus = status;
                if (!status.equalsIgnoreCase("Cancelled")) {
                    if (appDate.before(today)) {
                        computedStatus = "Past";
                    } else {
                        computedStatus = "Scheduled";
                    }
                }

                // FILTERING LOGIC
                boolean addRow = switch (filter) {
                    case "All" -> true;
                    case "Upcoming" -> computedStatus.equals("Scheduled");
                    case "Past" -> computedStatus.equals("Past");
                    case "Cancelled" -> computedStatus.equals("Cancelled");
                    default -> true;
                };

                if (addRow) {
                    model.addRow(new Object[]{
                            rs.getDate("appointmentDate"),
                            rs.getString("reason"),
                            rs.getString("doctorName"),
                            computedStatus
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading appointments: " + e.getMessage());
        }
    }


    /** -----------------------------
     * CANCEL APPOINTMENT
     * ----------------------------- */
    private void cancelAppointment() {
        int row = tblAppointments.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to cancel.");
            return;
        }

        int appointmentId = (int) tblAppointments.getValueAt(row, 0);
        String status = tblAppointments.getValueAt(row, 4).toString();

        if (status.equalsIgnoreCase("CANCELLED")) {
            JOptionPane.showMessageDialog(this,
                    "This appointment is already cancelled.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this appointment?",
                "Confirm Cancel",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "UPDATE appointments SET status='CANCELLED' WHERE appointmentId=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setInt(1, appointmentId);
            pst.executeUpdate();

            JOptionPane.showMessageDialog(this, "Appointment cancelled.");
            loadAppointments(); // refresh table

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error cancelling appointment: " + ex.getMessage());
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

        NavBtnMedicalHistory = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        btnDashboard = new javax.swing.JButton();
        NavBtnBookAppointment = new javax.swing.JButton();
        NavBtnEditProfile = new javax.swing.JButton();
        NavBtnMedicalHistory1 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        btnCancel = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblAppointments = new javax.swing.JTable();
        cmbFilter = new javax.swing.JComboBox<>();

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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(0, 121, 151));

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/2.png"))); // NOI18N
        jLabel8.setText("Redstone Health Center");

        btnDashboard.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDashboard.setForeground(new java.awt.Color(255, 255, 255));
        btnDashboard.setText("Dash board");
        btnDashboard.setBorder(null);
        btnDashboard.setBorderPainted(false);
        btnDashboard.setContentAreaFilled(false);
        btnDashboard.setDefaultCapable(false);
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

        NavBtnMedicalHistory1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnMedicalHistory1.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnMedicalHistory1.setText("Medical History");
        NavBtnMedicalHistory1.setBorder(null);
        NavBtnMedicalHistory1.setBorderPainted(false);
        NavBtnMedicalHistory1.setContentAreaFilled(false);
        NavBtnMedicalHistory1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnMedicalHistory1ActionPerformed(evt);
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
                .addComponent(jLabel8)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(65, 65, 65)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7)
                    .addComponent(NavBtnMedicalHistory1)
                    .addComponent(NavBtnEditProfile)
                    .addComponent(btnDashboard)
                    .addComponent(NavBtnBookAppointment))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(80, 80, 80)
                .addComponent(btnDashboard)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(NavBtnBookAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnEditProfile)
                .addGap(18, 18, 18)
                .addComponent(NavBtnMedicalHistory1)
                .addGap(18, 18, 18)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Cancel Appointments");

        btnCancel.setBackground(new java.awt.Color(0, 153, 0));
        btnCancel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel");
        btnCancel.setBorder(null);
        btnCancel.setBorderPainted(false);
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        tblAppointments.setBackground(new java.awt.Color(0, 102, 102));
        tblAppointments.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        tblAppointments.setForeground(new java.awt.Color(255, 255, 255));
        tblAppointments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Appointment ID", "Appointment Date", "Doctor", "Reason", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(tblAppointments);

        cmbFilter.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Upcoming", "Past", "Cancelled" }));
        cmbFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterActionPerformed(evt);
            }
        });

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
                                .addGap(27, 27, 27)
                                .addComponent(jLabel6))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(229, 229, 229)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 49, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 754, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(30, 30, 30))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(jLabel6)
                .addGap(19, 19, 19)
                .addComponent(cmbFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(146, Short.MAX_VALUE))
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
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnDashboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDashboardActionPerformed
        // TODO add your handling code here:
        new patientsDash(patientId, username).setVisible(true);
        dispose();
    }//GEN-LAST:event_btnDashboardActionPerformed

    private void NavBtnBookAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnBookAppointmentActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnBookAppointmentActionPerformed

    private void NavBtnEditProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnEditProfileActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnEditProfileActionPerformed

    private void NavBtnMedicalHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnMedicalHistoryActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnMedicalHistoryActionPerformed

    private void NavBtnMedicalHistory1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnMedicalHistory1ActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_NavBtnMedicalHistory1ActionPerformed

    private void cmbFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterActionPerformed
        // TODO add your handling code here:
        loadAppointments();
    }//GEN-LAST:event_cmbFilterActionPerformed

    /**
     * @param args the command line arguments
     */
   

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton NavBtnBookAppointment;
    private javax.swing.JButton NavBtnEditProfile;
    private javax.swing.JButton NavBtnMedicalHistory;
    private javax.swing.JButton NavBtnMedicalHistory1;
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnDashboard;
    private javax.swing.JComboBox<String> cmbFilter;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable tblAppointments;
    // End of variables declaration//GEN-END:variables
}
