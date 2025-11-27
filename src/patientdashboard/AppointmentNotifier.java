/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package patientdashboard;

import javax.swing.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight event bus for appointment changes.
 *
 * - Frames that display appointment data (e.g., History) can register a listener.
 * - Frames that modify appointment data (e.g., BookAppointment, Cancel) should notify
 *   the AppointmentNotifier so listeners can refresh their views.
 *
 * This is intentionally minimal and kept in the patientdashboard package for ease-of-use.
 */

/**
 *
 * @author tshiy
 */
public final class AppointmentNotifier {
    public interface Listener {
        /**
         * Called when appointments for the given patientId may have changed.
         * Implementations should refresh data as needed.
         *
         * @param patientId the patient whose appointments changed
         */
        void onAppointmentsChanged(int patientId);
    }

    private static final AppointmentNotifier INSTANCE = new AppointmentNotifier();

    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    private AppointmentNotifier() {}

    public static AppointmentNotifier getInstance() {
        return INSTANCE;
    }

    public void addListener(Listener l) {
        if (l != null) listeners.addIfAbsent(l);
    }

    public void removeListener(Listener l) {
        if (l != null) listeners.remove(l);
    }

    /**
     * Notify listeners that appointments for a patient have changed.
     * Listener callbacks are invoked on the EDT (safe for updating Swing components).
     *
     * @param patientId the patient id whose appointments changed
     */
    public void notifyChange(int patientId) {
        for (Listener l : listeners) {
            SwingUtilities.invokeLater(() -> {
                try {
                    l.onAppointmentsChanged(patientId);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}