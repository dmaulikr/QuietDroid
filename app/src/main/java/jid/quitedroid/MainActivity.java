package jid.quitedroid;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.w3c.dom.Text;


import java.util.Map;

import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.NormalMode;
import jid.quitedroid.Receivers.CalendarProcessor;
import jid.quitedroid.Services.BackgroundService;
import jid.quitedroid.Services.ModeService;

public class MainActivity extends AppCompatActivity {
    //sharedpreference key to get the boolean for quitedroid enabled
    //if false: quitedroid acts as if uninstalled
    //if true: quitedroid automatically schedules intents to set the phone in various modes at times of events

    public static NotificationCompat.Builder mBuilder;
    public static NotificationManager manager;
    public static Context appContext;
    public static PendingIntent pendingIntent;
    boolean flag = false;

    BlockingMode blockingMode;
    MeetingMode meetingMode;
    NormalMode normalMode;

    Text layout;

    final int calendar_permissions = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final String TAG = "MainActivity.java";

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        appContext = getApplicationContext();

//        Intent backgroundIntent = new Intent(getApplicationContext(), BackgroundService.class);
//        startService(backgroundIntent);
//        Intent testIntent = new Intent(getApplicationContext(), BindingActivity.class);
//        startService(testIntent);

        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
            != PackageManager.PERMISSION_GRANTED){

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        calendar_permissions);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

        }

        final AlarmManager am = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        final Switch enable_quitedroid = (Switch)findViewById(R.id.sw_activate_quitedroid);
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean quiteDroid_isEnabled = sharedPreferences.getBoolean(GlobalConstants.QUITEDROID_ENABLED, false);
        enable_quitedroid.setChecked(quiteDroid_isEnabled);

        enable_quitedroid.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(GlobalConstants.QUITEDROID_ENABLED, isChecked);
                editor.commit();

                ContextWrapper cw = new ContextWrapper(getBaseContext());

                if(isChecked){
                    Log.d(TAG, "onCheckedChanged: switch enabled");

                    //start sticky background service
                    Intent backgroundIntent = new Intent(getApplicationContext(), BackgroundService.class);

//                    Register Proximity Sensor


                    cw.startService(backgroundIntent);


                }else{
                    CalendarProcessor.log("Cancel", "the monday triggers");
                    //cancel the next monday trigger, cancel all scheduled alarms from database
                    Intent intent = new Intent(getApplicationContext(), CalendarProcessor.class);
                    intent.setAction(CalendarProcessor.ACTION_WEEKLY_TRIGGER);
                    am.cancel(pendingIntent);
                    CalendarProcessor.cancelAllAlarms(getApplicationContext());
                    Intent backgroundIntent = new Intent(getBaseContext(), BackgroundService.class);
                    cw.stopService(backgroundIntent);
                }
                enable_quitedroid.setChecked(isChecked);

            }
        });


        Button disableMondayTrigger = (Button)findViewById(R.id.btnDisableMondayTrigger);
        disableMondayTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedpreferences = ContextHandler.getContext().getSharedPreferences("ExceptionList", Context.MODE_PRIVATE);

                //Retrieve exception list
                Map<String,?> keys = sharedpreferences.getAll();

                for(Map.Entry<String,?> entry : keys.entrySet()){
                    //Debug output
                    Log.d("map values", entry.getKey() + ": " +
                            entry.getValue().toString());
                }
            }
        });
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ContextHandler.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case calendar_permissions: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Open new activity, the settings page
//            Intent myIntent = new Intent(MainActivity.this, SettingsFeaturesActivity.class);
//            this.startActivity(myIntent);

            this.startActivity(new Intent(MainActivity.this, SettingsFeaturesActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doNothing(){
        //Do nothing
    }
}
