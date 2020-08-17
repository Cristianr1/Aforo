package com.koiti.aforo;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

/**
 * Thread that queries the occupation and the available space, and set these values in the view.
 */
public class Available implements Runnable {

    private Context context;
    private TextView textAvailable, textOccupation;
    private volatile boolean query = true;

    public Available(Context context, TextView textAvailable, TextView textOccupation) {
        this.context = context;
        this.textAvailable = textAvailable.findViewById(R.id.available);
        this.textOccupation = textOccupation.findViewById(R.id.occupation);
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

        String url = "http://" + ip + "/parkline/public/api/ocupacion?parqueadero=" + parkingLot;

        while (query) {
            RequestQueue queue = Volley.newRequestQueue(context);

            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            Log.d("Response: ", response.toString());
                            try {
                                JSONObject jsonObject = response.getJSONObject(0);
                                HashMap<String, Object> occupationMap = new Gson().fromJson(jsonObject.toString(), HashMap.class);
                                int numOccupation = (int) ((double) occupationMap.get("ocupacion"));
                                int numAvailableSpace = (int) ((double) occupationMap.get("disponible"));
                                String availableSpace = Integer.toString(numAvailableSpace);
                                String occupation = Integer.toString(numOccupation);
                                textAvailable.setText(availableSpace);
                                textOccupation.setText(occupation);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            Log.d("Response: ", error.toString());
                            textAvailable.setText("Sin dato");
                            textOccupation.setText("Sin dato");
                            if (error.networkResponse != null)
                                try {
                                    String responseBody = new String(error.networkResponse.data, "utf-8");
                                    JSONObject data = new JSONObject(responseBody);
                                    HashMap<String, Object> occupationMap = new Gson().fromJson(data.toString(), HashMap.class);
                                    Toasty.error(context, String.valueOf(occupationMap.get("message")), Toast.LENGTH_SHORT).show();
                                    stop();
                                } catch (UnsupportedEncodingException | JSONException e) {
                                    e.printStackTrace();
                                    Toasty.error(context, "El parqueadero no ha sido configurado.", Toast.LENGTH_SHORT).show();
                                }
                            else
                                Toasty.error(context, "Sin conexión.", Toast.LENGTH_SHORT).show();
                        }
                    });

            // Add the request to the RequestQueue.
            queue.add(jsonArrayRequest);

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        query = false;
        Log.d("hilo", "detenido");
    }
}
