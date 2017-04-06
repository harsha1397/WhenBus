package com.whenbus.whenbus;

/**
 * Created by harsha on 1/4/17.
 */

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String [] send = new  String[2];
        send[0] = email;
        send[1] = password;
        // TODO: Implement your own authentication logic here.
        LoginPostTask loginPostTask = new LoginPostTask();
        loginPostTask.execute(send);
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        // On complete call either onLoginSuccess or onLoginFailed
                        onLoginSuccess();
                        // onLoginFailed();
                        progressDialog.dismiss();
                    }
                }, 3000);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    public class LoginPostTask  extends AsyncTask<String, Integer, Boolean> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
////        progressDialog = ProgressDialog.show(Confirmation.this, "Please wait...", "Retrieving data ...", true);
//
//        }



        @Override
        protected Boolean doInBackground(String... something) {
            Boolean result = true, temp = false;
            String postBody = "";
            FileOutputStream outputStream;


//            if(Double.parseDouble(data11)<3) {
//                result = false;
//                return result;
//            }

            MediaType FORM_DATA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
//        try {
//            postBody =  "email" + "=" + URLEncoder.encode(something[0], "UTF-8")+
//                    "&password"+ "=" +URLEncoder.encode(something[1], "UTF-8")
//            ;
//
//            Log.w("1", "passed");
//        } catch (UnsupportedEncodingException ex) {
//            Log.w("1", "failed");
//            result = false;
//        }
            OkHttpClient client = new OkHttpClient();
//        RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);

            HttpUrl.Builder urlBuilder = HttpUrl.parse("http://192.168.100.6:8000/login").newBuilder();
            urlBuilder.addQueryParameter("email", something[0]);
            urlBuilder.addQueryParameter("password", something[1]);
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
//                .post(postBody)
                    .build();
            //Send the request
            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();
                Log.w("received", responseData);
            }
            catch (Exception e){
                e.printStackTrace();
            }

            return  result;
        }

        @Override
        protected void onProgressUpdate(Integer... value) {
            super.onProgressUpdate(value);
        }
        @Override
        protected void onPostExecute(Boolean result){
            //Print Success or failure message accordingly
//            Toast.makeText(this,result?"Message successfully sent!":"There was some error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
        }
    }
}