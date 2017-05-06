package jid.quitedroid.Database;

import android.provider.BaseColumns;

/**
 * Created by JiYeon on 2016-06-28.
 */
public class TableData {
    public TableData(){

    }

    public static abstract class TableInfo implements BaseColumns
    {
        public static final String EVENT_TITLE = "event_title"; //First column name
        public static final String EVENT_START_TIME = "event_start_time"; //Second column name
        public static final String EVENT_END_TIME = "event_end_time"; //Third column name
        public static final String EVENT_MODE = "event_mode"; //Fourth column name
        public static final String NUMBEROF_TRIGGER = "numberof_trigger"; //Fifth column name
        public static final String MODE_SCHEDULE = "mode_schedule"; //Sixth column name
        public static final String EVENT_TRIGGER_TIMES = "event_trigger_times"; //Seventh column name
        public static final String GROUP_ID = "group_id"; //Eighth column name
        public static final String EVENT_ID = "event_id";
        public static final String OVERLAP_ID = "overlap_id"; //Ninth column name
        public static final String DATABASE_NAME = "quitedroid_info"; //Database Name
        public static final String TABLE_NAME_RAW = "calendar_info_raw"; //Table Name
        public static final String TABLE_NAME_ORGANIZED = "calendar_info_organized"; //Table Name
    }
}
