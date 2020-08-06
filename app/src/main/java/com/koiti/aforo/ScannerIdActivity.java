package com.koiti.aforo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.koiti.aforo.input.InputActivity;
import com.koiti.aforo.output.OutputActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity that displays the camera and detects pdf417 codes.
 */
public class ScannerIdActivity extends AppCompatActivity {

    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private ToneGenerator toneGenerator;
    private String barcodeData;
    private Context context;
    private String idNumber, surname, secondSurname, firstName, secondName, gender, birthDate, rh;
    private Boolean input;
    private SPData spData = new SPData();
    private ArrayList<String> idData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        context = this;

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        surfaceView = findViewById(R.id.surface_view);
        initialiseDetectorsAndSources();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initialiseDetectorsAndSources() {
        final BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.PDF417)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannerIdActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    if (barcodes.valueAt(0) != null) {

                        toneGenerator.startTone(ToneGenerator.TONE_SUP_PIP, 150);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 300 milliseconds
                        if (v != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                v.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //deprecated in API 26
                                v.vibrate(300);
                            }
                        }

                        class DataProcessing implements Runnable {

                            @Override
                            public void run() {
                                barcodeData = barcodes.valueAt(0).displayValue;
                                String alphaAndDigits = barcodeData.replaceAll("[^\\p{Alpha}\\p{Digit}+_]+", " ");
                                String[] splitStr = alphaAndDigits.split("\\s+");
                                Log.d("scanner", Arrays.toString(splitStr));

                                int corrimiento = 0;
                                Pattern pat = Pattern.compile("[A-Z]");
                                if (splitStr[2 + corrimiento].length() > 7) {
                                    corrimiento--;
                                }

                                Matcher match = pat.matcher(splitStr[3 + corrimiento]);
                                int lastCapitalIndex = -1;
                                if (match.find()) {
                                    lastCapitalIndex = match.start();
                                }

                                idNumber = splitStr[3 + corrimiento].substring(lastCapitalIndex - 10, lastCapitalIndex);
                                surname = splitStr[3 + corrimiento].substring(lastCapitalIndex);
                                secondSurname = splitStr[4 + corrimiento];
                                firstName = splitStr[5 + corrimiento];
                                secondName = splitStr[6 + corrimiento];
                                gender = splitStr[7 + corrimiento].contains("M") ? "M" : "F";
                                birthDate = splitStr[7 + corrimiento].substring(8, 10) + "/"
                                        + splitStr[7 + corrimiento].substring(6, 8) + "/"
                                        + splitStr[7 + corrimiento].substring(2, 6);
                                rh = splitStr[7 + corrimiento].substring(splitStr[7 + corrimiento].length() - 2);

                                idData.add(idNumber);
                                idData.add(surname);
                                idData.add(secondSurname);
                                idData.add(firstName);
                                idData.add(secondName);
                                idData.add(gender);
                                idData.add(birthDate);
                                idData.add(rh);

                                input = spData.getValueBoolean("input", context);

                                Intent intent;
                                if (input)
                                    intent = new Intent(context, InputActivity.class);
                                else
                                    intent = new Intent(context, OutputActivity.class);


                                intent.putStringArrayListExtra("Identification_data", idData);
                                startActivity(intent);
                                finish();
                            }
                        }

                        new Thread(new DataProcessing()).start();
                        barcodeDetector.release();
                    }
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }

}