package cs.inhatc.sensor.smart_sensor_project;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class WalkingViewer extends AppCompatActivity implements SensorEventListener{

    private SensorManager objSMG;
    private Sensor sensor_Accelerometer;

    private WalkData walkD;

    private BarChart barChart;
    private int chartLine, num, total;

    private TextView walkCount_walking;

    //그래프 그리기 위한 ArrayList
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();

    private Button btnReturn;
    private Cursor cursor;

    /*private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;*/

    //가속도 센서 Delay
    /*private static final int SHAKE_THRESHOLD = 800;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_walking);

        Intent intentT = getIntent();
        total = Integer.parseInt(intentT.getStringExtra("total"));
        barChart = findViewById(R.id.chart);
        btnReturn = findViewById(R.id.btnReturn);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBack = new Intent();
                intentBack.putExtra("returnCount",walkCount_walking.getText());
                intentBack.putExtra("totalBack",String.valueOf(total));
                setResult(0, intentBack);
                WalkingViewer.this.finish();
            }
        });

        walkCount_walking = findViewById(R.id.walkCount_walking);
        walkD = new WalkData(this);

        objSMG = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor_Accelerometer = objSMG.getDefaultSensor(TYPE_ACCELEROMETER);

        cursor = walkD.selectWData(7);
        selectData();
        chartSet();
    }

    private void intentStartContext(){
        Intent intent = new Intent(WalkingViewer.this, KakaoMapViewer.class);
        startActivity(intent);
    }

    private void selectData(){

        while (cursor.moveToNext())
        {
            entries.add(new BarEntry(chartLine, cursor.getInt(1)));
            labels.add(cursor.getString(2));
            chartLine++;

            if(WalkData.getDate().matches(cursor.getString(2)))
            {
                walkCount_walking.setText(cursor.getString(1));
                num = Integer.parseInt(cursor.getString(1));
            }
        }
        walkD.closeData();
    }

    private void chartSet(){
        BarDataSet barDataSet = new BarDataSet(entries, null);
        barDataSet.setDrawValues(false);
        barDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        barDataSet.setDrawValues(!barDataSet.isDrawValuesEnabled());
        barDataSet.setValueTextSize(15);

        BarData barData = new BarData(barDataSet);
        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.getLegend().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));

        YAxis yLAxis = barChart.getAxisLeft();
        yLAxis.setTextColor(Color.BLACK);
        yLAxis.setTextSize(10);
        yLAxis.setAxisMinimum(0f);
        yLAxis.setDrawGridLines(true);

        YAxis yRAxis = barChart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDescription(description);
        barChart.setNoDataText("걸음걸이 데이터가 없습니다.");
        barChart.setNoDataTextColor(Color.BLACK);
        barChart.animateY(2000, Easing.EasingOption.EaseInCubic);
        barChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();

        objSMG.registerListener(this, sensor_Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(event.values[0] > 8){
                walkCount_walking.setText(String.valueOf(++num));
                total++;
            }
        }
        walkD.insertAupdateWData(walkCount_walking);
        walkD.closeData();

        /*if (event.sensor.getType() == TYPE_ACCELEROMETER)
        {
            long currentTime = System.currentTimeMillis();
            long gabOfTime = (currentTime - lastTime);
            if (gabOfTime > 100) {
                lastTime = currentTime;

                speed = Math.abs(event.values[0] + event.values[1] + event.values[2] - lastX - lastY - lastZ) / gabOfTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    moveCount++;
                    walkCount.setText(moveCount);
                }

                lastX = event.values[0];
                lastY = event.values[1];
                lastZ = event.values[2];
            }
        }*/
    }

    @Override
    public void onBackPressed() {
        intentStartContext();
        WalkingViewer.this.finish();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
