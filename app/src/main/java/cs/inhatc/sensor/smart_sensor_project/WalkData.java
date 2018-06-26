package cs.inhatc.sensor.smart_sensor_project;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TextView;

import com.github.mikephil.charting.data.BarEntry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WalkData extends SQLiteOpenHelper {

    private SQLiteDatabase sql;
    private Cursor cursor;

    private static String date;

    public static void setDate(String date) {
        WalkData.date = date;
    }

    public static String getDate() {
        return date;
    }

    public WalkData(Context context) {
        super(context, "DataBase", null, 1);

        sql = getWritableDatabase();

        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd");
        setDate(fm1.format(new Date()));
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table WalkHistory (_id INTEGER PRIMARY KEY AUTOINCREMENT, walk_cnt int(20), Testdate date(10))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS WalkHistory");
        onCreate(db);
    }

    public Cursor selectWData(int limitNum){
        String numValue = String.valueOf(limitNum);
        cursor = sql.rawQuery("SELECT * FROM WalkHistory ORDER BY WalkHistory._id DESC Limit '" + numValue + "';", null);

        return cursor;
    }

    public void insertAupdateWData(TextView walkCount){
        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        String date = fm1.format(new Date());

        sql = getWritableDatabase();
        sql.execSQL("INSERT INTO WalkHistory SELECT NULL,0,'"+date+"' WHERE NOT EXISTS(SELECT WalkHistory.Testdate FROM WalkHistory WHERE WalkHistory.Testdate='"+date+"');");
        sql.execSQL("UPDATE WalkHistory SET walk_cnt="+Integer.parseInt(""+walkCount.getText())+" WHERE WalkHistory.Testdate='"+date+"';");
    }

    public void closeData(){
        if(!cursor.equals(null)){
            cursor.close();
        }
        if(!sql.equals(null)){
            sql.close();
        }
    }
}