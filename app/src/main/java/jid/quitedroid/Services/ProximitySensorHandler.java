package jid.quitedroid.Services;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

import jid.quitedroid.ContextHandler;

/**
 * Created by JiYeon on 2016-04-16.
 */
public class ProximitySensorHandler implements SensorEventListener{
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        ContextHandler contextHandler = new ContextHandler();
        Toast.makeText(contextHandler.getContext(), sensorEvent.values[0] + "I am testing :)", Toast.LENGTH_SHORT).show();
    }
}
