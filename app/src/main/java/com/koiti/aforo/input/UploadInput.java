package com.koiti.aforo.input;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.koiti.aforo.Available;
import com.koiti.aforo.SPData;
import com.koiti.aforo.ScannerIdActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class UploadInput implements Runnable {

    private ArrayList<String> idData;
    private List<EditText> editTextsDynamically;
    private Context context;
    private Activity activity;
    private Available available;

    UploadInput(Context context, ArrayList<String> idData, List<EditText> editTextsDynamically, Activity activity, Available available) {
        this.context = context;
        this.idData = idData;
        this.editTextsDynamically = editTextsDynamically;
        this.activity = activity;
        this.available = available;
    }

    @Override
    public void run() {
        final SPData spData = new SPData();
        String ip = spData.getValueString("serverIp", context);
        String parkingLot = "";
        int userId = spData.getValueInt("userId", context);

        try {
            parkingLot = URLEncoder.encode(spData.getValueString("parkingLot", context), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int numChildren = Integer.parseInt(idData.get(11));

        StringBuilder temperatureChildren = new StringBuilder();

        if (numChildren > 0) {
            for (int i = 0; i < editTextsDynamically.size() - 1; i++) {
                temperatureChildren.append(editTextsDynamically.get(i).getText()).append(";");
            }
            temperatureChildren.append(editTextsDynamically.get(editTextsDynamically.size() - 1).getText());
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://" + ip + "/parkline/public/api/entradas?" +
                "parqueadero=" + parkingLot +
                "&registro_documento=" + idData.get(0) +
                "&registro_nombre1=" + idData.get(3) +
                "&registro_nombre2=" + idData.get(4) +
                "&registro_apellido1=" + idData.get(1) +
                "&registro_apellido2=" + idData.get(2) +
                "&registro_sexo=" + idData.get(5) +
                "&registro_fecha=" + idData.get(6) +
                "&registro_sanguineo=" + idData.get(7) +
                "&registro_telefono=" + idData.get(8) +
                "&registro_direccion=" + idData.get(10) +
                "&registro_temperatura=" + idData.get(9) +
                "&registro_menores=" + idData.get(11) +
                "&registro_temperatura_menores=" + temperatureChildren +
                "&usuario=" + userId;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response is: ", response);
                        InputAlert inputAlert = new InputAlert(context, available, activity);
                        AlertDialog successAlert = inputAlert.createSuccessAlert();
                        successAlert.show();
                        successAlert.setCanceledOnTouchOutside(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Response is:", error.toString());
                if (error.networkResponse != null)
                    try {
                        String responseBody = new String(error.networkResponse.data, "utf-8");
                        JSONObject data = new JSONObject(responseBody);
                        String message = data.getString("message");
                        Toasty.error(context, message, Toast.LENGTH_SHORT).show();
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                else
                    Log.d("Sin conexión", "UploadInput");
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
