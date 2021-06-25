package com.example.syncadaptermysql;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Variable para guardar los valores con la direccion IP y archivo alojado
    //en el servidor de XAMPP
    public static final String URL_SAVE_DATA = "http://192.168.52.1/WebService/save.php";
    SQLiteDataHelper db;//objeto para la bd
    //Creación de valiables para los objetos
    Button btnGuardar;
    EditText etNombre;
    EditText etTelefono;
    ListView lvContactos;
    //Lista para almacenar todos los nombres
    private List<Name> names;


    //1 significa que los datos estan sincronizados y 0 que no
    public static final int NAME_SYNCED_WITH_SERVER = 1;
    public static final int NAME_NOT_SYNCED_WITH_SERVER = 0;

    //Un receptor para saber si los datos estan sincronizados o no
    public static final String DATA_SAVED_BROADCAST = "com.ricardo.datasaved";

    //Broadcast receiver para saber si el status de la sincronización
    private BroadcastReceiver broadcastReceiver;

    //Adapterobject para el ListView
    private NameAdapter nameAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver(new NetworkStateChecker(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        //Inicialización de la instancia de la BD
        db = new SQLiteDataHelper(this);
        names = new ArrayList<>();
        //Linkeo de controladores
        btnGuardar = (Button) findViewById(R.id.btnGuardar);
        etNombre = (EditText) findViewById(R.id.etNombre);
        etTelefono = (EditText) findViewById(R.id.etTelefono);
        lvContactos= (ListView) findViewById(R.id.lvContactos);

        //Oyente del botón Guardar
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNameToServer();
            }
        });

        //Lamada al método para cargar todos los nombres almacenados
        loadNames();

        // El receptor de transmisión para actualizar el estado de sincronización
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Cargando los nombres y teléfonos de nuevo
                loadNames();
            }
        };

        //Registrando el broadcast receiver para actualizar el status de la sincronización
        registerReceiver(broadcastReceiver, new IntentFilter(DATA_SAVED_BROADCAST));
    }
    //este método cargará los nombres de la base de datos con
    // el estado de sincronización actualizado
    private void loadNames() {
        names.clear();
        Cursor cursor = db.getNames();
        if (cursor.moveToFirst()) {
            do {
                Name name = new Name(
                        cursor.getString(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_PHONE)),
                        cursor.getInt(cursor.getColumnIndex(SQLiteDataHelper.COLUMN_STATUS))
                );
                names.add(name);
            } while (cursor.moveToNext());
        }

        nameAdapter = new NameAdapter(this, R.layout.names, names);
        lvContactos.setAdapter(nameAdapter);
    }

    //Este método simplemente actualizará la lista
    private void refreshList() {
        nameAdapter.notifyDataSetChanged();
    }
    //Este método es guardar el nombre en el servidor
    private void saveNameToServer() {
        final ProgressDialog progressDialog = new ProgressDialog(this);

        final String name = etNombre.getText().toString().trim();
        final String phone = etTelefono.getText().toString().trim();

        if (phone.isEmpty() || name.isEmpty()){
            Toast.makeText(getApplicationContext(), "No puede dejar campos vacios", Toast.LENGTH_SHORT).show();
        }
        else {
            progressDialog.setMessage("Saving Name...");
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SAVE_DATA,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressDialog.dismiss();
                            try {
                                JSONObject obj = new JSONObject(response);
                                if (!obj.getBoolean("error")) {
                                    // si hay un exito
                                    // almacenando el nombre en sqlite con estado sincronizado
                                    saveNameToLocalStorage(name, phone, NAME_SYNCED_WITH_SERVER);
                                } else {
                                    //Si no guarda exitosamente se almacena en la BD
                                    // SQLite con el status no sincronizado
                                    saveNameToLocalStorage(name, phone, NAME_NOT_SYNCED_WITH_SERVER);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressDialog.dismiss();
                            //En caso de error al almacenar el nombre en sqlite con estado no sincronizado
                            saveNameToLocalStorage(name, phone, NAME_NOT_SYNCED_WITH_SERVER);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("name", name);
                    params.put("phone", phone);
                    return params;
                }
            };
            //Ejecutamos la clase para la libreria Volley
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
    }//Mapear los valores que se van a mandar y almacenar los parametros
    private void saveNameToLocalStorage(String name, String phone, int status) {
        etNombre.setText("");
        etTelefono.setText("");
        db.addName(name, phone, status);
        Name n = new Name(name, phone, status);
        names.add(n);
        refreshList();
    }
}
