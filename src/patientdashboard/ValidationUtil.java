/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patientdashboard;

import java.util.regex.Pattern;

public class ValidationUtil {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\d{7,15}$");
    private static final Pattern IDNUM = Pattern.compile("^\\d{6,20}$");

    public static boolean isEmail(String s) {
        return s != null && EMAIL.matcher(s).matches();
    }
    public static boolean isPhone(String s) {
        return s != null && PHONE.matcher(s).matches();
    }
    public static boolean isIdNumber(String s) {
        return s != null && IDNUM.matcher(s).matches();
    }
    public static boolean isNotEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}


