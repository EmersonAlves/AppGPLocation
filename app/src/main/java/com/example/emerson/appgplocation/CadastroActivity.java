package com.example.emerson.appgplocation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.example.emerson.appgplocation.model.Usuario;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class CadastroActivity extends AppCompatActivity {

    private EditText etNome;
    private EditText etUser;
    private EditText etSenha;
    private Switch swTipo;
    private Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        etNome = (EditText)findViewById(R.id.etNome);
        etUser = (EditText)findViewById(R.id.etUser);
        etSenha = (EditText)findViewById(R.id.etSenha);
        swTipo = (Switch)findViewById(R.id.swTipo);
        btnSalvar = (Button) findViewById(R.id.btnSalvar);

        actionEvent();
    }
    public void actionEvent(){
        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    class UsuarioAsyncTask extends AsyncTask<Usuario, Void, Usuario> {
        private Dialog dialog;
        @Override
        protected Usuario doInBackground(Usuario... params) {
            String urlString = params[0].getUrl();
            try {
                Usuario usuario = params[0];
                HttpContext localContext = new BasicHttpContext();
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlString);
                post.setHeader("Content-type", "application/json");
                //post.setHeader("Authorization",token);
                //post.setHeader("Cookie","ASP.NET_SessionId="+sessao+"; path=/; HttpOnly");
                JSONObject obj = new JSONObject();
                obj.put("nome", usuario.getNome());
                obj.put("login", usuario.getLogin());
                obj.put("senha", usuario.getPassword());
                obj.put("tipo", usuario.getTipo());


                StringEntity se = new StringEntity(obj.toString());
                post.setEntity(se);
                HttpResponse response = client.execute(post, localContext);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String json = getStringFromInputStream(instream);
                    instream.close();
                    instream.close();
                    usuario = getUsuario(usuario, json);
                    return usuario;
                }
            } catch (Exception e) {
                Log.e("Error", "Falha ao acessar Web service", e);
            }
            return params[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(CadastroActivity.this, "Aguarde",
                    "Cadastrando Usuario, Por Favor Aguarde...");
        }

        @Override
        protected void onPostExecute(Usuario usuario) {
            super.onPostExecute(usuario);
            dialog.dismiss();
            if (usuario.getIdUsuario() != null) {
                Toast.makeText(getApplicationContext(), "Usuario Cadastrado", Toast.LENGTH_LONG).show();
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        CadastroActivity.this).setTitle("Atenção")
                        .setMessage("Não foi possivel acessar o servidor...")
                        .setPositiveButton("OK", null);
                builder.create().show();
            }
        }

        private Usuario getUsuario(Usuario usuario, String jsonString) {
            List<Usuario> usuarios = new ArrayList<>();

            try {
                JSONArray locationLists = new JSONArray(jsonString);
                JSONObject orcJson;
                for (int i = 0; i < locationLists.length(); i++) {
                    orcJson = new JSONObject(locationLists.getString(i));

                    Log.i("TESTE", "id=" + orcJson.getLong("idusuario"));

                    usuario.setIdUsuario(orcJson.getLong("idusuario"));
                    usuarios.add(usuario);
                }
            } catch (JSONException e) {
                Log.e("Error", "Erro no parsing do JSON", e);
            }
            return usuarios.get(0);
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
