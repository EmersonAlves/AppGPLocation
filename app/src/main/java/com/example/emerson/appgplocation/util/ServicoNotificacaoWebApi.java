package com.example.emerson.appgplocation.util;

/**
 * Created by Emerson on 29/05/2017.
 */
import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.emerson.appgplocation.ActivityMensagem;
import com.example.emerson.appgplocation.R;
import com.example.emerson.appgplocation.db.DBManager;
import com.example.emerson.appgplocation.model.EnviarUsuario;
import com.example.emerson.appgplocation.model.Mensagem;
import com.example.emerson.appgplocation.model.Usuario;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

public class ServicoNotificacaoWebApi extends android.app.Service {
    private DBManager dbManager;
    private HandlerThread handlerThread;
    private Handler handler;


   //Define o tempo entre notificações, altere como quiser
    private int TEMPO_ENTRE_NOTIFICAÇOES_SEGUNDOS = 5000;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        handlerThread = new HandlerThread("HandlerThread");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dbManager = new DBManager(this);

        //Previne que seja executado em subsequentes chamadas a onStartCommand
        if (!handlerThread.isAlive()) {
            Log.d("NotifyService", "Notificações iniciadas");
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    if(dbManager.getListaMensagens().size() > 0) {
                        Usuario usuario = dbManager.getListaUsuarios().get(0);
                        EnviarUsuario enviarUsuario = new EnviarUsuario();
                        enviarUsuario.setUsuario(usuario);
                        long id = 0;
                        for(Mensagem mensagem : dbManager.getListaMensagens()){
                            id = mensagem.getIdMsg();
                        }
                        enviarUsuario.setUrl(Url.url + "/php/verMsg.php?iduser=" + usuario.getIdUsuario()+"&ultimo="+id);
                        new ReceberMensagemDadosJsonAsyncTask().execute(enviarUsuario);
                    }else{
                        Usuario usuario = dbManager.getListaUsuarios().get(0);
                        EnviarUsuario enviarUsuario = new EnviarUsuario();
                        enviarUsuario.setUsuario(usuario);
                        enviarUsuario.setUrl(Url.url + "/php/verMsg.php?iduser=" + usuario.getIdUsuario());

                        new ReceberMensagemDadosJsonAsyncTask().execute(enviarUsuario);
                    }
                    handler.postDelayed(this, TEMPO_ENTRE_NOTIFICAÇOES_SEGUNDOS);
                }
            };
            handler.post(runnable);
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        handlerThread.quit();
    }

    class ReceberMensagemDadosJsonAsyncTask extends AsyncTask<EnviarUsuario, Void, List<Mensagem>> {
        @Override
        protected List<Mensagem> doInBackground(EnviarUsuario... params) {
            List<Mensagem> mensagens = new ArrayList<>();
            String urlString = params[0].getUrl();
            HttpContext localContext = new BasicHttpContext();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(urlString);

            try {
                HttpResponse response = client.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String json = getStringFromInputStream(instream);
                    instream.close();

                    mensagens = getMsg(json);
                    return mensagens;
                }
            } catch (Exception e) {
                Log.e("Error", "Falha ao acessar Web service", e);
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Mensagem> result) {
            super.onPostExecute(result);
            if(result != null && result.size() > 0){
                for(Mensagem mensagem : result) {
                    dbManager.inserirMSG(mensagem);
                }
                if(ActivityMensagem.active){
                    ActivityMensagem.carregarMensagens();
                } else {
                    sendNotification();
                }

            }
        }
        private List<Mensagem> getMsg(String jsonString) {
            List<Mensagem> mensagens = new ArrayList<>();
            Log.i("Info","JSON Teste:"+jsonString);
            try {
                JSONArray locationLists = new JSONArray(jsonString);
                for (int i = 0; i < locationLists.length(); i++) {
                    JSONObject teste = new JSONObject(locationLists.getString(i));

                    //Log.i("TESTE", "nome=" + locationJson.getString("idpoint"));

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdMsg(teste.getLong("idmsg"));
                    mensagem.setIdenviado(teste.getLong("iduserinviou"));
                    mensagem.setIdrecebido(teste.getLong("iduserrecebeu"));
                    mensagem.setMsg(teste.getString("msg"));

                    mensagens.add(mensagem);
                }
                return mensagens;
            } catch (JSONException e) {
                Log.e("Error", "Erro no parsing do JSON", e);
            }
            return null;
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
    private void sendNotification(){
        Intent intent = new Intent (this ,
                ActivityMensagem.class);
        PendingIntent pendingIntent = PendingIntent.getActivity
                (this, 0, intent, 0);
        Notification notification = new Notification.Builder (this)
                .setContentTitle("Mensagem")
                .setContentText("Recebeu uma nova mensagem...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .getNotification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService (NOTIFICATION_SERVICE);
        notificationManager.notify (0, notification);
        Log.d("NotifyService", "notificação enviada");
    }
}