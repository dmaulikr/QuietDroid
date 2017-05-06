package jid.quitedroid;

import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by JiYeon on 2016-03-18.
 */
public class messageHandler {

    //Send a sms message when a call is denied according to the mode
    protected void sendSMSMessage(String phoneNumber, String current_mode){
        try {
            SmsManager smsManager = SmsManager.getDefault();

            if(current_mode.equals("Blocking Mode")){
                Log.d("Intent Detected", "blocking call later please");
//                smsManager.sendTextMessage(phoneNumber, null, "Please call back at a later time. I do not have access to the phone. This message was generated by QuiteDroid.", null, null);
            }else if(current_mode.equals("Formal Mode")){
                Log.d("Intent Detected", "formal call later please");
//                smsManager.sendTextMessage(phoneNumber, null, "Please call back at a later time. I am busy right now, so I cannot receive your call. This message was generated by QuiteDroid.", null, null);
            }
        }

        catch (Exception e) {
            //Display to user that the sms message did not send
            //Toast.makeText(getApplicationContext(), "SMS failed, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
