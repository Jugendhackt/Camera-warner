package org.jugendhackt.camera_warner;

/**
 * Created by Julian Mundhahs on 16.06.2017.
 */

public interface ServiceCallbacks {
    /**
     * Indicates to the listener that the backupLog has changed
     */
    void newData();
    void positionUpdate();
}
