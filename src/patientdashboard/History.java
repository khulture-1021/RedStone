/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package patientdashboard;

import util.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.sql.*;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;           // requires iText library
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import javax.swing.*;
import java.awt.*;
import java.util.regex.Pattern;


/**
 *
 * @author bompe
 */
public class History extends javax.swing.JFrame {

    private int patientId;
    private String username;
    /**
     * Creates new form History
     */
    public History(int patientId, String username) {
        initComponents();
        this.patientId = patientId;
        this.username = username;
        
        loadHistory();
        setupSearch();   // optional: double-click open details
    }
    
    // ================= LOAD HISTORY TABLE ======================
    private void loadHistory() {
        DefaultTableModel model = (DefaultTableModel) tblHistory.getModel();
        model.setRowCount(0);

        String query = "SELECT date, time, doctor, diagnosis, notes, prescription, service_fees FROM history WHERE patient_id = ? ORDER BY date DESC, time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setInt(1, patientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("date"));
                row.add(rs.getString("time"));
                row.add(rs.getString("doctor"));
                row.add(rs.getString("diagnosis"));
                row.add(rs.getString("notes"));
                row.add(rs.getString("prescription"));
                row.add(rs.getString("service_fees"));
                model.addRow(row);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load history:\n" + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TableRowSorter<DefaultTableModel> rowSorter;

    private void setupSearch() {
        DefaultTableModel model = (DefaultTableModel) tblHistory.getModel();
        rowSorter = new TableRowSorter<>(model);
        tblHistory.setRowSorter(rowSorter);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            private void applyFilter() {
                String text = txtSearch.getText().trim();
                if (text.length() == 0) {
                    rowSorter.setRowFilter(null);
                } else {
                    // filter across multiple columns: date, doctor, diagnosis, prescription
                    // build a regex filter (case-insensitive)
                    String regex = "(?i).*" + Pattern.quote(text) + ".*";
                    RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
                        public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                            for (int i : new int[]{0,2,3,5}) { // date, doctor, diagnosis, prescription
                                Object val = entry.getValue(i);
                                if (val != null && val.toString().matches(regex)) return true;
                            }
                            return false;
                        }
                    };
                    rowSorter.setRowFilter(rf);
                }
            }
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
    }

    

    
    
    private void exportToPdfFile(String outputPath) {
    DefaultTableModel model = (DefaultTableModel) tblHistory.getModel();
    Document document = new Document();
    try {
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        document.add(new Paragraph("Patient History Report"));
        document.add(new Paragraph("Patient ID: " + patientId));
        document.add(new Paragraph("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        document.add(new Paragraph(" "));
        for (int i = 0; i < model.getRowCount(); i++) {
            String date = String.valueOf(model.getValueAt(i, 0));
            String time = String.valueOf(model.getValueAt(i, 1));
            String doctor = String.valueOf(model.getValueAt(i, 2));
            String diagnosis = String.valueOf(model.getValueAt(i, 3));
            String notes = String.valueOf(model.getValueAt(i, 4));
            String prescription = String.valueOf(model.getValueAt(i, 5));
            String fees = String.valueOf(model.getValueAt(i, 6));

            document.add(new Paragraph("Date: " + date + "    Time: " + time));
            document.add(new Paragraph("Doctor: " + doctor));
            document.add(new Paragraph("Diagnosis: " + diagnosis));
            document.add(new Paragraph("Notes: " + notes));
            document.add(new Paragraph("Prescription: " + prescription));
            document.add(new Paragraph("Service Fees: " + fees));
            document.add(new Paragraph("------------------------------------------------------------"));
        }
        JOptionPane.showMessageDialog(this, "Exported PDF to: " + outputPath, "Export Success", JOptionPane.INFORMATION_MESSAGE);
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Failed to export PDF:\n" + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
    } finally {
        if (document.isOpen()) document.close();
    }
}

    private void onExportPdfClick() {
    String defaultPath = System.getProperty("user.home") + "/history_report_" + patientId + ".pdf";
    String path = JOptionPane.showInputDialog(this, "Enter output path for PDF:", defaultPath);
    if (path != null && !path.trim().isEmpty()) {
        exportToPdfFile(path.trim());
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
        NavBtnBack2Dash = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        NavBtnBookAppointment = new javax.swing.JButton();
        NavBtnCancelAppointment = new javax.swing.JButton();
        NavBtnEditProfile = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblHistory = new javax.swing.JTable();
        txtSearch = new javax.swing.JTextField();
        btnExportPDF = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(204, 204, 255));

        jPanel2.setBackground(new java.awt.Color(0, 121, 151));

        NavBtnBack2Dash.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        NavBtnBack2Dash.setForeground(new java.awt.Color(255, 255, 255));
        NavBtnBack2Dash.setText("Dashboard");
        NavBtnBack2Dash.setBorder(null);
        NavBtnBack2Dash.setBorderPainted(false);
        NavBtnBack2Dash.setContentAreaFilled(false);
        NavBtnBack2Dash.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NavBtnBack2DashActionPerformed(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Verdana", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/2.png"))); // NOI18N
        jLabel8.setText("Redstone Health Center");

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

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setText("Help");
        jButton7.setBorder(null);
        jButton7.setBorderPainted(false);
        jButton7.setContentAreaFilled(false);
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton7ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel8)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7)
                    .addComponent(NavBtnEditProfile)
                    .addComponent(NavBtnCancelAppointment, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NavBtnBookAppointment)
                    .addComponent(NavBtnBack2Dash))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addGap(50, 50, 50)
                .addComponent(NavBtnBack2Dash)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(NavBtnBookAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnCancelAppointment)
                .addGap(18, 18, 18)
                .addComponent(NavBtnEditProfile)
                .addGap(18, 18, 18)
                .addComponent(jButton7)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel1.setText("Medical History");

        tblHistory.setBackground(new java.awt.Color(0, 153, 153));
        tblHistory.setForeground(new java.awt.Color(255, 255, 255));
        tblHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Time", "Doctor", "Diagnosis", "Notes", "Prescription", "Service Fee"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(tblHistory);

        txtSearch.setText("Search (date/doctor/prescription)");

        btnExportPDF.setText("Export to PDF");
        btnExportPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportPDFActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(84, 84, 84)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(442, 442, 442)
                        .addComponent(btnExportPDF)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 820, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(43, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(63, 63, 63))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jLabel1)
                        .addGap(34, 34, 34))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 485, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnExportPDF)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void NavBtnBack2DashActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnBack2DashActionPerformed
        // TODO add your handling code here:
        new patientsDash(patientId, username).setVisible(true);
        dispose();
    }//GEN-LAST:event_NavBtnBack2DashActionPerformed

    private void NavBtnBookAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnBookAppointmentActionPerformed
        // TODO add your handling code here:
        new BookAppointment(patientId,username).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_NavBtnBookAppointmentActionPerformed

    private void NavBtnCancelAppointmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnCancelAppointmentActionPerformed
        // TODO add your handling code here:
        new Cancel(patientId,username).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_NavBtnCancelAppointmentActionPerformed

    private void NavBtnEditProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NavBtnEditProfileActionPerformed
        // TODO add your handling code here:
        new Edit(patientId,username).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_NavBtnEditProfileActionPerformed

    private void btnExportPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportPDFActionPerformed
        // TODO add your handling code here:
        onExportPdfClick();
    }//GEN-LAST:event_btnExportPDFActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
        new HelpFrame(patientId,username).setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton7ActionPerformed

    /**
     * @param args the command line arguments
     */
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton NavBtnBack2Dash;
    private javax.swing.JButton NavBtnBookAppointment;
    private javax.swing.JButton NavBtnCancelAppointment;
    private javax.swing.JButton NavBtnEditProfile;
    private javax.swing.JButton btnExportPDF;
    private javax.swing.JButton jButton7;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblHistory;
    private javax.swing.JTextField txtSearch;
    // End of variables declaration//GEN-END:variables
}
