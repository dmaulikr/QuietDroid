package jid.quitedroid.Services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.R;
import jid.quitedroid.Receivers.CalendarProcessor;

/**
 * Created by Dawin on 7/21/2016.
 */
public class BackgroundService extends Service implements SensorEventListener {
    // Binder given to clients
    private ModeHandler modeHandler = new ModeHandler();
    private final IBinder mBinder = new LocalBinder();
    String TAG = "BackgroundService.class";

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BackgroundService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, START_STICKY, startId);

        SensorManager sm;
        Sensor sensor;
        sm = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);


        Log.d(TAG, "onStartCommand: Started Service...");

        //initial processing and starts the weekly triggers (ACTION_INITIAL_TRIGGER)
        if(intent != null){

            Intent calendarProcessIntent = new Intent(this, CalendarProcessor.class);
            calendarProcessIntent.setAction(CalendarProcessor.ACTION_INITIAL_TRIGGER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, calendarProcessIntent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager)getApplicationContext().getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);

        }else {

            Log.d(TAG, "onStartCommand: Restarting!");

            Intent calendarProcessIntent = new Intent(this, CalendarProcessor.class);
            calendarProcessIntent.setAction(CalendarProcessor.ACTION_INITIAL_TRIGGER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, calendarProcessIntent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager am = (AlarmManager)getApplicationContext().getSystemService(ALARM_SERVICE);
            am.set(AlarmManager.RTC, System.currentTimeMillis(), pendingIntent);
        }
            Notification notification = new NotificationCompat.Builder(this).
                    setContentTitle("Quite Droid")
                    .setContentText("Running")
                    .setSmallIcon(R.drawable.applicationlogo)
                    .setOngoing(true).build();
        this.startForeground(2, notification);

        return START_STICKY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //NULL
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //Set internal formal mode
        Log.d(TAG, "onSensorChanged: " + sensorEvent.values[0]);
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
        String current_mode = modeHandler.getMode();
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
    public void onDestroy(){
        SensorManager sm;
        sm = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        sm.unregisterListener(this);
    }

}
