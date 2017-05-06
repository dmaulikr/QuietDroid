package jid.quitedroid.Modes;

import android.graphics.Color;

import jid.quitedroid.R;

/**
 * Created by Dawin on 4/14/2016.
 */
public class MeetingMode extends Mode implements IMode{
    public static final int icon = R.drawable.icon_orange;
    public static final String name = "Formal Mode";
    public static final String soundType = "vibrate"; //Set to vibrate, but later incorporate proximity sensor values
    public static final int COLOR = Color.rgb(255,140,0); //Yellow for now, find the correct RGB
    public static final String[] keywords = {"meeting", "interview"};
    public MeetingMode(){
        super(icon, name, soundType, COLOR, keywords);
    }
    public static boolean isModeKeyword(String keyword) {
        //for normal mode
        if(keywords.length <= 0) return false;
        keyword = keyword.toLowerCase();
        for (String s : keywords)
            if (keyword.contains(s))
                return true;
        return false;
    }
}
