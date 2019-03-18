package com.polda.ari.palapps;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Frm_dt_toko extends AppCompatActivity {
    TextView txt_lat, txt_long,txt_kode_cust,txt_kode_spv;
    private Button buttonUpload;
    SessionSharePreference session;

    private Bitmap bitmap, bitmap2;

    private int PICK_IMAGE_REQUEST = 1;

    private static final String TAG = Frm_dt_toko.class.getSimpleName();
    private static final int CAMERA_REQUEST_CODE = 7777;
    private static final int CAMERA_REQUEST_CODE2 = 7778;

    ImageView f_lokasi, f_ktp;
    EditText txt_lokasi, txt_nama_l,txt_alamat_l,txt_no_ktp,txt_no_hp,txt_no_lokasi;
    String kd_cust;

    private static final int REQUEST_LOCATION = 1;

    LocationManager locationManager;
    String lattitude,longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frm_dt_toko);
        session = new SessionSharePreference(Frm_dt_toko.this.getApplicationContext());

        txt_nama_l = (EditText) findViewById(R.id.txt_nama_l);
        txt_alamat_l = (EditText) findViewById(R.id.txt_alamat_l);
        txt_kode_cust = (TextView) findViewById(R.id.txt_kode_cust);
        txt_lokasi = (EditText) findViewById(R.id.txt_no_lokasi);
        txt_no_ktp = (EditText) findViewById(R.id.txt_no_ktp);
        txt_no_hp = (EditText) findViewById(R.id.txt_no_hp);
        txt_no_lokasi = (EditText) findViewById(R.id.txt_no_lokasi);
        f_lokasi = (ImageView) findViewById(R.id.imageView);
        f_ktp = (ImageView) findViewById(R.id.imageView2);

        txt_lat = (TextView) findViewById(R.id.txt_lat);
        txt_long = (TextView) findViewById(R.id.txt_long);
        //Toast.makeText(getApplication(),"Data "+txt_lat.getText().toString().trim()+"\n"+txt_long.getText().toString().trim(),Toast.LENGTH_LONG).show();
        txt_kode_spv = (TextView) findViewById(R.id.txt_kode_spv);

        txt_nama_l.setEnabled(false);
        txt_alamat_l.setEnabled(false);
        txt_lokasi.setEnabled(false);
        buttonUpload = (Button) findViewById(R.id.btnUpload);

        final Intent intent = getIntent();
        kd_cust = intent.getStringExtra(Config.TAG_KODE);
        String kode_spv = session.getNama();
        //Toast.makeText(getApplication(),"Data "+kode_spv,Toast.LENGTH_LONG).show();
        txt_kode_spv.setText(kode_spv);
        getJSON(kd_cust);

        Camerapermission();

        int version = Build.VERSION.SDK_INT;

        if(version >= 19){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGps();

            } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                getLocation();
            }
        }else {
            if (Frm_toko.mLastLocation != null) {
            /*Toast.makeText(Frm_dt_toko.this," Get Location \n " +
                    "Latitude : "+ Frm_toko.mLastLocation.getLatitude()+
                    "\nLongitude : "+Frm_toko.mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();*/
                txt_lat.setText("" + Frm_toko.mLastLocation.getLatitude());
                txt_long.setText("" + Frm_toko.mLastLocation.getLongitude());
                getCompleteAddressString(Frm_toko.mLastLocation.getLatitude(), Frm_toko.mLastLocation.getLongitude());
            }
        }


        f_lokasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            }
        });

        f_ktp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_REQUEST_CODE2);
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txt_no_ktp.getText().toString().trim().equals("") || txt_no_hp.getText().toString().trim().equals("")){
                    Toast.makeText(getApplication(),"Data tidak boleh kosong !",Toast.LENGTH_LONG).show();
                }else{
                    uploadImage();
                }
            }
        });

    }


    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(Frm_dt_toko.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (Frm_dt_toko.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Frm_dt_toko.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                double latti = location.getLatitude();
                double longi = location.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
                /*Toast.makeText(getApplication(),"Your current location is"+ "\n" + "Lattitude = " + lattitude
                        + "\n" + "Longitude = " + longitude,Toast.LENGTH_LONG).show();*/
                txt_lat.setText(lattitude);
                txt_long.setText(longitude);
                getCompleteAddressString(latti,longi);

            } else  if (location1 != null) {
                double latti = location1.getLatitude();
                double longi = location1.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);
               /*Toast.makeText(getApplication(),"Your current location is"+ "\n" + "Lattitude = " + lattitude
                        + "\n" + "Longitude = " + longitude,Toast.LENGTH_LONG).show();*/
                txt_lat.setText(lattitude);
                txt_long.setText(longitude);
                getCompleteAddressString(latti,longi);

            } else  if (location2 != null) {
                double latti = location2.getLatitude();
                double longi = location2.getLongitude();
                lattitude = String.valueOf(latti);
                longitude = String.valueOf(longi);

                /*Toast.makeText(getApplication(),"Your current location is"+ "\n" + "Lattitude = " + lattitude
                        + "\n" + "Longitude = " + longitude,Toast.LENGTH_LONG).show();*/
                txt_lat.setText(lattitude);
                txt_long.setText(longitude);
                getCompleteAddressString(latti,longi);

            }else{

                Toast.makeText(this,"Unble to Trace your location",Toast.LENGTH_SHORT).show();

            }
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                //Toast.makeText(getApplication(),"Data Alamat Anda : "+ strReturnedAddress.toString(),Toast.LENGTH_LONG).show();
                txt_lokasi.setText(strReturnedAddress.toString());
                //Log.w("My Current loction address", strReturnedAddress.toString());
            } else {
                //Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            //Log.w("My Current loction address", "Canont get Address!");
        }
        return strAdd;
    }




    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(Frm_dt_toko.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        Toast.makeText(Frm_dt_toko.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image_toko = getStringImage(bitmap);
                String image_ktp = getStringImage(bitmap2);

                //Getting Image Name
                String kode_cust = txt_kode_cust.getText().toString().trim();
                String no_ktp = txt_no_ktp.getText().toString().trim();
                String no_hp = txt_no_hp.getText().toString().trim();
                String lokasi = txt_no_lokasi.getText().toString().trim();
                String kd_spv = txt_kode_spv.getText().toString().trim();
                String lati = txt_lat.getText().toString().trim();
                String longi = txt_long.getText().toString().trim();

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(Config.TAG_KODE_CUST, kode_cust);
                params.put(Config.TAG_IM_TOKO, image_toko);
                params.put(Config.TAG_NO_KTP, no_ktp);
                params.put(Config.TAG_IM_KTP, image_ktp);
                params.put(Config.TAG_NO_HP, no_hp);
                params.put(Config.TAG_LOKASI, lokasi);
                params.put(Config.TAG_KODE_SPV, kd_spv);
                params.put(Config.TAG_LAT, lati);
                params.put(Config.TAG_LONG, longi);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void Camerapermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(Frm_dt_toko.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(Frm_dt_toko.this,
                    Manifest.permission.CAMERA)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(Frm_dt_toko.this,
                        new String[]{Manifest.permission.CAMERA},
                        0);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

        switch (requestCode) {
            case(CAMERA_REQUEST_CODE) :
                if(resultCode == Activity.RESULT_OK)
                {
                    bitmap = (Bitmap) data.getExtras().get("data");
                    f_lokasi.setImageBitmap(bitmap);
                }
                break;

            case(CAMERA_REQUEST_CODE2) :
                if(resultCode == Activity.RESULT_OK)
                {
                    bitmap2 = (Bitmap) data.getExtras().get("data");
                    f_ktp.setImageBitmap(bitmap2);
                }
                break;
        }

    }


    private void getJSON(final String id){
        class GetJSON extends AsyncTask<Void,Void,String> {

            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(Frm_dt_toko.this,"Menampilkan Data","Tunggu Sebentar...",false,false);
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
                nama_lok.put(Config.TAG_KODE, id);

                RequestHandler rh = new RequestHandler();
                String s = rh.sendPostRequest(Config.UPLOAD_URL_DT, nama_lok);
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

                String kode = c.getString(Config.TAG_KODE);
                String nama = c.getString(Config.TAG_NAMA_TOKO);
                String alamat = c.getString(Config.TAG_ALAMAT_TOKO);
                String ktp = c.getString(Config.TAG_NO_KTP);
                String hp = c.getString(Config.TAG_NO_HP);

                txt_nama_l.setText(nama);
                txt_alamat_l.setText(alamat);
                txt_kode_cust.setText(kode);
                txt_no_ktp.setText(ktp);
                txt_no_hp.setText(hp);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



}
