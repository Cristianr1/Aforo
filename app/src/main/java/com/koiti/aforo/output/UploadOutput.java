package com.koiti.aforo.output;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.koiti.aforo.SPData;
import com.koiti.aforo.ScannerIdActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import es.dmoral.toasty.Toasty;

public class UploadOutput implements Runnable {

    private Context context;
    private Activity activity;
    private String document;

    UploadOutput(Context context, String document, Activity activity) {
        this.context = context;
        this.document = document;
        this.activity = activity;
    }

    @Override
    public void run() {
        SPData spData = new SPData();
        String ip = spData.getValueString("serverIp", context);
        String parkingLot = "";

        try {
            parkingLot = URLEncoder.encode(spData.getValueString("parkingLot", context), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://" + ip + "/parkline/public/api/salidas?" +
                "parqueadero=" + parkingLot +
                "&registro_documento=" + document;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d("Response is: ", response);
                        Intent intent = new Intent(context, ScannerIdActivity.class);
                        context.startActivity(intent);
                        activity.finish();
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
                else {
                    Log.d("Sin conexión", "UploadOutput");
                    Toasty.error(context, "Sin conexión.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
