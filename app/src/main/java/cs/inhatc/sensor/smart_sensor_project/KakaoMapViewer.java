package cs.inhatc.sensor.smart_sensor_project;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.mf.map.api.MapView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static com.kakao.util.maps.helper.Utility.getPackageInfo;

public class KakaoMapViewer extends AppCompatActivity implements SensorEventListener {

    private SensorManager objSMG;
    private Sensor sensor_Accelerometer;

    private WalkData walkD;

    //뒤로가기 버튼 Delay
    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;
    private int num;

    private MapView mapView;
    private TextView walkCount_main;
    private PermissionCall perMC;
    private Button btnMove;

    Cursor cursor;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.view_main);

        mapView = new MapView(this);
        perMC = new PermissionCall(this);
        walkD = new WalkData(this);

        walkCount_main = findViewById(R.id.walkCount_main);
        btnMove = findViewById(R.id.btnmove);

        mapView.setDaumMapApiKey("ad85475be6af5302b83e3ef713460b83");
        ViewGroup mapViewContainer = findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = objSMG.getDefaultSensor(TYPE_ACCELEROMETER);

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(KakaoMapViewer.this,WalkingViewer.class);
                startActivity(intent);
                KakaoMapViewer.this.finish();
            }
        });

        cursor = walkD.selectWData(1);

        while (cursor.moveToNext()) {
            if (WalkData.getDate().matches(cursor.getString(2))) {
                num = Integer.parseInt(cursor.getString(1));
                walkCount_main.setText(cursor.getString(1));
            }
        }
        walkD.closeData();

        //Log.i("키해시",getKeyHash(getApplicationContext())); // 키해시 얻는 방법
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
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(event.values[0] > 8){
                walkCount_main.setText(String.valueOf(num++));
            }
        }
        walkD.insertAupdateWData(walkCount_main);
        walkD.closeData();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            super.onBackPressed();
        } else {
            backPressedTime = tempTime;
            Toast.makeText(this, "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show();
        }
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
