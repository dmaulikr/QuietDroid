package jid.quitedroid.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JiYeon on 2016-06-28.
 */
//Main Database Operations
public class DatabaseOperations extends SQLiteOpenHelper {
    public static final int database_version = 1;

    //CREATE TABLE QUERIES FOR RAW AND ORGANIZED TABLES
    public String CREATE_QUERY_RAW = "CREATE TABLE " + TableData.TableInfo.TABLE_NAME_RAW + "(" + TableData.TableInfo.EVENT_TITLE + " TEXT," + TableData.TableInfo.EVENT_START_TIME + " LONG," + TableData.TableInfo.EVENT_END_TIME + " LONG," + TableData.TableInfo.EVENT_MODE + " INTEGER," + TableData.TableInfo.NUMBEROF_TRIGGER + " INTEGER," + TableData.TableInfo.MODE_SCHEDULE + " TEXT," + TableData.TableInfo.EVENT_TRIGGER_TIMES + " TEXT," + TableData.TableInfo.GROUP_ID + " INTEGER," + TableData.TableInfo.OVERLAP_ID + " INTEGER," + TableData.TableInfo.EVENT_ID + " TEXT);";
    public String CREATE_QUERY_ORGANIZED = "CREATE TABLE " + TableData.TableInfo.TABLE_NAME_ORGANIZED + "(" + TableData.TableInfo.EVENT_TITLE + " TEXT," + TableData.TableInfo.EVENT_START_TIME + " LONG," + TableData.TableInfo.EVENT_END_TIME + " LONG," + TableData.TableInfo.EVENT_MODE + " INTEGER," + TableData.TableInfo.NUMBEROF_TRIGGER + " INTEGER," + TableData.TableInfo.MODE_SCHEDULE + " TEXT," + TableData.TableInfo.EVENT_TRIGGER_TIMES + " TEXT," + TableData.TableInfo.GROUP_ID + " INTEGER," + TableData.TableInfo.OVERLAP_ID + " INTEGER," + TableData.TableInfo.EVENT_ID + " TEXT);";
    private long endTime = 0;

    public DatabaseOperations(Context context) {
        super(context, TableData.TableInfo.DATABASE_NAME, null, database_version);
        Log.d("Database", "Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_QUERY_RAW);
        db.execSQL(CREATE_QUERY_ORGANIZED);
        Log.d("Database", "Table");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //Insert event into database
    public void insert(DatabaseOperations databaseOperations, String tableName, String titleOfEvent, long dtStart, long dtEnd, int mode, int numberOfTrigger, String modeSchedule, String triggerTimes, int groupID, int full_overlapID, String eventID) {
        SQLiteDatabase sqLiteDatabase = databaseOperations.getWritableDatabase(); //enable write to database
        //Content values to insert to database
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableData.TableInfo.EVENT_TITLE, titleOfEvent);
        try {
            contentValues.put(TableData.TableInfo.EVENT_START_TIME, dtStart);
            contentValues.put(TableData.TableInfo.EVENT_END_TIME, dtEnd);
        } catch (Exception exception) {
            Log.d("Database Item Insertion", exception.getMessage());
        }
        contentValues.put(TableData.TableInfo.EVENT_MODE, mode);
        contentValues.put(TableData.TableInfo.NUMBEROF_TRIGGER, numberOfTrigger);
        contentValues.put(TableData.TableInfo.MODE_SCHEDULE, modeSchedule);
        contentValues.put(TableData.TableInfo.EVENT_TRIGGER_TIMES, triggerTimes);
        contentValues.put(TableData.TableInfo.GROUP_ID, groupID);
        contentValues.put(TableData.TableInfo.OVERLAP_ID, full_overlapID);
        contentValues.put(TableData.TableInfo.EVENT_ID, eventID);
        //Insert content values to database
        sqLiteDatabase.insert(tableName, null, contentValues);
        Log.d("Database", "One row inserted");
    }

    //Delete the specified event using event_mode, event_start_time, event_end_time
    public void removeEvent(DatabaseOperations databaseOperations, String tableName, String eventID) {
        SQLiteDatabase sqLiteDatabase = databaseOperations.getWritableDatabase();
        sqLiteDatabase.delete(tableName, TableData.TableInfo.EVENT_ID + " = ?", new String[]{eventID});
    }

    //Return a cursor object
    public Cursor getCursor(DatabaseOperations databaseOperations, String tableName) {
        //Read from the database
        SQLiteDatabase sqLiteDatabase = databaseOperations.getReadableDatabase();
        //Store column names in a string array
        String[] columns = {TableData.TableInfo.EVENT_TITLE, TableData.TableInfo.EVENT_START_TIME, TableData.TableInfo.EVENT_END_TIME, TableData.TableInfo.EVENT_MODE, TableData.TableInfo.NUMBEROF_TRIGGER, TableData.TableInfo.MODE_SCHEDULE, TableData.TableInfo.EVENT_TRIGGER_TIMES, TableData.TableInfo.GROUP_ID, TableData.TableInfo.OVERLAP_ID, TableData.TableInfo.EVENT_ID};
        //Request query
        Cursor cursor = sqLiteDatabase.query(tableName, columns, null, null, null, null, null);
        return cursor;
    }

    //Return a cursor object corresponding to the input groupID
    public Cursor getNewCursor(DatabaseOperations databaseOperations, String groupID){
        //Read from the database
        SQLiteDatabase sqLiteDatabase = databaseOperations.getReadableDatabase();
        //Store column names in a string array
        String[] columns = {TableData.TableInfo.EVENT_TITLE, TableData.TableInfo.EVENT_START_TIME, TableData.TableInfo.EVENT_END_TIME, TableData.TableInfo.EVENT_MODE, TableData.TableInfo.NUMBEROF_TRIGGER, TableData.TableInfo.MODE_SCHEDULE, TableData.TableInfo.EVENT_TRIGGER_TIMES, TableData.TableInfo.GROUP_ID, TableData.TableInfo.OVERLAP_ID, TableData.TableInfo.EVENT_ID};
        //Request query
        Cursor cursor = sqLiteDatabase.query(TableData.TableInfo.TABLE_NAME_RAW, columns, TableData.TableInfo.GROUP_ID + " LIKE ?", new String[]{"%" + groupID + "%"}, null, null, null, null);
        return cursor;
    }

    //Return a list of group ID
    public ArrayList<String> getGroupIDList(){
        ArrayList<String> groupIDList = new ArrayList<>();
        //Create cursor
        Cursor cursor = getCursor(this, TableData.TableInfo.TABLE_NAME_RAW);
        //Move cursor to first row
        cursor.moveToFirst();
        //Initialize string to compare with
        String compareGroupID = "";

        //Continue while there is a next row to move to
        do {
            try{
                //Compare the last element with the current group ID in db, if they are not the same insert into list
                if(!compareGroupID.equals(cursor.getString(7))){
                    groupIDList.add(cursor.getString(7));
                    compareGroupID = groupIDList.get(groupIDList.size() - 1);
                }
            }catch(Exception ex){
                Log.d("Dawin","There was a problem :D");
            }
        } while (cursor.moveToNext());

        //Close cursor
        cursor.close();

        return groupIDList;
    }

    public String setNewEventID(DatabaseOperations databaseOperations, String eventID){
        int numberOfSameEvents = 0; //Initialize number of same events
        Boolean start = false; //Set start flag as false
        SQLiteDatabase sqLiteDatabase = databaseOperations.getWritableDatabase(); //Initialize database
        Cursor cursor = sqLiteDatabase.query(TableData.TableInfo.TABLE_NAME_ORGANIZED, new String[]{TableData.TableInfo.EVENT_ID},
                TableData.TableInfo.EVENT_ID + " LIKE ?", new String[]{"%" + eventID + "%"},
                null, null, null); //Query database, find all events with the same event ID
        cursor.moveToFirst();

        if(cursor.moveToFirst()){ //Set cursor to first event
            start = true;
        }

        //Iterate through database and find number of events with same eventID
        do{
            if(start) numberOfSameEvents++; //Increment number of same events
        }while(cursor.moveToNext());
        //Close cursor
        cursor.close();
        //Return new event ID
        return buildNewEventID(eventID, numberOfSameEvents);
    }

    private String buildNewEventID(String eventID, int numberOfEvents){
        StringBuilder sb = new StringBuilder();
        sb.append(eventID);
        sb.append("_");
        sb.append(numberOfEvents);
        Log.d("Number of events", sb.toString());
        return sb.toString();
    }

    //Delete the specified event using event_mode, event_start_time, event_end_time
    public long findLatestTime(DatabaseOperations databaseOperations) {
        try {
            //Create cursor
            Cursor cursor = getCursor(this, TableData.TableInfo.TABLE_NAME_ORGANIZED);
            //Move cursor to last row
            cursor.moveToLast();
            //Store most recent time
            endTime = cursor.getLong(2);
            //Close cursor
            cursor.close();
        } catch (Exception ex) {
            Log.d("Latest Time ERROR", ex.getMessage());
        }

        return endTime;
    }

    //Set end time as the earliest time found in the raw database
    public void setEarliestTime(long newTime) {
        endTime = newTime;
    }

    //Delete all data from Table
    public void deleteTableData(DatabaseOperations databaseOperations, String tableName) {
        //enable write to database
        SQLiteDatabase sqLiteDatabase = databaseOperations.getWritableDatabase();
        sqLiteDatabase.delete(tableName, null, null);
    }

    //Log the content of database
    public void showInformation(String tableName){
        //Create cursor
        Cursor cursor = getCursor(this, tableName);
        //Move cursor to first row
        cursor.moveToFirst();
        //Continue while there is a next row to move to
        do{
            try{
                Log.d("In the Database", cursor.getString(0) + " START t: " + cursor.getString(1) + " END t: " + cursor.getString(2) + " MODE: " + cursor.getString(3) + " : " + cursor.getString(4) + " : " + cursor.getString(5) + " : " + cursor.getString(6) + " : " + cursor.getString(7) + " : " + cursor.getString(8) + " Unique ID: " + cursor.getString(9));
            }catch(Exception ex){
                Log.d("ERROR Database",ex.getMessage());
            }
        }while(cursor.moveToNext());
    }

    //Log the content of database
    public List<String> getAllEventIds(String tableName) {
        List<String> eventIds = new ArrayList<>();
        //Create cursor
        Cursor cursor = getCursor(this, tableName);
        //Move cursor to first row
        cursor.moveToFirst();

        //Continue while there is a next row to move to
        do {
            try{
                eventIds.add(cursor.getString(9));
            }catch(Exception ex){
                Log.d("Dawin","There was a problem :D");
            }
        } while (cursor.moveToNext());

        //Close cursor
        cursor.close();
        return eventIds;
    }
}
