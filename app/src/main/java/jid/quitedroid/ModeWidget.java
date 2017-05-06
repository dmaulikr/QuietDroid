package jid.quitedroid;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import jid.quitedroid.Modes.BlockingMode;
import jid.quitedroid.Modes.MeetingMode;
import jid.quitedroid.Modes.NormalMode;

/**
 * Implementation of App Widget functionality.
 */
public class ModeWidget extends AppWidgetProvider {
    private final static String WIDGET_NORMAL_BUTTON = "android.appwidget.action.WIDGET_NORMAL_BUTTON";
    private final static String WIDGET_FORMAL_BUTTON = "android.appwidget.action.WIDGET_FORMAL_BUTTON";
    private final static String WIDGET_BLOCKING_BUTTON = "android.appwidget.action.WIDGET_BLOCKING_BUTTON";

    // Initialize Modes
    BlockingMode blockingMode = new BlockingMode();
    MeetingMode meetingMode = new MeetingMode();
    NormalMode normalMode = new NormalMode();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mode_widget);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        Log.d("Receiving", intent.getAction());
        String action = intent.getAction();

        //When Normal Button (Green) has been pressed, activate normal mode
        if (WIDGET_NORMAL_BUTTON.equals(action)) {
            normalMode.showNotification();
            normalMode.setSoundType();
            //When Formal Button (Orange) has been pressed, activate formal mode
        }else if(WIDGET_FORMAL_BUTTON.equals(action)){
            meetingMode.showNotification();
            meetingMode.setSoundType();
            //When Blocking Button (Red) has been pressed, activate blocking mode
        }else if(WIDGET_BLOCKING_BUTTON.equals(action)){
            blockingMode.showNotification();
            blockingMode.setSoundType();
        }//else if(action.equals(REFRESH_ACTION)){

//        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
            Log.d("Widget","Widget has been updated!");

            //Set Remote Views
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.mode_widget);

            //Register an onclick listener for the corresponding buttons
            addNewIntent(views, context, R.id.normalButton, WIDGET_NORMAL_BUTTON);
            addNewIntent(views, context, R.id.formalButton, WIDGET_FORMAL_BUTTON);
            addNewIntent(views, context, R.id.blockingButton, WIDGET_BLOCKING_BUTTON);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

            //Create intent to update widget
            //Place in service
            //Start Service
//            startService(context);
        }
    }

    private void addNewIntent(RemoteViews views, Context context, int buttonId, String intentName){
        Intent intent = new Intent(intentName); //context, ModeWidget.class
        PendingIntent pendingIntentNormal = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(buttonId, pendingIntentNormal);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
//        endService(context);
        super.onDeleted(context, appWidgetIds);
    }
}

