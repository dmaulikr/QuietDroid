package jid.quitedroid.Services;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.*;

import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.Mode;
import jid.quitedroid.Modes.NormalMode;

/**
 * Created by Dawin on 3/17/2016.
 */
public class ModeService extends Service implements SensorEventListener {

    /**
     * indicates how to behave if the service is killed
     */
    private static final String TAG = ModeService.class.getSimpleName();

    private ModeHandler modeHandler = new ModeHandler();
    private String current_mode;

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //NULL
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Set internal formal mode
        Log.d(TAG, "onSensorChanged: " + sensorEvent.timestamp);
        setInternalFormalMode(sensorEvent);
    }

    //Force initial sensor event
    public void forceSensorValue(){
        SensorEvent sensorEvent;
        //Set internal formal mode
//        setInternalFormalMode(sensorEvent);
    }

    private void setInternalFormalMode(SensorEvent sensorEvent){
        //Receive current mode
        current_mode = modeHandler.getMode();
        if(current_mode.equals(MeetingMode.name)){
            //if the phone is placed in the bag or in pocket, distinguish the formal mode into a "formal situation"
            if(sensorEvent.values[0] < 1){
                modeHandler.setInternalFormalMode(Boolean.TRUE);
                //if the phone is not placed in the bag or pocket, distinguish the formal mode into an "informal situation"
            }else{
                modeHandler.setInternalFormalMode(Boolean.FALSE);
            }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String modeName = intent.getStringExtra("mode");
            Mode mode = null;

            switch (modeName) {

                case BlockingMode.name:
                    mode = new BlockingMode();
                    break;
                case MeetingMode.name:
                    mode = new MeetingMode();
                    break;
                case NormalMode.name:
                    mode = new NormalMode();
                    break;
                default:
                    //should not reach this code
                    //do nothing
                    Log.d(TAG, "onReceive: " + "modeName is default, something is wrong!!! Name: " + modeName);
            }

            if (mode != null) {
                mode.setSoundType();
                mode.showNotification();
            }

            Log.d(TAG, "onStartCommand: Background Mode Service started... " + modeName);

//        SensorManager sm = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
//        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }
        return START_REDELIVER_INTENT;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
        Log.d(TAG, "onDestroy: Background Mode Service Destroyed...");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
