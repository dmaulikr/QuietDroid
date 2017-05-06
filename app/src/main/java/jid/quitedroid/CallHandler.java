package jid.quitedroid;

//From the ITelephony.aidl file
import com.android.internal.telephony.ITelephony;
import java.lang.reflect.Method;
import java.util.Map;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import jid.quitedroid.Features.PriorityCallHandler;
import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.Mode;
import jid.quitedroid.Services.BackgroundService;
import jid.quitedroid.Services.ModeHandler;

/**
 * Created by JiYeon on 2016-03-20.
 */

public class CallHandler extends BroadcastReceiver{
    private static String TAG="PhoneStateReceiver";
    private static final String MyPREFERENCES = "ExceptionList" ;
    private PriorityCallHandler priorityCallHandler = new PriorityCallHandler(); //Priority Call Handler
    private SoundHandler soundHandler = new SoundHandler(); //Sound Handler
    private messageHandler  msgHandler = new messageHandler(); //Message Handler
    private ModeHandler modeHandler = new ModeHandler(); //Mode Handler

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String state = bundle.getString(TelephonyManager.EXTRA_STATE);

        Log.d("Intent Detected", "Intent Detected");

        //If the OnReceive activity was not called by the incoming call, return. No need to continue.
        if (!TelephonyManager.EXTRA_STATE_RINGING.equalsIgnoreCase(state)) {
            //Reset sound types
            resetSound();
            return;
        }

        //When the state of the phone changes, deny all incoming calls
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        try {
            Class c = Class.forName(tm.getClass().getName());
            Method m = c.getDeclaredMethod("getITelephony");
            m.setAccessible(true);
            ITelephony telephonyService = (ITelephony) m.invoke(tm);
            String phoneNumber = bundle.getString("incoming_number");

            if (blockCall(context, phoneNumber)) {
                Log.d("Entered","BLOCKED CALL BLOCKED CALL");

                //Record caller
                priorityCallHandler.recordCaller(phoneNumber);

//                telephonyService.silenceRinger();  //Silence ringer to me modified
                telephonyService.endCall();
                Toast.makeText(context, phoneNumber , Toast.LENGTH_LONG).show();

                //Generate rejection message according to mode
                msgHandler.sendSMSMessage(phoneNumber, modeHandler.getMode());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Determine if the incoming call should be passed through or not
    private boolean blockCall(Context context, String phoneNumber){
        String current_mode = modeHandler.getMode();
        Boolean isFormalSituation = modeHandler.getInternalFormalMode();

        //If current mode is Blocking Mode, return false
        if (current_mode.equals(BlockingMode.name)) {
            return true;
        }

        //If current mode is formal mode
        if(current_mode.equals(MeetingMode.name)){
            if(isFormalSituation){
                if(isCallerInExceptionList(context, phoneNumber)){
                    //If caller is in exception list, make the phone vibrate and pass through call
                    soundHandler.setSoundMode("vibrate");
                    return false;
                }else{
                    //If the caller is not listed on the exception list, deny call
                    return true;
                }
            }else{
                if(isCallerInExceptionList(context, phoneNumber)){
                    //If the caller is in the exception list in an informal situation, make the phone emit sounds
                    soundHandler.setSoundMode("normal");
                }
                //Pass through all calls in informal situations
                return false;
            }
        }

        //All other cases, pass through call
        return false;
    }

    //Check if the caller is listed on the exception list
    //If caller is in the excpetion list, return true. Otherwise return false
    private boolean isCallerInExceptionList(Context context, String phoneNumber){
        //Retrieve exception list from shared preferences
        SharedPreferences sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        Map<String,?> keys = sharedpreferences.getAll();

        //Iterate through exception list
        for(Map.Entry<String,?> entry : keys.entrySet()){
            //If caller's phone number matches, return TRUE
            if(entry.getValue().toString().trim().equals(phoneNumber.trim())){
                return true;
            }
        }
        //Caller is not listed in the exception list, return false
        return false;
    }

    //This method is directly linked with formal mode
    //Reset the sound to its original type
    private void resetSound(){
        String current_mode = modeHandler.getMode();
        Boolean isFormalSituation = modeHandler.getInternalFormalMode();

        if(current_mode.equals("Formal Mode")){
            if(isFormalSituation){
                Log.d("Silent","Silent");
                soundHandler.setSoundMode("silent");
            }else{
                Log.d("vibrate","vibrate");
                soundHandler.setSoundMode("vibrate");
            }
        }

        return;
    }
}
