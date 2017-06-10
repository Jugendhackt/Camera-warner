package org.jugendhackt.camera_warner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void openMap(View view) {
        Intent mapsActivity = new Intent(this, MapsActivity.class);
        startActivity(mapsActivity);
    }

    public void openSettings(View view) {
        Intent mapsActivity = new Intent(this, EinstellungenActivity.class);
        startActivity(mapsActivity);
    }
}
