package com.whenbus.whenbus;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by harsha on 11/2/17.
 */

public class MainActivity extends AppCompatActivity {
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);

        context = this;
        if ( ContextCompat.checkSelfPermission( context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( MainActivity.this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    1);
        }
        else {
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
//
//        Boolean isLoggedIn =settings.getBoolean("logged_in", false);
//        if(!isLoggedIn)
//        {
//            Intent intent = new Intent(context, LoginActivity.class);
//            startActivity(intent);
//        }
//        else
//        {
            Intent intent = new Intent(context, UserInputActivity.class);
            startActivity(intent);
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Intent intent = new Intent(context, UserInputActivity.class);
                        startActivity(intent);
                    } else {
                        this.finish();
                    }
                }
            }
        }
    }

}
