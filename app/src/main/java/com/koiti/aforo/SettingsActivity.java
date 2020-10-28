package com.koiti.aforo;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Context context;
    private SPData spData = new SPData();
    private ArrayList<String> arrayListSpinner = new ArrayList<>();
    private TextInputEditText textServerIp;
    private TextInputEditText textServerSocketIp;
    private TextInputEditText textServerSocketPort;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;

        Button getBack = findViewById(R.id.btnBack);
        getBack.setOnClickListener(listener);

        Button logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(listener);

        boolean login = spData.getValueBoolean("login", context);

        if (!login)
            logout.setVisibility(View.GONE);
        else
            logout.setVisibility(View.VISIBLE);

        textServerIp = findViewById(R.id.serverIp);
        textServerIp.addTextChangedListener(textWatcher);
        textServerIp.setText(spData.getValueString("serverIp", context));

        textServerSocketIp = findViewById(R.id.serverSocketIp);
        textServerSocketIp.addTextChangedListener(textWatcher);
        textServerSocketIp.setText(spData.getValueString("socketIp", context));

        textServerSocketPort = findViewById(R.id.serverSocketPort);
        textServerSocketPort.addTextChangedListener(textWatcher);
        String port = String.valueOf(spData.getValueInt("socketPort", context));
        textServerSocketPort.setText(port);

        Spinner spinnerParkingLot = findViewById(R.id.spinnerParkingLot);
        spinnerParkingLot.setOnItemSelectedListener(this);

        try {
            File file = new File(getDir("data", MODE_PRIVATE), "jsonMap");
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
            ArrayList<HashMap> arrayListParkingLot = (ArrayList) inputStream.readObject();

            for (HashMap i : arrayListParkingLot)
                arrayListSpinner.add(String.valueOf(i.get("parqueadero_nombre")));


            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayListSpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            //load spinner with data
            spinnerParkingLot.setAdapter(adapter);

            spinnerParkingLot.setSelection(spData.getValueInt("parkingLotPosition", context));

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private View.OnClickListener listener = new View.OnClickListener() {
        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        public void onClick(View v) {
            // do something when the button is clicked
            switch (v.getId()) {
                case R.id.btnBack:
                    finish();
                    break;
                case R.id.btnLogout:
                    Logout logout = new Logout(context, SettingsActivity.this);
                    new Thread(logout).start();
                    break;
            }
        }
    };

    private final TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            if (Objects.requireNonNull(textServerIp.getText()).hashCode() == s.hashCode())
                spData.save(text, "serverIp", context);
            else if (Objects.requireNonNull(textServerSocketIp.getText()).hashCode() == s.hashCode())
                spData.save(text, "socketIp", context);
            else {
                int value = 0;
                if (!text.equals("")) {
                    value = Integer.parseInt(text);
                }
                spData.save(value, "socketPort", context);
            }
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        spData.save(arrayListSpinner.get(position), "parkingLot", context);
        spData.save(position, "parkingLotPosition", context);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
