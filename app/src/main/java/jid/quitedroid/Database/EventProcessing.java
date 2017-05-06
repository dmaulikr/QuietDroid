package jid.quitedroid.Database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import jid.quitedroid.ContextHandler;
import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.NormalMode;
import jid.quitedroid.Receivers.SetModeReceiver;
import jid.quitedroid.Services.ModeService;

/**
 * Created by JiYeon on 2016-07-18.
 */

//Process all the events in organized table of database
public class EventProcessing {
    private DatabaseOperations databaseOperations;
    private AlarmManager am;

    //When this method is invoked, all the organized events are placed into the Alarm Manager
    public void processAllEvents(Context context) {
        databaseOperations = new DatabaseOperations(context);
        //Initialize Alarm Manager
        am = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //Create cursor
        Cursor cursor = databaseOperations.getCursor(databaseOperations, TableData.TableInfo.TABLE_NAME_ORGANIZED);
        //Move cursor to first row
        cursor.moveToFirst();
        //Continue while there is a next row to move to
        do{
            //Retrieve new intent, set to Alarm Manager
            try{

                //am.set(AlarmManager.RTC, cursor.getLong(1), createNewIntent(context, cursor.getInt(3), cursor.getString(9)));


            }catch(Exception ex){
                Log.d("ERROR Event Process:",ex.getMessage());
            }
        }while(cursor.moveToNext());
    }

    private PendingIntent createNewIntent(Context context, int mode, String eventID){
        Intent d_intent = new Intent(context, SetModeReceiver.class);
        d_intent.setData(Uri.parse(eventID));
        d_intent.putExtra("mode", manageMode(mode));
        PendingIntent d_pi = PendingIntent.getBroadcast(context, 1, d_intent, PendingIntent.FLAG_ONE_SHOT);
        return d_pi;


//        PendingIntent d_pi = PendingIntent.getService(context, 1, d_intent, PendingIntent.FLAG_ONE_SHOT);
//        return d_pi;
    }

    private String manageMode(int mode){
        switch(mode){
            case 2: //Blocking Mode
                return BlockingMode.name;
            case 1: //Formal Mode
                return MeetingMode.name;
            case 0: //Normal Mode
                return NormalMode.name;
            default: //Normal Mode
                return "Testing Non Valid Mode";
        }
    }
}
