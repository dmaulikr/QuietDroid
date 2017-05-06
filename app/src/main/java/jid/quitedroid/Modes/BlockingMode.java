package jid.quitedroid.Modes;

import android.graphics.Color;

import jid.quitedroid.R;

/**
 * Created by Dawin on 4/14/2016.
 */
public class BlockingMode extends Mode implements IMode{
    public static final int icon = R.drawable.icon_red;
    public static final String name = "Blocking Mode";
    public static final String soundType = "silent";
    public static final int COLOR = Color.RED;
    public static final String[] keywords = {"exam", "test"};

    public BlockingMode(){
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