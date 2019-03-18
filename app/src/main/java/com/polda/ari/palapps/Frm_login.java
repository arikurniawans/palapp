package com.polda.ari.palapps;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Frm_login extends AppCompatActivity {
    SessionSharePreference session;
    NiftyDialogBuilder dialogs;
    public static final String USER_NAME = "USERNAME";
    EditText txt_kode;
    Button btn_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_login);
        session = new SessionSharePreference(Frm_login.this.getApplicationContext());
        dialogs = NiftyDialogBuilder.getInstance(this);
        txt_kode = (EditText) findViewById(R.id.txt_kode);
        btn_login = (Button) findViewById(R.id.btnSignIn);

        String nama = session.getNama();

        if(nama!=null){
            Intent intent = new Intent(Frm_login.this, MenuApps.class);
            startActivity(intent);
        }else{
            Toast.makeText(getApplication(),"Anda belum login",Toast.LENGTH_LONG).show();
        }

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login(txt_kode.getText().toString().trim());
            }
        });

    }


    private void login(final String username) {
        //Toast.makeText(getApplication(),"Data "+username,Toast.LENGTH_LONG).show();

        class LoginAsync extends AsyncTask<String, Void, String> {

            private Dialog loadingDialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loadingDialog = ProgressDialog.show(Frm_login.this, "Please wait", "Loading...",false,false);
            }

            @Override
            protected String doInBackground(String... params) {


                InputStream is = null;
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("username", username));
                String result = null;

                try{
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpPost httpPost = new HttpPost(
                            "http://web.perdanaadhilestari.com/api_spv/login.php");
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpClient.execute(httpPost);

                    HttpEntity entity = response.getEntity();

                    is = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    result = sb.toString();
                } catch (ClientProtocolException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){
                String s = result.trim();
                loadingDialog.dismiss();
                if(s.equalsIgnoreCase("success")){
                    String nama = String.valueOf(txt_kode.getText());
                    session.setNama(nama);
                    Intent intent = new Intent(Frm_login.this, MenuApps.class);
                    intent.putExtra(USER_NAME, txt_kode.getText().toString().trim());
                    finish();
                    startActivity(intent);
                }else {
                    dialogs
                            .withTitle("Informasi")
                            .withMessage("Akun tidak terdaftar !!!")
                            .withDialogColor("#c0392b")
                            .withButton1Text("OK")
                            .withEffect(Effectstype.Shake);
                    dialogs.setButton1Click(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialogs.dismiss();
                        }
                    });
                    dialogs.show();
                }
            }
        }

        LoginAsync la = new LoginAsync();
        la.execute(username);

    }

    @Override
    public void onBackPressed(){
        dialogs
                .withTitle("Informasi")
                .withMessage("Anda belum login !!!")
                .withDialogColor("#c0392b")
                .withButton1Text("OK")
                .withEffect(Effectstype.Shake);
        dialogs.isCancelableOnTouchOutside(false);
        dialogs.setButton1Click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogs.dismiss();
                Intent intent = new Intent(Frm_login.this,MainActivity.class);
                startActivity(intent);

            }
        });
        dialogs.show();
    }


}
