package jid.quitedroid;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by JiYeon on 2016-04-01.
 */
public class SoundHandler{

    //Change Sound Mode according to the input mode (Silent, Vibrate, Mode)
    public void setSoundMode(String mode){

        //Declare Audio Manager to control different sound modes (Silent, Vibrate, Normal)
        AudioManager audioManager = (AudioManager) ContextHandler.getContext().getSystemService(Context.AUDIO_SERVICE);

        switch (mode.toLowerCase()) {
            case "vibrate":
                audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                break;

            case "normal":
                audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                break;

            case "silent":
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                break;
        }

    }
}
