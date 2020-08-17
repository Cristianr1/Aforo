package com.koiti.aforo.output;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.koiti.aforo.MainActivity;
import com.koiti.aforo.R;

import java.util.ArrayList;

public class OutputActivity extends AppCompatActivity {
    private Context context;
    private TextView tvDocument;
    private ArrayList<String> idData;
    private String document;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        context = this;

        tvDocument = findViewById(R.id.documentOutput);

        ImageView home = findViewById(R.id.homeOutput);
        home.setOnClickListener(listener);

        Button getOut = findViewById(R.id.btnGetOut);
        getOut.setOnClickListener(listener);

        idData = getIntent().getStringArrayListExtra("Identification_data");

        if (idData != null) {
            tvDocument.setText(idData.get(0));
            document = idData.get(0);
        }
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intentNav;
            switch (v.getId()) {
                case R.id.btnGetOut:
                    UploadOutput uploadOutput = new UploadOutput(context, document, OutputActivity.this);
                    new Thread(uploadOutput).start();
                    break;
                case R.id.homeOutput:
                    intentNav = new Intent(context, MainActivity.class);
                    startActivity(intentNav);
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
