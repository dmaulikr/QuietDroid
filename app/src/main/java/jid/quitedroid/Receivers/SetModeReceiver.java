package jid.quitedroid.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.Mode;
import jid.quitedroid.Modes.NormalMode;
import jid.quitedroid.Services.BackgroundService;
import jid.quitedroid.Services.ModeService;

/**
 * Created by Dawin on 7/17/2016.
 */
public class SetModeReceiver extends BroadcastReceiver {
    BackgroundService mService;

    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = "SetModeReceiver";
        Log.d(TAG, "onReceive: " + intent.getDataString());
        try{
            if (intent != null) {
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
                        return;
                }

                mode.setSoundType();
                mode.showNotification();
            }
        }catch(Exception e){
            Log.d(TAG, "onReceive: " + e.getMessage());
        }
    }
}
