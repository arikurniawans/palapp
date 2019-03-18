package com.polda.ari.palapps;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MenuApps extends AppCompatActivity {
    SessionSharePreference session;
    ImageButton im_toko, im_keluar;
    TextView txt_namasup;
    NiftyDialogBuilder dialogs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_apps);
        dialogs = NiftyDialogBuilder.getInstance(this);
        im_toko = (ImageButton) findViewById(R.id.btn_toko);
        im_keluar = (ImageButton) findViewById(R.id.btn_exit);
        txt_namasup = (TextView) findViewById(R.id.txt_namasup);
        session = new SessionSharePreference(MenuApps.this.getApplicationContext());
        String kode_spv = session.getNama();

        im_toko.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MenuApps.this, Frm_toko.class);
                startActivity(intent);
            }
        });

        im_keluar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogs.withTitle("Informasi")
                        .withMessage("Apakah anda yakin ingin keluar aplikasi ?")
                        .withDialogColor("#2cafe2")
                        .withButton1Text("YA")
                        .withButton2Text("TIDAK")
                        .withEffect(Effectstype.Fall);
                dialogs.isCancelableOnTouchOutside(true);
                dialogs.setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        session.setNama(null);
                        Intent intent = new Intent(MenuApps.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
                dialogs.setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogs.dismiss();
                    }
                });
                dialogs.show();
            }
        });

        getJSON(kode_spv);

    }


    private void getJSON(final String id){
        class GetJSON extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MenuApps.this,"Menampilkan Data","Tunggu Sebentar...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                //Toast.makeText(getApplication(),"Data "+s,Toast.LENGTH_LONG).show();
                showDetail(s);
            }

            @Override
            protected String doInBackground(Void... params) {
                HashMap<String,String> nama_lok = new HashMap<>();
                nama_lok.put(Config.TAG_KODE_SPV, id);

                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest(Config.UPLOAD_URL_SP, nama_lok);
                return s;
            }
        }
        GetJSON gj = new GetJSON();
        gj.execute();
    }

    private void showDetail(String json){

        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray result = jsonObject.getJSONArray("result");

            for(int i = 0; i<result.length(); i++){
                JSONObject c = result.getJSONObject(i);

                String nama = c.getString(Config.TAG_NAMA_SPV);
                txt_namasup.setText(nama);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(MenuApps.this, MenuApps.class);
        startActivity(intent);
    }


}
