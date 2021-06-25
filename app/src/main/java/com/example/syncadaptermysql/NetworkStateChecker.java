package com.example.syncadaptermysql;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.AuthProvider;
import java.util.HashMap;
import java.util.Map;

public class NetworkStateChecker extends BroadcastReceiver {
    private Context context;
    private SQLiteDataHelper db;

    @Override
    public  void onReceive(Context context, Intent intent){
        this.context = context;
        db = new SQLiteDataHelper(context);

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        //Si existe la conexión...
        if (activeNetwork != null) {
            //Si está conectado (wi-fi o datos)
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI || activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

                //Obtiene todos los datos no sincronizados
                Cursor cursor = db.getUnsyncedNames();
                if (cursor.moveToFirst()) {
                    do {
                        //Guarda los datos no sincronizados
                        saveName(
                                cursor.getInt(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_ID)),
                                cursor.getString(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_NAME)),
                                cursor.getString(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_PHONE))
                        );
                    } while (cursor.moveToNext());
                }
            }
        }
    }

    private void saveName(final int id, final String name, final String phone) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, MainActivity.URL_SAVE_DATA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (!obj.getBoolean("error")) {
                                //Actualizar el estado en sqlite
                                db.updateNameStatus(id, MainActivity.NAME_SYNCED_WITH_SERVER);

                                //Enviando la transmisión para actualizar la lista
                                context.sendBroadcast(new Intent(MainActivity.DATA_SAVED_BROADCAST));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            //Mapear los valores que se van a mandar y almacenar los parametros
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("phone", phone);
                return params;
            }
        };//Ejecutamos la clase para la libreria Volley
        VolleySingleton.getInstance(context).addToRequestQueue(stringRequest);
    }
}
