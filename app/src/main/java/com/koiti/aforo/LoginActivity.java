package com.koiti.aforo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    private Context context;
    private EditText editTextUser, editTextPassword;
    private SPData spData = new SPData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUser = findViewById(R.id.editTextUser);
        editTextPassword = findViewById(R.id.editTextPassword);

        addListenerOnButton();

        // if there is a user who is logged in, starts the main activity
        boolean login = spData.getValueBoolean("login", context);
        if (login) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void addListenerOnButton() {
        Button login = findViewById(R.id.btnLogin);
        Button settings = findViewById(R.id.btnSettingsLogin);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = String.valueOf(editTextUser.getText());
                String password = String.valueOf(editTextPassword.getText());

                File file = new File(getDir("data", MODE_PRIVATE), "jsonMap");
                ProcessLogin processLogin = new ProcessLogin(context, user, password, LoginActivity.this, file);
                new Thread(processLogin).start();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });
    }
}
