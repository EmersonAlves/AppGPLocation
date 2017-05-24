package com.example.emerson.appgplocation;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.example.emerson.appgplocation.model.Location;
import com.example.emerson.appgplocation.util.Url;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        new DownloadJsonAsyncTask()
                .execute(Url.url+"/painel/php/markerPoint.php");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
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
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-3.7542353 , -38.5307835),11));
    }
    class DownloadJsonAsyncTask extends AsyncTask<String, Void, List<Location>> {

        private Dialog dialog;
        @Override
        protected List<Location> doInBackground(String... params) {
            String urlString = params[0];
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(urlString);
            List<Location> locations = new ArrayList<>();
            try {
                HttpResponse response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    /*InputStream instream = entity.getContent();
                    String json = toString(instream);
                    instream.close();*/
                    InputStream instream = entity.getContent();
                    String json = getStringFromInputStream(instream);
                    instream.close();

                    locations = getLocation(json);
                }
            } catch (Exception e) {
                Log.e("Error", "Falha ao acessar Web service", e);
            }
            return locations;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = ProgressDialog.show(MapsActivity.this, "Aguarde",
                    "Baixando JSON, Por Favor Aguarde...");
        }

        private List<Location> getLocation(String jsonString) {

            List<Location> locations = new ArrayList<>();

            try {
                JSONArray locationLists = new JSONArray(jsonString);
                JSONObject locationJson;

                for (int i = 0; i < locationLists.length(); i++) {
                    locationJson = new JSONObject(locationLists.getString(i));

                    //Log.i("TESTE", "nome=" + locationJson.getString("idpoint"));

                   Location location = new Location();
                    location.setIdpoint(locationJson.getLong("idcliente"));
                    location.setLat(locationJson.getString("lat"));
                    location.setLog(locationJson.getString("log"));
                    location.setDescricao(locationJson.getString("nome"));

                    locations.add(location);
                }
            } catch (JSONException e) {
                Log.e("Error", "Erro no parsing do JSON", e);
            }

            return locations;
        }

        @Override
        protected void onPostExecute(List<Location> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            if (result.size() > 0) {
                //Encontrou
                popularLocaisMarcados(result);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MapsActivity.this).setTitle("Atenção")
                        .setMessage("Não foi possivel acessar essas informações...")
                        .setPositiveButton("OK", null);
                builder.create().show();
            }
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

   public void popularLocaisMarcados(List<Location> lista){
       for(Location location: lista) {
           mMap.addMarker(new MarkerOptions()
                   .position(new LatLng( Double.parseDouble(location.getLat()), Double.parseDouble(location.getLog())))
                   .icon(BitmapDescriptorFactory.fromResource(R.drawable.point))
                   .title(location.getDescricao()));
       }
    }
}
