package com.example.emerson.appgplocation;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.emerson.appgplocation.db.DBManager;
import com.example.emerson.appgplocation.model.Usuario;
import com.example.emerson.appgplocation.util.Url;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ActivityMenuVendedor extends AppCompatActivity {
    private DBManager dbManager;
    private Button btnCliente;
    private Button btnMapaCliente;
    private Button btnSair;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_vendedor);

        dbManager = new DBManager(this);

        btnCliente = (Button)findViewById(R.id.btnCliente);
        btnMapaCliente = (Button)findViewById(R.id.btnMapaCliente);
        btnSair = (Button)findViewById(R.id.btnSair);
        Activity activity = this;
        eventAction(activity);
    }

    public void eventAction(final Activity activity){
        btnCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator = new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }
        });
        btnMapaCliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMenuVendedor.this,MapsActivity.class);
                startActivity(intent);
            }
        });

        btnSair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent("SERVICO_TEST");
                stopService(it);
                Usuario usuario = dbManager.getListaUsuarios().get(0);
                dbManager.deleteUsuario(usuario);
                Intent intent = new Intent(ActivityMenuVendedor.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() != null){
                String url = result.getContents();
                if(url.contains(Url.url)){
                    Intent intent = new Intent(ActivityMenuVendedor.this,ActivityCliente.class);
                    intent.putExtra("url",url);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), "QRCODE Invalido", Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
