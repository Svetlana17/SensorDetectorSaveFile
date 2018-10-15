package com.arkadygamza.shakedetector;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import rx.Observable;
import rx.Subscription;

public class GiroscopeActivity extends AppCompatActivity {

    private final List<SensorPlotter> mPlotters = new ArrayList<>(3);
    private TextView tvText;
    private Button startButton;
    private Button shareButton;
    private SensorManager sensorManager;
    private Sensor sensorAccel;
    private Sensor sensorGiroscope;
    private StringBuilder sb = new StringBuilder();
    private DBHelper dbHelper;
    private Timer timer;
    private final static int UPDATE_TIME = 400;
    private boolean writingData = false;
    private float[] valuesAccel = new float[3];
    private float[] valuesGiroscope = new float[3];
    Button one;
    public String state = "DEFAULT";
    File file;
    private Observable<?> mShakeObservable;
    private Subscription mShakeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giroscope);
        one = (Button) findViewById(R.id.save);

        tvText = (TextView) findViewById(R.id.tvText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGiroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        dbHelper = new DBHelper(this);
        startButton = (Button) findViewById(R.id.start);
        shareButton = (Button) findViewById(R.id.share);
        mShakeObservable = ShakeDetector.create(this);


        //  Button saveButton = (Button) findViewById(R.id.save);


        one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (writingData) {
                    saveData();
                } else {
                    writingData = !writingData;
                    startButton.setText(writingData ? R.string.stop_writing : R.string.start_writing_data);
                }
            }
        });
        Button shareButton = (Button) findViewById(R.id.shared);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });
    }

    private void saveData() {
        boolean result = Utils.saveFile(
                new File(getExternalFilesDir(null), "accel" + System.currentTimeMillis() + ".txt"),
                dbHelper.readData().getBytes());
        if (!result) {
            Toast.makeText(getApplicationContext(), "Can't write to file!", Toast.LENGTH_LONG).show();
        }
        dbHelper.clearDataBase();
        writingData = false;
        startButton.setText(R.string.start_writing_data);
    }

    private void share() {
        File dir = getExternalFilesDir(null);
        File zipFile = new File(dir, "accel.zip");
        if (zipFile.exists()) {
            zipFile.delete();
        }
        File[] fileList = dir.listFiles();
        try {
            zipFile.createNewFile();
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
            out.setLevel(Deflater.DEFAULT_COMPRESSION);
            for (File file : fileList) {
                zipFile(out, file);
            }
            out.close();
            sendBundleInfo(zipFile);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Can't send file!", Toast.LENGTH_LONG).show();
        }
    }

    private void sendBundleInfo(File zipFile) {
        // private void sendBundleInfo(File file) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file));
        startActivity(Intent.createChooser(emailIntent, "Send data"));
    }
    //  }


    private static void zipFile(ZipOutputStream zos, File file) throws IOException {
        zos.putNextEntry(new ZipEntry(file.getName()));
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4092];
        int byteCount = 0;
        try {
            while ((byteCount = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, byteCount);
            }
        } finally {
            safeClose(fis);
        }
        zos.closeEntry();
    }

    private static void safeClose(FileInputStream fis) {
        try {
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();


        Observable.from(mPlotters).subscribe(SensorPlotter::onResume);
        mShakeSubscription = mShakeObservable.subscribe((object) -> Utils.beep());

       // sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(listener, sensorGiroscope, SensorManager.SENSOR_DELAY_NORMAL);


        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showInfo();
                        if (writingData) {

                            dbHelper.addData(valuesAccel, valuesGiroscope);
                        }
                    }
                });
            }
        };
        timer.schedule(task, 0, UPDATE_TIME);
    }

    @Override
    protected void onPause() {
        super.onPause();
//        saveData();
        sensorManager.unregisterListener(listener);
        timer.cancel();
    }


    String format(float values[]) {
        return String.format(Locale.US, "%1$.2f\t\t%2$.2f\t\t%3$.2f", values[0], values[1], values[2]);
    }

    void showInfo() {
        sb.setLength(0);
        //sb.append("Акселерометр: ").append(format(valuesAccel))
               sb .append("\n\nГироскоп: ").append(format(valuesGiroscope));

        tvText.setText(sb);
    }



    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    for (int i = 0; i < 3; i++) {
                        valuesAccel[i] = event.values[i];

                    }
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    for (int i = 0; i < 3; i++) {
                        valuesGiroscope[i] = event.values[i];
                        //  System.arraycopy(event.values, 0, valuesGiroscope, 0, 3);
                        break;

                    }
            }

        }
    };

    }





