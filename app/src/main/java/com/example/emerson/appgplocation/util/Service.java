package com.example.emerson.appgplocation.util;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.example.emerson.appgplocation.ActivityMenuVendedor;
import com.example.emerson.appgplocation.MainActivity;
import com.example.emerson.appgplocation.db.DBManager;
import com.example.emerson.appgplocation.model.Configuracao;
import com.example.emerson.appgplocation.model.EnviarUsuario;
import com.example.emerson.appgplocation.model.Posicao;
import com.example.emerson.appgplocation.model.Usuario;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emerson on 15/05/2017.
 */

public class Service extends android.app.Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private DBManager dbManager;
    private HandlerThread handlerThread;
    private Handler handler;
    private Location location;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;

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

    public synchronized void callConnection() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public void initLocalRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationService() {
        initLocalRequest();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, Service.this);
    }

    public void stopLocationService() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, Service.this);
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
                    callConnection();
                    if (dbManager.getListaUsuarios().size() > 0) {
                        if (location != null) {
                            Usuario usuario = dbManager.getListaUsuarios().get(0);
                            Posicao posicao = new Posicao();
                            posicao.setIdUsuario(usuario.getIdUsuario());
                            posicao.setLat(location.getLatitude());
                            posicao.setLongi(location.getLongitude());

                            EnviarUsuario enviar = new EnviarUsuario();
                            enviar.setPosicao(posicao);
                            enviar.setUrl(Url.url + "/php/salvaPosicao.php");
                            new EnviarDadosJsonAsyncTask().execute(enviar);
                            if (dbManager.getConfig(1L) != null) {
                                TEMPO_ENTRE_NOTIFICAÇOES_SEGUNDOS = (1000 * 60) * dbManager.getConfig(1L).getIntervalo();
                            }
                            Log.i("Info", "TEste 1 ");
                        }
                    } else {
                        stopLocationService();
                        handlerThread.quit();
                    }
                    Log.i("Info", "TEste 2");
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location != null){
            Log.i("Info","teste "+location.getLongitude());
        }
        startLocationService();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
    }

    class EnviarDadosJsonAsyncTask extends AsyncTask<EnviarUsuario, Void, Configuracao> {

        @Override
        protected Configuracao doInBackground(EnviarUsuario... params) {
            Configuracao configuracao = new Configuracao();
            try {
                String urlString = params[0].getUrl();
                HttpContext localContext = new BasicHttpContext();
                HttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(urlString);
                post.setHeader("Content-type", "application/json");
                //post.setHeader("Authorization",token);
                //post.setHeader("Cookie","ASP.NET_SessionId="+sessao+"; path=/; HttpOnly");
                JSONObject obj = new JSONObject();
                obj.put("idusuario", params[0].getPosicao().getIdUsuario());
                obj.put("lat", params[0].getPosicao().getLat());
                obj.put("longi", params[0].getPosicao().getLongi());
                StringEntity se = new StringEntity(obj.toString());
                post.setEntity(se);
                HttpResponse response = client.execute(post, localContext);

                HttpEntity entity = response.getEntity();
                InputStream instream = entity.getContent();

                String resultString = getStringFromInputStream(instream);
                // JSONObject jsonObjRecv = new JSONObject(resultString);
                // Log.i("json servidor", jsonObjRecv.toString());
                instream.close();

                configuracao = getConfig(configuracao,resultString);

                return configuracao;
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
        }

        @Override
        protected void onPostExecute(Configuracao result) {
            super.onPostExecute(result);
            if(result != null){
                if(dbManager.getConfig(1L) == null) {
                    dbManager.inserirConfig(result);
                }else{
                    dbManager.atualizarConfig(result);
                }
            }
        }
        private Configuracao getConfig(Configuracao configuracao,String jsonString) {
            try {
                JSONObject usuarioLists = new JSONObject(jsonString);
                configuracao.setIdConfig(usuarioLists.getLong("idconfig"));
                configuracao.setIntervalo(usuarioLists.getInt("intervalo"));
                return configuracao;
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
}
