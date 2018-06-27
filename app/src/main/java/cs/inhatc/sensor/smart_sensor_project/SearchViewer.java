package cs.inhatc.sensor.smart_sensor_project;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchViewer extends AppCompatActivity implements View.OnClickListener {

    Button btnOk, btnBack;
    EditText EdtSearch;
    ListView listV;
    String Txt,FlagTxt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_searchlocation);

        btnOk = findViewById(R.id.btnOk);
        btnBack = findViewById(R.id.btnBack);
        EdtSearch = findViewById(R.id.edtSearch);
        listV = findViewById(R.id.listV);

        Intent intentGet = getIntent();
        Txt = intentGet.getStringExtra("Txt"); // 맨 마지막에 "s" 혹은 "f"를 붙여서 가져오는 문자열
        FlagTxt = Txt.substring(Txt.length()-1); // 마지막 식별문자를 자른다.
        Txt = Txt.substring(0,Txt.length()-1);

        if(Txt.equals("s") || Txt.equals("f")){
            EdtSearch.setText("");
        }else{
            EdtSearch.setText(Txt);
        }

        btnOk.setOnClickListener(this);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btnOk){
            if(EdtSearch.getText().length() != 0){
                Intent intent = new Intent();
                if(FlagTxt.equals("s")){
                    intent.putExtra("sTxt",String.valueOf(EdtSearch.getText()));
                    setResult(1, intent);
                }else{
                    intent.putExtra("fTxt",String.valueOf(EdtSearch.getText()));
                    setResult(2, intent);
                }
                SearchViewer.this.finish();
            }else{
                Toast.makeText(this, "결과 값이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        }else if(v.getId() == R.id.btnBack){
            SearchViewer.this.finish();
        }

    }

    public void SearchLoca(){

    }
}
