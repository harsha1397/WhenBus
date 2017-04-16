package com.whenbus.whenbus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by harsha on 16/4/17.
 */

public class NearestStop extends AppCompatActivity implements OnMapReadyCallback, LocationListener, RoutingListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    Context context;
    private GoogleMap map = null;
    private TextView dest, stop;
    private String destStop;
    private LatLng destLatLng, userLatLng;
    private Marker destMarker, userMarker;
    protected LocationManager locationManager;
    private List<Polyline> polylines;

    public void onLocationChanged(Location location) {
//        hasLocation = true;
//        currentLocation = location;
        Log.i("Location", "got location");
        if(map != null) {
            double destLat = location.getLatitude();
            double destLng = location.getLongitude();
            userLatLng = new LatLng(destLat, destLng);

            if (userMarker != null) userMarker.remove();
            MarkerOptions options = new MarkerOptions();
            options.position(userLatLng).title("Bus location");
//            options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
            userMarker = map.addMarker(options);
            userMarker.showInfoWindow();
        }
//        JSONObject post = new JSONObject();
//        JSONObject coord = new JSONObject();
//        double currentLatitude = location.getLatitude();
//        double currentLongitude = location.getLongitude();
//        int time = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)*60 + Calendar.getInstance().get(Calendar.MINUTE);
//        try{
//            coord.put("lat", currentLatitude);
//            coord.put("lng", currentLongitude);
//            post.put("key", key);
//            post.put("coord", coord);
//            post.put("timestamp", time);
//        }
//        catch (Exception e){
//
//        }
//        SendFeedback sendFeedback = new SendFeedback();
//        sendFeedback.execute(post.toString());
        Log.i("Updating ", "Location");
        route(userLatLng, destLatLng);
//        Toast.makeText(getBaseContext(),currentLat+"-"+currentLon, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        String [] columns = {
                "lat",
                "lng"
        };
        SQLiteDatabase mydatabase = openOrCreateDatabase("WhenBus_db.db",MODE_PRIVATE,null);
        Cursor sourceCursor = mydatabase.query("busstop_coord", columns, "busstop=?", new String[] {destStop}, null, null, null);
        sourceCursor.moveToFirst();
        Location src = new Location(LocationManager.GPS_PROVIDER);

        double destLat = Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lat")));
        double destLng = Double.parseDouble(sourceCursor.getString(sourceCursor.getColumnIndex("lng")));

        sourceCursor.close();
        destLatLng = new LatLng(destLat, destLng);

        if(destMarker != null)destMarker.remove();
        MarkerOptions options = new MarkerOptions();
        options.position(destLatLng).title("Destination");
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop));
        destMarker = map.addMarker(options);
        destMarker.showInfoWindow();
//        MarkerOptions m = new MarkerOptions()
//                .position(userLatLng)
//                .title("Destination")
////                .snippet("")
//                ;
//        map.addMarker(m).showInfoWindow();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nearest_stop);android.support.v7.app.ActionBar ab = getSupportActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.show();
        float distance = getIntent().getFloatExtra("distance", 500);
        destStop = getIntent().getExtras().get("dest").toString();
        dest = (TextView) findViewById(R.id.destination);
        stop = (TextView) findViewById(R.id.stop);
        stop.setText(destStop);
        dest.setText(distance + " m");
//        key = getIntent().getExtras().get("key").toString();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        polylines = new ArrayList<>();
        createLocationRequest();
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

    }
    @Override
    protected void onStop(){
//        locationManager.removeUpdates(TrackingActivity.this);
        super.onStop();
    }
    @Override
    protected void onRestart(){
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, this);
//        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, this);
        super.onRestart();
    }
//    class SendFeedback extends AsyncTask<String, Integer, Boolean> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected Boolean doInBackground(String... post) {
//            Boolean result = true;
//            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//            OkHttpClient client = new OkHttpClient();
//            RequestBody body = RequestBody.create(JSON, post[0]);
//            String postUrl = HOST + "/feedback/send/";
//            Log.i("url", postUrl);
//            HttpUrl.Builder urlBuilder = HttpUrl.parse(postUrl).newBuilder();
//            String url = urlBuilder.build().toString();
//
//            Request request = new Request.Builder()
//                    .url(url)
//                    .post(body)
//                    .build();
//            //Send the request
//            try {
//                Response response = client.newCall(request).execute();
//                String responseData = response.body().string();
//                JSONObject JSONresponse = new JSONObject(responseData);
//                String status = JSONresponse.get("status").toString();
//                if(status.equals("DROP")) {
//
////                    locationManager.removeUpdates(mLocationListener);
//                    TrackingActivity.this.finish();
//                }
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//
//            return  result;
//        }
//        @Override
//        protected void onPostExecute(Boolean result){
////            Toast.makeText(context,"Started tracking", Toast.LENGTH_LONG).show();
////            ShowMapActivity.this.finish();
//        }
//    }

    public void route(LatLng start, LatLng end){//todo
        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(start, end)
//                .key("@string/google_maps_key")
                .build();
        routing.execute();
    }
    @Override
    public void onRoutingCancelled() {
        Log.i(":", "Routing was cancelled.");
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        // The Routing request failed
        Log.i(":", "Routing failed");
        if(e != null) {
            //Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            //Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    public void onRoutingStart() {
        Log.i(":", "Routing started");
        // The Routing Request starts
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex)
    {
//        progressDialog.dismiss();
        Log.i(":", "Routing was success");
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(userLatLng);
        boundsBuilder.include(destLatLng);
        LatLngBounds bounds = boundsBuilder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 30);

        map.animateCamera(cu);


        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
//            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(Color.BLACK);
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = map.addPolyline(polyOptions);
            polylines.add(polyline);

            //Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

        // Start marker
//        if(m1 != null)m1.remove();
//        MarkerOptions options = new MarkerOptions();
//        options.position(start).title("Bus location");
//        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus));
//        m1 = map.addMarker(options);
//        m1.showInfoWindow();
//
//        // End marker
//        if(m2 != null)m2.remove();
//        options = new MarkerOptions();
//        options.position(end).title("Bus stop");
//        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop));
//        m2 =  map.addMarker(options);
//        m2.showInfoWindow();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v("Connection failed",connectionResult.toString());
    }

    boolean mRequestingLocationUpdates = false;
    @Override
    public void onConnected(Bundle bundle) {
        if (!mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        mRequestingLocationUpdates = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        mRequestingLocationUpdates = false;
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }
    @Override
    public void onConnectionSuspended(int i) {

    }
}