package com.arkadygamza.shakedetector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private final static String AccelerometerX = "AccelerometerX";
    private final static String AccelerometerY = "AccelerometerY";
    private final static String AccelerometerZ = "AccelerometerZ";
//    private final static String GyroscopeX="GyroscopeX";
//    private final static String GyroscopeY="GyroscopeY";
//    private final static String GyroscopeZ="GyroscopeZ";

    private final static String ID = "id";


    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL("create table mytable ("
                + ID + " integer primary key autoincrement,"
              + AccelerometerX + " double,"
                + "\t"
                + AccelerometerY + " double,"
                + "\t"
                + AccelerometerZ + " double"
            //,
//                + GyroscopeX + "double,"
//                + GyroscopeY + "double,"
//                + GyroscopeZ + "double"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addData(float[] giroscope, float[] accelerometer) {
        SQLiteDatabase db = getWritableDatabase();
        // create object for data
        ContentValues cv = new ContentValues();
        // prepare pairs to insert
        cv.put(AccelerometerX, accelerometer[0]);
        cv.put(AccelerometerY, accelerometer[1]);
        cv.put(AccelerometerZ, accelerometer[2]);
//        cv.put(GyroscopeX, giroscope[0]);
//        cv.put(GyroscopeY, giroscope[1]);
//        cv.put(GyroscopeZ, giroscope[2]);



        //insert object to db
        long rowID = db.insert("mytable", null, cv);
        close();
        return rowID;
    }

    public String readData() {
        SQLiteDatabase db = getWritableDatabase();
        // request all data (cursor) from table
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        StringBuilder builder = new StringBuilder();
        // check that table has data
        if (c.moveToFirst()) {
            // get column index by name
            int idColIndex = c.getColumnIndex(ID);
            int accelerometerXColIndex = c.getColumnIndex(AccelerometerX);
            int accelerometerYColIndex = c.getColumnIndex(AccelerometerY);
            int accelerometerZColIndex = c.getColumnIndex(AccelerometerZ);
//            int giroscopeXColIndex = c.getColumnIndex(GyroscopeX);
//            int giroscopeYColIndex = c.getColumnIndex(GyroscopeY);
//            int giroscopeZColIndex = c.getColumnIndex(GyroscopeZ);

            do {

                // get data by column indexes
                int id = c.getInt(idColIndex);
                float accelerometerX = c.getFloat(accelerometerXColIndex);
                float accelerometerY = c.getFloat(accelerometerYColIndex);
                float accelerometerZ = c.getFloat(accelerometerZColIndex);


//                float giroscopeX = c.getFloat(giroscopeXColIndex);
//                float giroscopeY = c.getFloat(giroscopeYColIndex);
//                float giroscopeZ = c.getFloat(giroscopeZColIndex);

                builder.append(id).append(";")
                        .append(accelerometerX).append("Acc X;")
                        .append(accelerometerY).append("Acc Y;")
                        .append(accelerometerZ).append("Acc Z;");
//                        .append(giroscopeX).append("Gir X;")
//                        .append(giroscopeY).append("Gir Y;")
//                        .append(giroscopeZ).append("Gir Z;");
            } while (c.moveToNext());
        }
        c.close();
        close();
        return builder.toString();
    }
    public int clearDataBase() {
        SQLiteDatabase db = getWritableDatabase();
        int clearCount = db.delete("mytable", null, null);
        close();
        return clearCount;
    }
}