package com.koiti.aforo.input;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.koiti.aforo.Available;
import com.koiti.aforo.ScannerIdActivity;

public class InputAlert {
    private Context context;
    private Activity activity;
    private Available available;

    InputAlert(Context context, Available available, Activity activity) {
        this.context = context;
        this.available = available;
        this.activity = activity;
    }

    public AlertDialog createSuccessAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle("Ingreso exitoso")
                .setMessage("El documento ha sido registrado con éxito.")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, ScannerIdActivity.class);
                                context.startActivity(intent);
                                activity.finish();
                                available.stop();
                            }
                        });
        return builder.create();
    }
}
