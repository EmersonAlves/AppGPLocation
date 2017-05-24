package com.example.emerson.appgplocation;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import com.example.emerson.appgplocation.model.EnviarUsuario;
import com.example.emerson.appgplocation.model.Usuario;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

public class ActivityCliente extends AppCompatActivity {

    private EditText etNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente);

        etNome = (EditText)findViewById(R.id.etNome);
        String url = getIntent().getStringExtra("url");

        EnviarUsuario enviarUsuario = new EnviarUsuario();
        enviarUsuario.setUrl(url);
        enviarUsuario.setUsuario(new Usuario());
        new EnviarDadosJsonAsyncTask().execute(enviarUsuario);

    }

    class EnviarDadosJsonAsyncTask extends AsyncTask<EnviarUsuario, Void, Usuario> {
        private Dialog dialog;
        @Override
        protected Usuario doInBackground(EnviarUsuario... params) {
            Usuario usuario = params[0].getUsuario();
            try {
                String urlString = params[0].getUrl();
                HttpContext localContext = new BasicHttpContext();
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlString);
                post.setHeader("Content-type", "application/json");
                //post.setHeader("Authorization",token);
                //post.setHeader("Cookie","ASP.NET_SessionId="+sessao+"; path=/; HttpOnly");
                JSONObject obj = new JSONObject();
                StringEntity se = new StringEntity(obj.toString());
                post.setEntity(se);
                HttpResponse response = client.execute(post, localContext);

                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();

                String resultString = getStringFromInputStream(instream);
                instream.close();

                usuario = getUsuario(usuario,resultString);

                return usuario;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(ActivityCliente.this, "Aguarde",
                    "Autenticando...");
        }

        @Override
        protected void onPostExecute(Usuario result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result.getIdUsuario() != null) {
                if (result.getIdUsuario() > 0) {
                    etNome.setText(result.getNome());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ActivityCliente.this).setTitle("Atenção")
                            .setMessage("Usuario Não Encontrado !")
                            .setPositiveButton("OK", null);
                    builder.create().show();
                    finish();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        ActivityCliente.this).setTitle("Atenção")
                        .setMessage("Error na conexão com servidor  !")
                        .setPositiveButton("OK", null);
                builder.create().show();
            }
        }
        private Usuario getUsuario(Usuario usuario,String jsonString) {
            try {
                JSONObject usuarioLists = new JSONObject(jsonString);
                usuario.setIdUsuario(usuarioLists.getLong("idusuario"));
                usuario.setNome(usuarioLists.getString("nome"));
            } catch (JSONException e) {
                Log.e("Error", "Erro no parsing do JSON", e);
            }

            return usuario;
        }
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }
}
