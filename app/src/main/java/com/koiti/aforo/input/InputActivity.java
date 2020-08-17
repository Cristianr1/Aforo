package com.koiti.aforo.input;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.koiti.aforo.Available;
import com.koiti.aforo.MainActivity;
import com.koiti.aforo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class InputActivity extends AppCompatActivity {
    private Context context;
    private ArrayList<String> idData;
    private List<EditText> editTextsDynamically = new ArrayList<>();

    private TextInputEditText textDocument, textSurname, textSecondSurname, textFirstName,
            textSecondName, textGender, textDateBirth, textPhone, textAddress, textTemperature, textNumChildren;

    private String phone, address, temperature, numChildren;
    private Available available;
    private LinearLayout linearLayout;
    private TextView textOccupation, textAvailable;
    private Button getIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        context = this;

        phone = address = temperature = numChildren = "";

        textDocument = findViewById(R.id.document);
        textSurname = findViewById(R.id.surname);
        textSecondSurname = findViewById(R.id.secondsurname);
        textFirstName = findViewById(R.id.firstname);
        textSecondName = findViewById(R.id.secondname);
        textGender = findViewById(R.id.gender);
        textDateBirth = findViewById(R.id.birth);
        textPhone = findViewById(R.id.phone);
        textTemperature = findViewById(R.id.temperature);
        textAddress = findViewById(R.id.address);
        textNumChildren = findViewById(R.id.children);

        textOccupation = findViewById(R.id.occupation);
        textAvailable = findViewById(R.id.available);

        linearLayout = findViewById(R.id.editTextContainer);

        ImageView home = findViewById(R.id.home);
        home.setOnClickListener(listener);

        getIn = findViewById(R.id.getIn);
        getIn.setOnClickListener(listener);

        textNumChildren.addTextChangedListener(textWatcher);

        populateEditText();

        available = new Available(context, textAvailable, textOccupation);
        Thread threadAvailable = new Thread(available);
        threadAvailable.start();
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intentNav;
            switch (v.getId()) {
                case R.id.getIn:
                    Log.d("get in", "button pressed");
                    phone = String.valueOf(textPhone.getText());
                    temperature = String.valueOf(textTemperature.getText());
                    address = String.valueOf(textAddress.getText());
                    numChildren = String.valueOf(textNumChildren.getText());

                    //indicates if all dynamicallyComplete have been filled
                    boolean dynamicallyComplete = true;
                    for (EditText i : editTextsDynamically) {
                        if (i.getText().toString().equals(""))
                            dynamicallyComplete = false;
                    }

                    if (!phone.equals("") && !temperature.equals("") && !address.equals("") && !numChildren.equals("") && dynamicallyComplete) {
                        if (idData.size() > 8) {
                            idData.set(8, phone);
                            idData.set(9, temperature);
                            idData.set(10, address);
                            idData.set(11, numChildren);
                        } else {
                            idData.add(phone);
                            idData.add(temperature);
                            idData.add(address);
                            idData.add(numChildren);
                        }

                        getIn.setEnabled(false);
                        UploadInput uploadInput = new UploadInput(context, idData, editTextsDynamically, InputActivity.this, available);
                        new Thread(uploadInput).start();
                    } else {
                        Toasty.error(InputActivity.this, "Por favor complete el formulario", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.home:
                    intentNav = new Intent(context, MainActivity.class);
                    startActivity(intentNav);
                    finish();
                    available.stop();
                    break;
            }
        }
    };

    private void populateEditText() {
        idData = getIntent().getStringArrayListExtra("Identification_data");
        if (idData != null) {
            textDocument.setText(idData.get(0));
            textSurname.setText(idData.get(1));
            textSecondSurname.setText(idData.get(2));
            textFirstName.setText(idData.get(3));
            textSecondName.setText(idData.get(4));
            textGender.setText(idData.get(5));
            textDateBirth.setText(idData.get(6));
        }
    }

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
            if (Objects.requireNonNull(textNumChildren.getText()).hashCode() == s.hashCode()) {
                int numChildren = 0;
                String text = s.toString();
                if (!text.equals("")) {
                    numChildren = Integer.parseInt(text);
                }
                linearLayout.removeAllViews();
                addEditText(numChildren);
            }
        }
    };


    /**
     * This function dynamically add EditText elements in the view
     *
     * @param numEditText The number of EditText that will be added.
     */
    private void addEditText(int numEditText) {
        editTextsDynamically.clear();
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(4);
        // Create EditText
        for (int i = 0; i < numEditText; i++) {
            TextInputEditText editText = new TextInputEditText(context);
            editTextsDynamically.add(editText);
            editText.setTextSize(14);
            editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL);
            editText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            editText.setPadding(20, 20, 20, 20);
            editText.setHint("Temperatura Niño " + (i + 1));
            editText.setFilters(FilterArray);
            editText.addTextChangedListener(textWatcher);
            // Add EditText to LinearLayout
            if (linearLayout != null) {
                linearLayout.addView(editText);
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        available = new Available(context, textAvailable, textOccupation);
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
