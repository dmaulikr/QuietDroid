package jid.quitedroid.Features;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Map;

import jid.quitedroid.ContextHandler;

/**
 * Created by JiYeon on 2016-07-25.
 */

//This class controls the functionality of:
//Same caller c
public class PriorityCallHandler {
    private static final String PriorityCallerList = "PriorityCallerList";
    private static final String ExceptionList = "ExceptionList" ;
    private SharedPreferences priorityCallerSharedPreferences, exceptionListSharedPreferences;
    private Boolean isEnabled = true; //TODO temporarily set to TRUE should be set to FALSE

    public PriorityCallHandler(){
        priorityCallerSharedPreferences = ContextHandler.getContext().getSharedPreferences(PriorityCallerList, Context.MODE_PRIVATE);
        exceptionListSharedPreferences = ContextHandler.getContext().getSharedPreferences(ExceptionList, Context.MODE_PRIVATE);
    }

    //Receive boolean from SettingsFeaturesActivity
    public void setEnabled(Boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    //When a call is blocked, record the caller's number in shared preferences
    public void recordCaller(String callerNumber){
        String blockedCallerNumber = callerNumber;
        //Find out how many times the same caller called
        int freq = priorityCallerSharedPreferences.getInt(blockedCallerNumber, 0);

        Log.d("BLOCKED CALLER NUMBER", freq + "");
        Log.d("BLOCKED CALLER NUMBER", freq + "");
        Log.d("BLOCKED CALLER NUMBER", freq + "");

        //If the same caller called for more than three times, include the
        if(freq > 2){
            //getCallerName()
            SharedPreferences.Editor exceptionListEditor = exceptionListSharedPreferences.edit();
            exceptionListEditor.putString("TEMP", callerNumber); //Temp should be changed to "PRIORITY CALLER + NAME"?
            exceptionListEditor.commit();
            return;
        }

        SharedPreferences.Editor editor = priorityCallerSharedPreferences.edit();

        //Increment frequency by one and set as new frequency
        editor.putInt(blockedCallerNumber, freq + 1);

        //Commit
        editor.commit();
    }

    //TODO
    private void getCallersName(){

    }

    public void resetCallerList(){
        SharedPreferences.Editor editor = priorityCallerSharedPreferences.edit();
        //Clear priority caller list shared preferences
        editor.clear();
        //Commit
        editor.commit();
    }
}
