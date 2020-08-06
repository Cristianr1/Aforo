package com.koiti.aforo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

/**
 * Main activity that manages the use that will be given to the app,
 * the two possible options are input and output.
 */
public class MainActivity extends AppCompatActivity {

    private Context context;
    private SPData spData = new SPData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        CardView btnInput = findViewById(R.id.btnInput);
        CardView btnOutput = findViewById(R.id.btnOutput);
        ImageView btnSettings = findViewById(R.id.btnSettingsMain);

        btnInput.setOnClickListener(listener);
        btnOutput.setOnClickListener(listener);
        btnSettings.setOnClickListener(listener);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        public void onClick(View v) {

            // do something when the button is clicked
            Intent intentNav = null;
            switch (v.getId()) {
                case R.id.btnInput:
                    intentNav = new Intent(context, ScannerIdActivity.class);
                    spData.save(true, "input", context);
                    finish();
                    break;
                case R.id.btnOutput:
                    intentNav = new Intent(context, ScannerIdActivity.class);
                    spData.save(false, "input", context);
                    finish();
                    break;
                case R.id.btnSettingsMain:
                    intentNav = new Intent(context, SettingsActivity.class);
                    break;
            }
            startActivity(intentNav);
        }
    };
}