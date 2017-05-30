package com.example.emerson.appgplocation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.emerson.appgplocation.db.DBManager;
import com.example.emerson.appgplocation.model.Configuracao;
import com.example.emerson.appgplocation.model.EnviarUsuario;
import com.example.emerson.appgplocation.model.Usuario;
import com.example.emerson.appgplocation.util.Service;
import com.example.emerson.appgplocation.util.ServicoNotificacaoWebApi;
import com.example.emerson.appgplocation.util.Url;

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
import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {
    private EditText etLogin;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCadastrar;

    private DBManager dbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbManager = new DBManager(this);

        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogar);
        btnCadastrar = (Button) findViewById(R.id.btnCadastrar);

        String provider = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(provider == null || provider.length() == 0){
            //Se vier null ou length == 0   é por que o GPS esta desabilitado.
            //Para abrir a tela do menu pode fazer assim:
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1);
        }

        if(dbManager.getConfig(1L) == null){
            Configuracao configuracao = new Configuracao();
            configuracao.setIntervalo(5);
            dbManager.inserirConfig(configuracao);
        }

        actionEvent();
        if(dbManager.getListaUsuarios().size() > 0){
            startService();
            Intent intent = new Intent(MainActivity.this,ActivityMenuVendedor.class);
            startActivity(intent);
            startService(new Intent(MainActivity.this, ServicoNotificacaoWebApi.class));
            finish();
        }
        //startService();
    }

    public void actionEvent(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarUsuario enviar = new EnviarUsuario();
                Usuario user = new Usuario();
                user.setLogin(etLogin.getText().toString());
                user.setPassword(etPassword.getText().toString());

                enviar.setUsuario(user);
                enviar.setUrl(Url.url+"/php/loginApp.php");

                new EnviarDadosJsonAsyncTask()
                        .execute(enviar);
            }
        });
        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,CadastroActivity.class);
                startActivity(intent);
            }
        });
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
                obj.put("login", params[0].getUsuario().getLogin());
                obj.put("senha", params[0].getUsuario().getPassword());
                StringEntity se = new StringEntity(obj.toString());
                post.setEntity(se);
                HttpResponse response = client.execute(post, localContext);

                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();

                String resultString = getStringFromInputStream(instream);
               // JSONObject jsonObjRecv = new JSONObject(resultString);
               // Log.i("json servidor", jsonObjRecv.toString());
                instream.close();

                usuario = getUsuario(usuario,resultString);

                return usuario;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MainActivity.this, "Aguarde",
                    "Autenticando...");
        }

        @Override
        protected void onPostExecute(Usuario result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if(result.getIdUsuario() != null) {
                if (result.getIdUsuario() > 0) {
                    //Salvou
                    dbManager.inserirUsuario(result);
                    startService();
                    Intent intent = new Intent(MainActivity.this, ActivityMenuVendedor.class);
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this).setTitle("Atenção")
                            .setMessage("Login ou Senha Invalido !")
                            .setPositiveButton("OK", null);
                    builder.create().show();
                }
            }else{
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this).setTitle("Atenção")
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

    public void startService(){
        Intent it = new Intent(MainActivity.this, Service.class);
        startService(it);
    }
}
