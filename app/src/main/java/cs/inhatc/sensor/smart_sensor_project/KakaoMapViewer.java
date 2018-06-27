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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
    private int num1, num2, total;

    private MapView mapView;
    private TextView walkCount_main;
    private PermissionCall perMC;
    private Button btnMove;
    private EditText EdtStart;
    private EditText EdtFinish;
    private Button btnSearch;
    private TextView walkCountY;
    private TextView sumResult;

    private Cursor cursor;
    private Intent intentSV;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.view_main);

        btnSearch = findViewById(R.id.btnSearch);
        walkCountY = findViewById(R.id.walkCountY);
        sumResult = findViewById(R.id.sumResult);

        mapView = new MapView(this);
        perMC = new PermissionCall(this);
        walkD = new WalkData(this);

        walkCount_main = findViewById(R.id.walkCount_main);
        btnMove = findViewById(R.id.btnMove);

        mapView.setDaumMapApiKey("ad85475be6af5302b83e3ef713460b83");
        ViewGroup mapViewContainer = findViewById(R.id.map_View);
        mapViewContainer.addView(mapView);

        //MapLocation MLoca =  new MapLocation(this);
        //MLoca.setMap(findViewById(R.id.map_View));

        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = objSMG.getDefaultSensor(TYPE_ACCELEROMETER);

        btnMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentWV = new Intent(KakaoMapViewer.this,WalkingViewer.class);
                intentWV.putExtra("total",String.valueOf(total));
                startActivityForResult(intentWV,1);
            }
        });


        EdtStart = findViewById(R.id.edtStart);
        EdtFinish = findViewById(R.id.edtFinish);

        intentSV = new Intent(KakaoMapViewer.this,SearchViewer.class);
        EdtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentSV.putExtra("Txt",String.valueOf(EdtStart.getText())+"s");
                startActivityForResult(intentSV, 0);
            }
        });
        EdtFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentSV.putExtra("Txt",String.valueOf(EdtFinish.getText())+"f");
                startActivityForResult(intentSV, 0);
            }
        });

        cursor = walkD.selectWData(1);
        while (cursor.moveToNext()) {
            if (WalkData.getDate().matches(cursor.getString(2))) {
                num1 = Integer.parseInt(cursor.getString(1));
                walkCount_main.setText(cursor.getString(1));
            }
        }

        cursor = walkD.selectWData(2);
        while (cursor.moveToNext()) {
            num2 = Integer.parseInt(cursor.getString(1));
        }
        total = num1-num2;
        if(total<0){
            walkCountY.setText(String.valueOf(total).substring(1));
            sumResult.setText("덜");
        }else{
            walkCountY.setText(String.valueOf(total));
        }

        walkD.closeData();
        //Log.i("키해시",getKeyHash(getApplicationContext())); // 키해시 얻는 방법
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 0:
                if(resultCode == 1){
                    EdtStart.setText(data.getStringExtra("sTxt"));
                }else if(resultCode == 2){
                    EdtFinish.setText(data.getStringExtra("fTxt"));
                }
                break;
            case 1:
                if(resultCode == 0){
                    walkCount_main.setText(data.getStringExtra("returnCount"));
                    String totalBack = data.getStringExtra("totalBack");
                    if(Integer.parseInt(totalBack) < 0){
                        walkCountY.setText(totalBack.substring(1));
                    }else{
                        walkCountY.setText(totalBack);
                    }
                }
                break;
            default:
                break;
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
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(event.values[0] > 8){
                walkCount_main.setText(String.valueOf(++num1));
                walkCountY.setText(String.valueOf(++total));

                if(num1-num2<0){
                    walkCountY.setText(String.valueOf(total).substring(1));
                    sumResult.setText("덜");
                }else{
                    sumResult.setText("더");
                }
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

    // 키해시 확인용
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
