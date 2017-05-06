package jid.quitedroid;

import android.app.Application;
import android.content.Context;

/**
 * Created by JiYeon on 2016-04-01.
 */

//This class provides context throughout the application Quite Droid
public class ContextHandler extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
