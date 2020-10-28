package com.koiti.aforo.output;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.koiti.aforo.Available;
import com.koiti.aforo.MainActivity;
import com.koiti.aforo.R;

import java.util.ArrayList;

public class OutputActivity extends AppCompatActivity {
    private Context context;
    private String document;
    private Available available;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        context = this;

        TextView tvDocument = findViewById(R.id.documentOutput);

        ImageView home = findViewById(R.id.homeOutput);
        home.setOnClickListener(listener);

        Button getOut = findViewById(R.id.btnGetOut);
        getOut.setOnClickListener(listener);

        ArrayList<String> idData = getIntent().getStringArrayListExtra("Identification_data");

        if (idData != null) {
            tvDocument.setText(idData.get(0));
            document = idData.get(0);
        }

        available = new Available(context);
        Thread threadAvailable = new Thread(available);
        threadAvailable.start();
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
                    available.stop();
                    break;
            }
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        available = new Available(context);
        Thread threadAvailable = new Thread(available);
        threadAvailable.start();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
        available.stop();
    }
}
