package jid.quitedroid.Services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import jid.quitedroid.ContextHandler;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.SoundHandler;

/**
 * Created by JiYeon on 2016-04-25.
 */
public class ModeHandler {
    private static final String MyPREFERENCES = "InternalMode";
    private SharedPreferences sharedpreferences;
    private SoundHandler soundHandler = new SoundHandler();

    //Constructor
    public ModeHandler() {
        //Shared preferences that store the current internal mode in a xml file
        sharedpreferences = ContextHandler.getContext().getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
    }

    public void setMode(String mode) {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        //Clear internal mode shared preferences
        editor.clear();
        //Put in new mode in mode shared preferences
        editor.putBoolean(mode, true);
        //Commit
        editor.commit();
    }

    public String getMode() {
        //Retrieve current mode
        Map<String, ?> keys = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            //Return current mode
            return entry.getKey();
        }
        //Should not reach here!
        Log.d("Mode ERROR","In getMode(), ModeHandler");
        return "Default Mode";
    }

    public void setInternalFormalMode(Boolean isFormalSituation) {
//        this.isFormalSituation = isFormalSituation; //Delete Soon!
        SharedPreferences.Editor editor = sharedpreferences.edit();
        //Clear internal mode shared preferences
        editor.clear();
        //Put in new mode in mode shared preferences
        editor.putBoolean(MeetingMode.name, isFormalSituation);
        //Commit
        editor.commit();
        //Set sound type
        setSoundType();
    }

    public Boolean getInternalFormalMode() {
        //Initialize flag
        Boolean isFormalSituation = true;
        //Retrieve current mode
        Map<String, ?> keys = sharedpreferences.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            //Store formal situation
            isFormalSituation = (Boolean)(entry.getValue());
        }
        return isFormalSituation;
    }

    private void setSoundType() {
        //If it is a formal situation set phone to silent -- this.isFormalSituation
        //Set default to formal situation
        if (sharedpreferences.getBoolean(MeetingMode.name, true)) {
            soundHandler.setSoundMode("silent");
            //If it is an informal situation set phone to vibrate
        } else {
            soundHandler.setSoundMode("vibrate");
        }
    }
}
