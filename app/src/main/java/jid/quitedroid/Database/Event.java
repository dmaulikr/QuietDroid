package jid.quitedroid.Database;

/**
 * Created by JiYeon on 2016-07-16.
 */
public class Event {
    private int mode = 0;
    private long event_start_time = 0;
    private long event_end_time = 0;
    private String event_ID = "";

    public int getMode(){
        return mode;
    }

    public long getEvent_start_time(){
        return event_start_time;
    }

    public long getEvent_end_time(){
        return event_end_time;
    }

    public String getEventID(){
        return event_ID;
    }

    public void setMode(int newMode){
        mode = newMode;
    }

    public void setEvent_start_time(long newEvent_start_time){
        event_start_time = newEvent_start_time;
    }

    public void setEvent_end_time(long newEvent_end_time){
        event_end_time = newEvent_end_time;
    }

    public void setEvent_ID(String newEvent_ID){
        event_ID = newEvent_ID;
    }
}
