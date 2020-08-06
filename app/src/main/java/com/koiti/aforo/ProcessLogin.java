package com.koiti.aforo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;


/**
 * This thread requests login, and extracts the information that comes in the request response
 */
public class ProcessLogin implements Runnable {
    private Context context;
    private String user, password;
    private Activity activity;
    private SPData spData = new SPData();
    private File file;

    ProcessLogin(Context context, String user, String password, Activity activity, File file) {
        this.context = context;
        this.user = user;
        this.password = password;
        this.activity = activity;
        this.file = file;
    }

    @Override
    public void run() {
        RequestQueue queue = Volley.newRequestQueue(context);
        String ip = spData.getValueString("serverIp", context);
        String encodedPassword = password;
        try {
            encodedPassword = URLEncoder.encode(password, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "http://" + ip + "/parkline/public/api/login?username=" + user + "&password=" + encodedPassword;


        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONArray>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("Response: ", response.toString());
                        try {
                            JSONObject jsonObject = response.getJSONObject(0);
                            HashMap<String, Object> generalDataMap = new Gson().fromJson(
                                    jsonObject.toString(), HashMap.class);

                            int userId = (int) ((double) generalDataMap.get("id"));
                            String token = String.valueOf(generalDataMap.get("api_token"));
                            spData.save(userId, "userId", context);
                            spData.save(token, "token", context);

                            JSONArray jsonArrayParkingLot = jsonObject.getJSONArray("parqueaderos");
                            jsonArrayParkingLot = jsonArrayParkingLot.getJSONArray(0);

                            //arrayListParkingLot will store the map coming from jsonArrayParkingLot
                            ArrayList<HashMap> arrayListParkingLot = new ArrayList<>();

                            for (int i = 0; i < jsonArrayParkingLot.length(); i++) {
                                HashMap<String, Object> userDataMap = new Gson().fromJson(
                                        jsonArrayParkingLot.getJSONObject(i).toString(), HashMap.class);
                                arrayListParkingLot.add(userDataMap);
                            }

                            //Persist the arrayListParkingLot in local storage
                            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
                            outputStream.writeObject(arrayListParkingLot);
                            outputStream.flush();
                            outputStream.close();

                            spData.save(true, "login", context);

                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                            activity.finish();
                        } catch (JSONException | FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.d("Response: ", error.toString());

                        if (error.networkResponse != null)
                            try {
                                String responseBody = new String(error.networkResponse.data, "utf-8");
                                JSONObject data = new JSONObject(responseBody);
                                HashMap<String, Object> occupationMap = new Gson().fromJson(data.toString(), HashMap.class);
                                Toasty.error(context, String.valueOf(occupationMap.get("message")), Toast.LENGTH_SHORT).show();
                            } catch (UnsupportedEncodingException | JSONException e) {
                                e.printStackTrace();
                            }
                        else
                            Toasty.error(context, "No fue posible establecer conexión con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });

        // Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
}
