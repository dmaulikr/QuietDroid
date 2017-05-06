package jid.quitedroid.Modes;

import android.graphics.Color;

import jid.quitedroid.R;

/**
 * Created by Dawin on 4/14/2016.
 */
public class NormalMode extends Mode implements IMode{
    public static final int icon = R.drawable.icon_green;
    public static final String name = "Normal Mode";
    public static final String soundType = "normal";
    public static final int COLOR = Color.GREEN;
    public static final String[] keywords = {};
    public NormalMode(){
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
