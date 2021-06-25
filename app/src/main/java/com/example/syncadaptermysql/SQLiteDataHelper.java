package com.example.syncadaptermysql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteDataHelper  extends SQLiteOpenHelper {
    //Constantes para el nombre de la base de datos, el nombre
    // de la tabla y los nombres de las columnas
    public static final String DB_NAME = "ContactosDB";
    public static final String TABLE_NAME = "usuario";
    public static final String COLUMN_ID = "id_usuario";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_STATUS = "status";
    //Versión de la BD
    private static final int DB_VERSION = 1;
    //Constructor de la BD
    public SQLiteDataHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    //Creamos la BD
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NAME
                + "(" + COLUMN_ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME +
                " VARCHAR, " + COLUMN_PHONE +
                " VARCHAR, " + COLUMN_STATUS +
                " TINYINT);";
        db.execSQL(sql);
    }

    //Actualizar la base de datos
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS Persons";
        db.execSQL(sql);
        onCreate(db);
    }
    //Este método toma dos argumentos, el primero es el nombre y teléfono que se guardará,
    // el segundo es el estado 0 significa que el nombre y teléfono está sincronizado con el
    // servidor 1 significa que el nombre y teléfono no está sincronizado con el servidor
    public boolean addName(String name, String telefono, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE, telefono);
        contentValues.put(COLUMN_STATUS, status);

        db.insert(TABLE_NAME, null, contentValues);
        db.close();
        return true;
    }
    //Este método toma dos argumentos, el primero es el id del nombre  y teléfono para el
    // cual tenemos que actualizar el estado de sincronización y el segundo es
    // el estado que se cambiará
    public boolean updateNameStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_STATUS, status);
        db.update(TABLE_NAME, contentValues, COLUMN_ID + "=" + id, null);
        db.close();
        return true;
    }
    //Este método nos dará todo el nombre y teléfono almacenado en sqlite
    public Cursor getNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMN_ID + " ASC;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
    //este método es para obtener todos los nombres y teléfonos no sincronizados
    //para que podamos sincronizarlo con la base de datos
    public Cursor getUnsyncedNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_STATUS + " = 0;";
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}
