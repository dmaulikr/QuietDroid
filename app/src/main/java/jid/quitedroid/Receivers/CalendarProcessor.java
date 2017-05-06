package jid.quitedroid.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import jid.quitedroid.Database.DatabaseOperations;
import jid.quitedroid.Database.EventOperations;
import jid.quitedroid.Database.EventProcessing;
import jid.quitedroid.Database.TableData;
import jid.quitedroid.GlobalConstants;
import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.Mode;
import jid.quitedroid.Modes.NormalMode;

/**
 * Created by Dawin on 6/18/2016.
 */
public class CalendarProcessor extends BroadcastReceiver {

    public static final int
            _ID = 0,
            CALENDAR_ID = 1,
            TITLE = 2,
            DTSTART = 3,
            DTEND = 4,
            EVENT_LOCATION = 5,
            DURATION = 6,
            RRULE = 7,
            RDATE = 8;

    public static String[] REQUESTING_ARGS = new String[]{
            CalendarContract.EventsEntity._ID,
            CalendarContract.EventsEntity.CALENDAR_ID,
            CalendarContract.EventsEntity.TITLE,
            CalendarContract.EventsEntity.DTSTART,
            CalendarContract.EventsEntity.DTEND,
            CalendarContract.EventsEntity.EVENT_LOCATION,
            CalendarContract.EventsEntity.DURATION,
            CalendarContract.EventsEntity.RRULE,
            CalendarContract.EventsEntity.RDATE
    };

    //custom intent action types:
    //action_initial_trigger for when the app is enabled
    //action_weekly_trigger for every monday to process the calendar
    public static final String ACTION_INITIAL_TRIGGER = "ACTION_INITIAL_TRIGGER",
            ACTION_WEEKLY_TRIGGER = "ACTION_WEEKLY_TRIGGER";

    public static Calendar getTodayDateOnly(){

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);

        return todayCalendar;
    }

    /*function unused
    *we process the calendar from todays date to the nearest sunday
    *we ignore the previous days (e.g. monday -> today)
    */
    public static Calendar getThisMonday(Calendar today){
        log("getThisMonday: " + today.getTime().toString());
        Calendar previousMonday = (Calendar) today.clone();
        //int daysToPreviousMonday = Calendar.MONDAY - today.get(Calendar.DAY_OF_WEEK);
        //previousMonday.add(Calendar.DAY_OF_WEEK, daysToPreviousMonday);

        previousMonday.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        if(previousMonday.compareTo(today) == 1)
            previousMonday.add(Calendar.DAY_OF_MONTH, -7);

        log("getThisMonday: " + previousMonday.getTime().toString());
        return previousMonday;
    }

    public static Calendar getThisSunday(Calendar today)
    {
        Calendar sunday = (Calendar)today.clone();
        sunday.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        sunday.add(Calendar.HOUR_OF_DAY, 23);
        sunday.add(Calendar.MINUTE, 59);
        sunday.add(Calendar.SECOND, 59);

        if(sunday.compareTo(today) == -1)
            sunday.add(Calendar.DAY_OF_MONTH, 7);

        log("getThisSunday()", "Today: " + today.getTime().toString() + "\nCalculated Sunday: " + sunday.getTime().toString());
        return sunday;
    }

    public static boolean processWeek(Context context) {
        DatabaseOperations database = new DatabaseOperations(context);
        //clean the current database (2 tables: raw and organised) for a fresh new week
        database.deleteTableData(database, TableData.TableInfo.TABLE_NAME_RAW);
        database.deleteTableData(database, TableData.TableInfo.TABLE_NAME_ORGANIZED);

        Calendar today = Calendar.getInstance();

        //temporarily select a date TODO change this later to Calendar.getInstance()
        //I have chosen this date as there were quite a few meetings that week :) ieee meeting, quitedroid meeting, camp meeting

//        today.set(Calendar.YEAR, 2016);
//        today.set(Calendar.MONTH, Calendar.MAY);
//        today.set(Calendar.DAY_OF_MONTH, 4);

        //Fetch calendar events from today and until sunday
        final String SELECTION = "("
                + CalendarContract.Events.DTSTART + " >= "
                + today.getTimeInMillis()
//                + " AND "
//                + CalendarContract.Events.DTEND + " <= "
//                + getThisSunday(today).getTimeInMillis()
                + " AND "
                + CalendarContract.Events.DTSTART + " <= "
                + getThisSunday(today).getTimeInMillis()
                + " AND "
                + CalendarContract.Events.DTEND + " != 0"
                + " AND "
                + CalendarContract.Events.ALL_DAY + " = 0"
                + ")"
                ;

        Cursor cursor = context.getApplicationContext()
                .getContentResolver()
                .query(
                        Uri.parse("content://com.android.calendar/events"),
                        REQUESTING_ARGS,
                        SELECTION, null, null);
        if (cursor == null) {
            log("failed to access to calendar, got a null cursor");
            return false;
        }else{
            //initialise group id to 0, dtstart and dtend registers
            int global_group_id = 0;
            long previousCursorDtStart = 0, previousCursorDtEnd = 0;
            long dtstart = 0, dtend = 0;

            if(cursor.getCount() == 0){
                log("processWeek()", "There are 0 Calendar events to be scheduled");
//                return false; TODO MUST PUT BACK
            }

//            cursor.moveToFirst();
//
//            do{
//                //check if current cursor overlaps with previous cursor
//
//                try {
//                    dtstart = Long.parseLong(cursor.getString(DTSTART));
//                    dtend = Long.parseLong(cursor.getString(DTEND));
//                } catch (Exception e) {
//                    log("processWeek()", "TryParseLong of dtstart,end failed");
//                    log("processWeek() title", cursor.getString(TITLE));
//                    log("processWeek() dtstart", cursor.getString(DTSTART));
//                    log("processWeek() duration ", cursor.getString(DURATION));
//                    log("processWeek() dtend", cursor.getString(DTEND));
//
//                }
//
//                String logMessage = "";
//                logMessage += "New Mode Intent created\n";
//                //modeIntent.setData(Uri.parse(cursor.getString(ORIGINAL_ID)));
//                logMessage += "Data: " + cursor.getString(_ID) + "\n";
//                logMessage += "Title: " + cursor.getString(TITLE) + "\n";
//
//                Calendar c = Calendar.getInstance();
//                c.setTimeInMillis(Long.parseLong(cursor.getString(DTSTART)));
//                logMessage += "Dtstart" + c.getTime().toString() + "\n";
//                c.setTimeInMillis(Long.parseLong(cursor.getString(DTEND)));
//                logMessage += "Dtend" + c.getTime().toString() + "\n";
//
//                String title = cursor.getString(TITLE);
//                int mode = Mode.getMode(title);
//                int mode_color = Mode.getModeColor(title);
//
//                /** CHANGING the COLOR Of events **/
//                ContentResolver cr = context.getContentResolver();
//                ContentValues changeColor = new ContentValues();
//
//                Uri updateUri = null;
//                changeColor.put(CalendarContract.Events.EVENT_COLOR, mode_color);
//                updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(cursor.getString(_ID)));
//                int rows = context.getContentResolver().update(updateUri, changeColor, null, null);
//
//
//                //if previous event and current event doesn't overlap, then give a different group id
//                //overlapping events have the same group id
//                if (!isOverlapping(previousCursorDtStart,
//                        previousCursorDtEnd,
//                        dtstart)) {
//                    global_group_id++;
//                }
//
//                //put each event details into database
//                database.insert(database,
//                        TableData.TableInfo.TABLE_NAME_RAW,
//                        title,
//                        dtstart,
//                        dtend,
//                        mode,
//                        0,
//                        "",
//                        "",
//                        global_group_id,
//                        0,
//                        cursor.getString(_ID)
//                ); //TODO Database needs to change, get rid of unused parameters
//
//                //set previous dtstart and end with current as loop has ended
//                previousCursorDtStart = dtstart;
//                previousCursorDtEnd = dtend;
//
//                log(logMessage);
//            }while(cursor.moveToNext());

            Log.d("processWeek()", "weekly processing has finished");

            cursor.close();
            database.showInformation(TableData.TableInfo.TABLE_NAME_RAW);
            database.close();
        }

        //TODO event operations, proces...
//        EventOperations eventOperations = new EventOperations();
//        eventOperations.organizeEventsToNewDbTable();

        Log.d("-", "-------------------------------------------------------------------------");
        AlarmManager am = (AlarmManager)context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, createNewIntent(context, BlockingMode.name, "1"));
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 15000, createNewIntent(context, NormalMode.name, "2"));
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 20000, createNewIntent(context, MeetingMode.name, "3"));
//        database.showInformation(TableData.TableInfo.TABLE_NAME_ORGANIZED);

//        EventProcessing eventProcessing = new EventProcessing();
//        eventProcessing.processAllEvents(context);

        return true;
    }
    private static PendingIntent createNewIntent(Context context, String mode, String eventID){
        Intent d_intent = new Intent(context, SetModeReceiver.class);
        d_intent.setData(Uri.parse(eventID));
        d_intent.putExtra("mode", mode);
        PendingIntent d_pi = PendingIntent.getBroadcast(context, 1, d_intent, PendingIntent.FLAG_ONE_SHOT);
        return d_pi;


//        PendingIntent d_pi = PendingIntent.getService(context, 1, d_intent, PendingIntent.FLAG_ONE_SHOT);
//        return d_pi;
    }
    private static boolean isOverlapping(long lower, long upper, long target){
        return target >= lower && target <= upper;
    }

    public static void processDirty(Context context){

    }

    public static void scheduleTriggerForNextMonday(Context context){
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);

        //check that calendarProcessor was invoked on a monday
        if((todayCalendar.get(Calendar.DAY_OF_WEEK)) == Calendar.MONDAY){
            //schedule monday trigger for next monday (today + 7 days)
            todayCalendar.add(Calendar.DAY_OF_WEEK, 7);
        }

        /*
        * PendingIntent below changed to public static of MainActivity for cancelling
        * */
        //PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        Intent intent = new Intent(context, CalendarProcessor.class);
        intent.setAction(ACTION_WEEKLY_TRIGGER);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        //Toast.makeText(context.getApplicationContext(), "Next monday trigger set in 2 seconds", Toast.LENGTH_LONG).show();
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 10000, pendingIntent);

        //TEMP DEBUGGIGNG TODO REMOVE THIS
        Random r = new Random();
        Intent d_intent = new Intent(context, SetModeReceiver.class);

        /*int randNum = r.nextInt(100);
        if(randNum < 100 && randNum > 60){
            d_intent.putExtra("mode", BlockingMode.name);
        }else if(randNum < 60 && randNum > 30){
            d_intent.putExtra("mode", MeetingMode.name);
        }else{
            d_intent.putExtra("mode", NormalMode.name);
        }
        PendingIntent d_pi = PendingIntent.getBroadcast(context, 1, d_intent, PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, d_pi);
        */
        log("scheduleTriggerForNextMonday()", "Next monday trigger set");
    }

    public static void cancelAllAlarms(Context context){
        DatabaseOperations databaseOperations = new DatabaseOperations(context);
        List<String> eventIds = databaseOperations.getAllEventIds(TableData.TableInfo.TABLE_NAME_ORGANIZED);
        //TODO cancel all alarms according to elements in the above array
        Iterator<String> eventIdsIterator = eventIds.iterator();
        Intent intent = new Intent(context, SetModeReceiver.class);
        PendingIntent pendingIntent = null;

        while(eventIdsIterator.hasNext()){
            String eventId = eventIdsIterator.next();
            intent.setData(Uri.parse(eventId)); //TODO is this correct data syntax?
            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pendingIntent);
            Log.d("CalendarProcessor.Java", "cancelAllAlarms: " + eventId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean quiteDroid_isEnabled = sharedPreferences.getBoolean(GlobalConstants.QUITEDROID_ENABLED, false);

        if(quiteDroid_isEnabled) {

            switch (intent.getAction()) {
                //first installation processing
                case ACTION_INITIAL_TRIGGER:
                    log("Receive?", "INITIAL TRIGGER RECEIVED");
                    cancelAllAlarms(context);
                    processWeek(context);
                    //Toast.makeText(context.getApplicationContext(), "INITIAL TRIGGERED", Toast.LENGTH_LONG).show();
                    //scheduleTriggerForNextMonday(context);
                    break;
            /*
            PHONE REBOOTED PROCESSING
            ALARMMANAGER IS RESET, NEEDS RESCHEDULING FROM SCRATCH
            */
                case Intent.ACTION_BOOT_COMPLETED:
                    Toast.makeText(context.getApplicationContext(), "BOOT COMPLETE TRIGGERED", Toast.LENGTH_LONG).show();
                    cancelAllAlarms(context);
                    processWeek(context);
                    scheduleTriggerForNextMonday(context);
                    break;
                //calendar dirty processing
                case Intent.ACTION_PROVIDER_CHANGED: //TODO find an action for calendar changed
                    //processDirty(context);
                    //Log.d("CalendarProcessor.java", "onReceive: PROVIDER CHANGED INVOKED");
                    //cancelAllAlarms(context);
                    //processWeek(context);

                    break;
                //monday trigger weekly processing
                case ACTION_WEEKLY_TRIGGER:
                    cancelAllAlarms(context);
                    processWeek(context); //TODO uncomment this
                    scheduleTriggerForNextMonday(context);
                    break;
                //EXCEPTIONAL CASE, DO NOTHING
                default:
                    break;
            }

        }else{
            log("CalendarProcesor", "No processing, due to quitedroid disabled");
        }






        //register next monday trigger
        //scheduleTriggerForNextMonday(context);

        //process calendar
        //Toast.makeText(context.getApplicationContext(), "Calendar processed!", Toast.LENGTH_LONG).show();
        //// TODO: 6/22/2016 in another class make sure that Intent.filterEquals exists to tell apart two pending intents


    }

    public static void log(String details){
        Log.d("Dawin LOG", details);
    }
    public static void log(String tag, String details){ Log.d(tag, details); }

    public static void myToast(Context context, String details){
        Toast.makeText(context.getApplicationContext(), details, Toast.LENGTH_LONG).show();
    }
}
