/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patientdashboard;

/**
 * Simple in-memory session holder for the running application.
 */
public class Session {
    private static int userId = -1;
    private static String username = null;
    private static String role = null; // "admin", "doctor", "patient"

    public static void start(int id, String user, String r) {
        userId = id; username = user; role = r;
    }
    public static void clear() { userId = -1; username = null; role = null; }
    public static int getUserId() { return userId; }
    public static String getUsername() { return username; }
    public static String getRole() { return role; }
    public static boolean isAdmin() { return "admin".equalsIgnoreCase(role); }
    public static boolean isDoctor() { return "doctor".equalsIgnoreCase(role); }
    public static boolean isPatient() { return "patient".equalsIgnoreCase(role); }
}


