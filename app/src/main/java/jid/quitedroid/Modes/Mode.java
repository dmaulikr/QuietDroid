package jid.quitedroid.Modes;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import jid.quitedroid.Features.PriorityCallHandler;
import jid.quitedroid.MainActivity;
import jid.quitedroid.Services.ModeHandler;
import jid.quitedroid.SoundHandler;

/**
 * Created by Dawin on 4/14/2016.
 */
public class Mode implements IMode {
    public static final int notificationID = 0;
    public static final int NORMAL_MODE = 0, MEETING_MODE = 1, BLOCKING_MODE = 2;

    private int icon;
    private String name;
    private String soundType;
    private int COLOR;
    protected Notification notification;

    private Context context = MainActivity.appContext;
    private SoundHandler soundHandler = new SoundHandler();
    private ModeHandler modeHandler = new ModeHandler();
    private PriorityCallHandler priorityCallHandler = new PriorityCallHandler();
    private String[] keywords;

    public Mode(int icon, String name, String soundType, int color, String[] keywords) {
        this.icon = icon;
        this.name = name;
        this.soundType = soundType;
        this.COLOR = color;
        this.keywords = keywords;

        notification = new NotificationCompat.Builder(context)
                .setSmallIcon(this.icon)
                .setContentTitle("Quite Droid")
                .setContentText(name)
                .setCategory(Notification.CATEGORY_EVENT) //causes notification to pop up
                .setPriority(Notification.PRIORITY_MAX) //causes notification to pop up
                .setColor(COLOR).build();
    }

    @Override
    public void showNotification() {
        MainActivity.manager.notify(notificationID, notification);
        //Temp
        modeHandler.setMode(this.name);
        //Clear priority call list
        priorityCallHandler.resetCallerList();
    }

    public void setSoundType() {
        soundHandler.setSoundMode(this.soundType);
    }

    public void deactivate() {
        MainActivity.manager.cancel(notificationID);
    }

    public static int getMode(String title){
        if(MeetingMode.isModeKeyword(title)){
            return MEETING_MODE;
        }else if(BlockingMode.isModeKeyword(title)){
            return BLOCKING_MODE;
        }else{
            return NORMAL_MODE;
        }
    }

    public static int getModeColor(String title) {
        if(MeetingMode.isModeKeyword(title)){
            return MeetingMode.COLOR;
        }else if(BlockingMode.isModeKeyword(title)){
            return BlockingMode.COLOR;
        }else{
            return NormalMode.COLOR;
        }
    }
}
