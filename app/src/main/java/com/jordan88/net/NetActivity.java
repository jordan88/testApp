package com.jordan88.net;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class NetActivity extends Activity implements SensorEventListener{

    ClothView clothView;
    ClothView.Cloth cloth;

    private SensorManager mSensorManager;
    private Sensor sensor;
    private float[] rotations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net);

        clothView = (ClothView) findViewById(R.id.net_view);
        if(cloth == null) {
            cloth = clothView.cloth;
        }
        else {
            clothView.cloth = cloth;
        }
        rotations = new float[]{0,ClothView.gravity,0};
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onPause() {
        super.onPause();
       // mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

       // mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
       // sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
       // mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        rotations = event.values.clone();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float[] getRotations() {
        return rotations;
    }
}
