package com.example.emerson.appgplocation.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


import com.example.emerson.appgplocation.model.Configuracao;
import com.example.emerson.appgplocation.model.Usuario;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by Emerson on 17/04/2017.
 */

public class DBManager {
    private static DBHelper dbHelper = null;

    public DBManager(Context context){
        if(dbHelper == null){
            dbHelper = new DBHelper(context);
        }
    }

    public void inserirUsuario(Usuario usuario){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idusuario",usuario.getIdUsuario());
        values.put("nome",usuario.getNome());
        db.insert("usuario",null,values);

    }

    public void inserirConfig(Configuracao configuracao){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("intervalo",configuracao.getIntervalo());
        db.insert("config",null,values);
    }

    public void atualizarConfig(Configuracao configuracao){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("intervalo",configuracao.getIntervalo());
        db.update("config",values,"idconfig="+configuracao.getIntervalo(),null);
    }

    public void deleteUsuario(Usuario usuario){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("idusuario",usuario.getIdUsuario());
        values.put("nome",usuario.getNome());
        db.delete("usuario",null,null);

    }
    public List<Usuario> getListaUsuarios(){
        String sql = "SELECT * FROM usuario";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        List<Usuario> usuarios = new ArrayList<>();

        if(cursor != null){
            while (cursor.moveToNext()){
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(cursor.getLong(0));
                usuario.setNome(cursor.getString(1));
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }
    public Configuracao getConfig(Long id){
        String sql = "SELECT * FROM config WHERE idconfig = '"+id+"'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);
        Configuracao configuracao = null;
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            configuracao = new Configuracao();
            configuracao.setIdConfig(cursor.getLong(0));
            configuracao.setIntervalo(cursor.getInt(1));
        }
        return configuracao;
    }
    public Usuario getUsuario(Long id){
        String sql = "SELECT * FROM usuario WHERE idusuario = '"+id+"'";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);
        Usuario usuario = new Usuario();
        if(cursor.getCount() > 0){
            cursor.moveToFirst();
            usuario.setIdUsuario(cursor.getLong(0));
            usuario.setNome(cursor.getString(1));
        }
        return usuario;
    }
}
