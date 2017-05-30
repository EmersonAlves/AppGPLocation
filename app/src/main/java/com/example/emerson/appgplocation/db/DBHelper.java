package com.example.emerson.appgplocation.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Emerson on 17/04/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_NAME = "SGD_FV";
    private static int DB_VERSION = 1;

    private static String TABLE_USUARIO =
            "CREATE TABLE usuario(" +
                    "idusuario INTEGER PRIMARY KEY," +
                    "nome TEXT)";
    private static String TABLE_CONFIG =
            "CREATE TABLE config(" +
                    "idconfig INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "intervalo INTEGER)";

    private static String TABLE_MSG =
            "CREATE TABLE mensagens(" +
                    "idmsg INTEGER PRIMARY KEY," +
                    "idenviou INTEGER," +
                    "idrecebeu INTEGER," +
                    "msg TEXT)";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_USUARIO);
        db.execSQL(TABLE_CONFIG);
        db.execSQL(TABLE_MSG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}