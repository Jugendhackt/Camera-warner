package org.jugendhackt.camera_warner;

import android.os.Parcel;

import com.google.android.gms.maps.model.LatLng;

import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CameraModelTest {

    private Camera camera;

    private int cameraLatitude = 42;
    private int cameraLongitude = 412;

    @Mock
    Parcel writeParcel;

    @Before
    public void setupCamera()
    {
        camera = new Camera(cameraLatitude, cameraLongitude);
    }

    @Test
    public void latitudeIsRight() {
        assertTrue(camera.getLatitude() == cameraLatitude);
    }

    @Test
    public void longitudeIsRight() {
        assertTrue(camera.getLongitude() == cameraLongitude);
    }

    @Test
    public void positionIsRight() {
        assertEquals(camera.getPosition(), new LatLng(cameraLatitude, cameraLongitude));
    }
}