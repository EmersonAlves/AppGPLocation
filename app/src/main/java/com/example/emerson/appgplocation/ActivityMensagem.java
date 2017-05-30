package com.example.emerson.appgplocation;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.emerson.appgplocation.db.DBManager;
import com.example.emerson.appgplocation.model.Configuracao;
import com.example.emerson.appgplocation.model.EnviarUsuario;
import com.example.emerson.appgplocation.model.Mensagem;
import com.example.emerson.appgplocation.model.Posicao;
import com.example.emerson.appgplocation.model.Usuario;
import com.example.emerson.appgplocation.util.ChatArrayAdapter;
import com.example.emerson.appgplocation.util.ChatMessage;
import com.example.emerson.appgplocation.util.Service;
import com.example.emerson.appgplocation.util.Url;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
import java.util.ArrayList;
import java.util.List;

public class ActivityMensagem extends AppCompatActivity {

    private EditText etMsg;
    private Button btnEnviar;

    private static DBManager dbManager;

    private ListView listaMsg;

    private static ChatArrayAdapter chatArrayAdapter;


    public static boolean active = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mensagem);

        listaMsg = (ListView) findViewById(R.id.listMsgs);
        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.right);
        listaMsg.setAdapter(chatArrayAdapter);

        etMsg = (EditText) findViewById(R.id.etMsg);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);

        listaMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listaMsg.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listaMsg.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });


        dbManager = new DBManager(this);

        carregarMensagens();
        actionEvect();

    }

    public static void carregarMensagens(){
        if(dbManager.getListaMensagens().size() > 0) {
            chatArrayAdapter.clearList();
            for (Mensagem mensagem : dbManager.getListaMensagens()) {
                if (mensagem.getIdenviado() != 1) {
                    chatArrayAdapter.add(new ChatMessage(true, mensagem.getMsg()));
                } else {
                    chatArrayAdapter.add(new ChatMessage(false, mensagem.getMsg()));
                }
            }
            chatArrayAdapter.notifyDataSetChanged();
        }
    }
    public void actionEvect(){
        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etMsg.getText().toString().trim().isEmpty()) {
                    String novaMsg =  etMsg.getText().toString();
                    Usuario usuario = dbManager.getListaUsuarios().get(0);
                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdenviado(usuario.getIdUsuario());
                    mensagem.setMsg(novaMsg);
                    EnviarUsuario enviarUsuario = new EnviarUsuario();
                    enviarUsuario.setMensagem(mensagem);
                    enviarUsuario.setUrl(Url.url + "/php/enviarMsg.php?idenviou=" + usuario.getIdUsuario() + "&msg=" + etMsg.getText().toString());
                    new EnviarDadosJsonAsyncTask().execute(enviarUsuario);
                    //chatArrayAdapter.add(new ChatMessage(true, mensagem.getMsg()));
                    chatArrayAdapter.notifyDataSetChanged();
                    etMsg.setText("");
                }
            }
        });
    }
    class EnviarDadosJsonAsyncTask extends AsyncTask<EnviarUsuario, Void, Mensagem> {
        @Override
        protected Mensagem doInBackground(EnviarUsuario... params) {
            Mensagem mensagem = params[0].getMensagem();
            try {
                String urlString = params[0].getUrl();
                HttpContext localContext = new BasicHttpContext();
                HttpClient client = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(urlString);
                httpget.setHeader("Content-type", "application/json");
                JSONObject obj = new JSONObject();
                StringEntity se = new StringEntity(obj.toString());
                HttpResponse response = client.execute(httpget);
                HttpEntity entity = response.getEntity();

                InputStream instream = entity.getContent();

                String resultString = getStringFromInputStream(instream);
                instream.close();

                mensagem = getMensagem(mensagem,resultString);

                return mensagem;
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
        }

        @Override
        protected void onPostExecute(Mensagem result) {
            super.onPostExecute(result);
        }
        private Mensagem getMensagem(Mensagem mensagem,String jsonString) {
            Log.i("Info","JSON Teste:"+jsonString);
            return mensagem;
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

    @Override
    public void onStart() {
        super.onStart();
        active = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        active = false;
    }
}