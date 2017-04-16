package com.whenbus.whenbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.whenbus.whenbus.Constants.HOST;

/**
 * Created by harsha on 11/2/17.
 */

public class ShowBuses extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private static String LOG_TAG = "ShowBuses";
    Context context;
    JSONArray buses;
    private String busStart, busEnd, busNo, dest;
    private double lat, lng;
    private Location currentLocation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_buses);
        context = this;
        try {
            buses = new JSONArray(getIntent().getStringExtra("buses"));
            currentLocation = (Location) getIntent().getExtras().get("currentLocation");
            dest = getIntent().getExtras().getString("dest");
            lat = getIntent().getDoubleExtra("lat", 0);
            lng = getIntent().getDoubleExtra("lng", 0);
//            src = getIntent().getStringExtra("src");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);




        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
                .MyClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                try {
                    JSONObject bus = buses.getJSONObject(position);
                    busNo = bus.getString("bus_no");
                    //                String[] srcDest = getSrcDest(busNo);
                    busStart = bus.getString("start_point");
                    busEnd = bus.getString("end_point");
                }
                catch (Exception e){

                }
                JSONObject post = new JSONObject();
                JSONObject coord = new JSONObject();
                try{
                    coord.put("lat", lat);
                    coord.put("lng", lng);
                    post.put("bus_no", busNo);
                    post.put("coord", coord);
                    post.put("start_point", busStart);
                    post.put("end_point", busEnd);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                PostDataTask postDataTask = new PostDataTask();
                postDataTask.execute(post.toString());
            }
        });
    }

    private String time(String tim){
        int ti;
        String t;
        ti = Integer.parseInt(tim);
        int hrs = ti/60;
        int min = ti%60;
        String zero = "";
        if(min < 10)zero = "0";
        t = Integer.toString(hrs) + ":" + zero + Integer.toString(min);
        return t;
    }
    @Override
    protected void onResume() {
        super.onResume();
//        ((MyRecyclerViewAdapter) mAdapter).setOnItemClickListener(new MyRecyclerViewAdapter
//                .MyClickListener() {
//            @Override
//            public void onItemClick(int position, View v) {
//                Log.i(LOG_TAG, "Clicked on Item " + position);
//            }
//        });
    }

//    private ArrayList<DataObject> getDataSet() {
//        ArrayList results = new ArrayList<DataObject>();
//        for (int index = 0; index < 20; index++) {
//            DataObject obj = new DataObject("5C" + index,
//                    "Taramani - Broadway " + index, "11:00AM" + index);
//            results.add(index, obj);
//        }
//        return results;
//    }
    private ArrayList<DataObject> getDataSet() {
        ArrayList results = new ArrayList<DataObject>();
        try{
            for (int index = 0; index < buses.length(); index++) {
                JSONObject bus = buses.getJSONObject(index);
                Log.i(LOG_TAG, bus.toString());
                String busNo = bus.getString("bus_no");
//                String[] srcDest = getSrcDest(busNo);
                String startPoint = bus.getString("start_point");
                String endPoint = bus.getString("end_point");
                String timetmp = buses.getJSONObject(index).getString("time");
                String src = buses.getJSONObject(index).getString("src");
                String time = time(timetmp);
                DataObject obj = new DataObject(busNo,
                        startPoint + " - " + endPoint,
                        time);
                results.add(index, obj);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return results;
    }

    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int itemPosition = mRecyclerView.indexOfChild(v);
            try {
                JSONObject bus = buses.getJSONObject(itemPosition);
                busNo = bus.getString("bus_no");
                //                String[] srcDest = getSrcDest(busNo);
                busStart = bus.getString("start_point");
                busEnd = bus.getString("end_point");
            }
            catch (Exception e){

            }
            JSONObject post = new JSONObject();
            JSONObject coord = new JSONObject();
            try{
                coord.put("lat", lat);
                coord.put("lng", lng);
                post.put("bus_no", busNo);
                post.put("coord", coord);
                post.put("start_point", busStart);
                post.put("end_point", busEnd);
            }
            catch (Exception e){
                e.printStackTrace();
            }
            PostDataTask postDataTask = new PostDataTask();
            postDataTask.execute(post.toString());



            //Log.e("",String.valueOf(itemPosition));
        }
    }

    public String[] getSrcDest(String bus)
    {
        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
        Cursor cursor = mydatabase.query("busno_info", new String[] {"src, dest"}, "busno=?", new String[] {bus}, null, null, null);
        cursor.moveToFirst();
        String [] directions = new String[2];
        directions [0] = cursor.getString(cursor.getColumnIndex("src"));
        directions[1] = cursor.getString(cursor.getColumnIndex("dest"));
        return directions;
    }

    public void toastMessage(String message){
        Toast.makeText(this,message,
                Toast.LENGTH_LONG).show();
    }
    class PostDataTask extends AsyncTask<String, Integer, Boolean> {
        int code = 0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//          progressDialog = ProgressDialog.show(Confirmation.this, "Please wait...", "Retrieving data ...", true);
        }

        @Override
        protected Boolean doInBackground(String... post) {
            Boolean result = true;
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, post[0]);
            String postUrl = HOST + "/info/bus/";
            Log.i("url",postUrl);
            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
            String url = urlBuilder.build().toString();

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            //Send the request
            String responseData = "";
            try {
                Response response = client.newCall(request).execute();
                try {
                    responseData = response.body().string();
                }
                catch(Exception e){
                    code = 1;
                    result = false;
                    return result;
                }
                if(responseData.length() == 0){
                    code = 1;
                    result = false;
                    return result;
                }
                JSONObject JSONresponse = new JSONObject(responseData);
                int time = Integer.parseInt(JSONresponse.get("time").toString());
                JSONObject busLocation = (JSONObject) JSONresponse.get("busLoc");
                String nearestStop = JSONresponse.get("stop").toString();
                int id = JSONresponse.getInt("id");
                String [] stop = new String[1];
                stop[0] = nearestStop;
                String [] columns = {
                        "lat",
                        "lng"
                };
                SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
                Cursor sourceCursor = mydatabase.query("busstop_coord", columns, "busstop=?", stop, null, null, null);
                sourceCursor.moveToFirst();

                Location src = new Location(LocationManager.GPS_PROVIDER);

                src.setLatitude(Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lat"))));
                src.setLongitude(Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lng"))));
                sourceCursor.close();

                Location busL = new Location(LocationManager.GPS_PROVIDER);
                busL.setLatitude(Double.parseDouble(busLocation.get("lat").toString()));
                busL.setLongitude(Double.parseDouble(busLocation.get("lng").toString()));

//                progressDialog.dismiss();

                Intent intent = new Intent(context, ShowMapActivity.class).putExtra("currentLocation", currentLocation)
                        .putExtra("busLocation", busL)
                        .putExtra("srcLocation", src)
                        .putExtra("busStart", busStart)
                        .putExtra("busEnd", busEnd)
                        .putExtra("busNo", busNo)
                        .putExtra("time", time)
                        .putExtra("busId", id)
                        .putExtra("dest", dest)
                        ;
                startActivity(intent);
            }
            catch (Exception e){
                e.printStackTrace();
//                progressDialog.dismiss();
                result = false;
            }

            return  result;
        }

        @Override
        protected void onPostExecute(Boolean result){
            if(!result){
                if(code == 1)
                    toastMessage("Server error");
                else
                    toastMessage("No internet connection. Please try again");
            }
//            Toast.makeText(this,result?"Message successfully sent!":"There wassome error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onPostResume(){
        super.onPostResume();
    }
}
