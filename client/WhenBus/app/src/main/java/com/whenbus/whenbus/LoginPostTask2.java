package com.whenbus.whenbus;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by harsha on 1/4/17.
 */

public class LoginPostTask2 extends AsyncTask<String, Integer, Boolean> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

//        progressDialog = ProgressDialog.show(Confirmation.this, "Please wait...", "Retrieving data ...", true);

    }

//    @Override
//    protected void onProgressUpdate(Integer... value) {
//        super.onProgressUpdate(value);
//    }

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
//            Log.w("received", responseData);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return  result;
    }


    @Override
    protected void onPostExecute(Boolean result){
        //Print Success or failure message accordingly
//            Toast.makeText(this,result?"Message successfully sent!":"There was some error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
    }
}