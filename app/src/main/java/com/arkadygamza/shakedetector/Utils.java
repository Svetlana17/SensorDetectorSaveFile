package com.arkadygamza.shakedetector;

import android.media.AudioManager;
import android.media.ToneGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Utils {
    public static void beep() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_SYSTEM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
    }
    public static boolean saveFile(File file, byte[] bytes) {
        FileOutputStream outputStream = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(file);
            stream.write(bytes);
        } catch (Exception e) {
            return false;
        } finally {
            if (outputStream != null)
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return true;
    }

}
