package cs.inhatc.sensor.smart_sensor_project;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;

public class WalkingViewer extends AppCompatActivity implements SensorEventListener{

    private long lastTime;
    private float speed;
    private float lastX;
    private float lastY;
    private float lastZ;

    private BarChart barChart;
    private int chartLine;

    //그래프 그리기 위한 ArrayList
    ArrayList<BarEntry> entries = new ArrayList<>();
    ArrayList<String> labels = new ArrayList<String>();

    //가속도 센서 Delay
    private static final int SHAKE_THRESHOLD = 800;

    WalkData walkData;
    SQLiteDatabase sql;

    TextView walkCount;

    Button btnBack;

    int moveCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_walking);

        walkCount = (TextView)findViewById(R.id.walkCount);
        barChart = (BarChart)findViewById(R.id.chart);

        walkData = new WalkData(this);
        btnBack = findViewById(R.id.button2);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalkingViewer.this.finish();
            }
        });

        sql = walkData.getWritableDatabase();

        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM WalkHistory ORDER BY WalkHistory._id DESC Limit 7;", null);

        while (cursor.moveToNext())
        {
            entries.add(new BarEntry(chartLine, cursor.getInt(1)));
            labels.add(cursor.getString(2));
            chartLine++;

            if(KakaoMapViewer.getDate().matches(cursor.getString(2)))
            {
                walkCount.setText(cursor.getString(1));
                moveCount = Integer.parseInt(cursor.getString(1));
            }
        }

        cursor.close();
        sql.close();

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
    protected void onDestroy() {
        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd",Locale.KOREA);
        String date = fm1.format(new Date());
        sql = walkData.getWritableDatabase();
        sql.execSQL("INSERT INTO WalkHistory SELECT NULL,0,'"+date+"' WHERE NOT EXISTS(SELECT WalkHistory.Testdate FROM WalkHistory WHERE WalkHistory.Testdate='"+date+"');");

        sql.execSQL("UPDATE WalkHistory SET walk_cnt="+Integer.parseInt(""+walkCount.getText())+" WHERE WalkHistory.Testdate='"+date+"';");

        sql.close();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == TYPE_ACCELEROMETER)
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
