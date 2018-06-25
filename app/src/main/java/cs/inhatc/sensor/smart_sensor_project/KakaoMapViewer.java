package cs.inhatc.sensor.smart_sensor_project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static com.kakao.util.maps.helper.Utility.getPackageInfo;

public class KakaoMapViewer extends AppCompatActivity implements SensorEventListener {

    private static SensorManager objSMG;
    private static Sensor sensor_Accelerometer;
    private static String date;

    public static String getDate() {
        return date;
    }

    //뒤로가기 버튼 Delay
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;

    private MapView mapView;
    private WalkData walkData;
    private TextView walkCount;
    private SQLiteDatabase sql;
    private permissionCall perMC;
    private Button btnMove;
    private int num;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.view_main);
        perMC = new permissionCall(this);

        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd");
        date = fm1.format(new Date());

        mapView = new MapView(this);
        mapView.setDaumMapApiKey("ad85475be6af5302b83e3ef713460b83");
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = KakaoMapViewer.objSMG.getDefaultSensor(TYPE_ACCELEROMETER);

        walkData = new WalkData(this);
        walkCount = findViewById(R.id.walkCount);
        btnMove = findViewById(R.id.button);

        sql = walkData.getWritableDatabase();

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KakaoMapViewer.this,WalkingViewer.class);
                startActivity(intent);
            }
        });

        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM WalkHistory ORDER BY WalkHistory._id DESC Limit 1;", null);

        while (cursor.moveToNext()) {
            if (date.matches(cursor.getString(2))) {
                num = Integer.parseInt(cursor.getString(1));
                walkCount.setText(cursor.getString(1));
            }
        }
        cursor.close();
        sql.close();

        //Log.i("키해시",getKeyHash(getApplicationContext())); // 키해시 얻는 방법
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();

            SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            String date = fm1.format(new Date());
            sql = walkData.getWritableDatabase();
            sql.execSQL("INSERT INTO WalkHistory SELECT NULL,0,'"+date+"' WHERE NOT EXISTS(SELECT WalkHistory.Testdate FROM WalkHistory WHERE WalkHistory.Testdate='"+date+"');");

            sql.execSQL("UPDATE WalkHistory SET walk_cnt="+Integer.parseInt(""+walkCount.getText())+" WHERE WalkHistory.Testdate='"+date+"';");

            sql.close();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        objSMG.registerListener(this, sensor_Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        objSMG.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(event.values[0] > 7){
                walkCount.setText(String.valueOf(num++));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public static String getKeyHash(final Context context) {
        PackageInfo packageInfo = getPackageInfo(context, PackageManager.GET_SIGNATURES);
        if (packageInfo == null)
            return null;

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                return android.util.Base64.encodeToString(md.digest(), android.util.Base64.NO_WRAP);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
