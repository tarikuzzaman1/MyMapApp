package com.rosc.android.map.mymapapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
//import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rosc.android.map.mymapapp.realm.LocationTask;

//import permissions.dispatcher.NeedsPermission;
//import permissions.dispatcher.RuntimePermissions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by TARIK-ROSC on 1/2/2018.
 */

public class MapsDemoActivity extends AppCompatActivity {

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private LocationRequest mLocationRequest;
    Location mCurrentLocation;
    //private long UPDATE_INTERVAL = 60000;  /* 60 secs */
    private long UPDATE_INTERVAL = 2 * 60000;  /* 2 minutes */
    //private long FASTEST_INTERVAL = 5000; /* 5 secs */


    // Location updates intervals in sec
    //private static int UPDATE_INTERVAL = 10000; // 10 sec
    //private static int FATEST_INTERVAL = 5000; // 5 sec
    //private static int DISPLACEMENT = 10; // 10 meters
    private static int DISPLACEMENT = 60; // 10 meters


    private final static String KEY_LOCATION = "location";

    /*
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    Activity xActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_demo);

        xActivity = MapsDemoActivity.this;
        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_LOCATION)) {
            // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
            // is not null.
            mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        } else {
            Toast.makeText(this, "Error - Map Fragment was null!!", Toast.LENGTH_SHORT).show();
        }

    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            // Map is ready
            Toast.makeText(this, "Map Fragment was loaded properly!", Toast.LENGTH_SHORT).show();
            if (checkPermission()) {
                //MapDemoActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
                //MapDemoActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
                getMyLocation();
                startLocationUpdates();

                /*LatLng origin = new LatLng(23.796228,90.373368);
                LatLng dest = new LatLng(23.779025,90.378986);
                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);*/
            } else {
                requestPermission(MapsDemoActivity.this);
            }

            textFunction();
            drawPolygon();
            drawPolygon1();
        } else {
            Toast.makeText(this, "Error - Map was null!!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //MapDemoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    //@NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        //noinspection MissingPermission
        if (checkPermission()) {
            map.setMyLocationEnabled(true);

            FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
            //noinspection MissingPermission
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("MapDemoActivity", "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        }
    }
    /*
     * Called when the Activity becomes visible.
    */
    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
     * Called when the Activity is no longer visible.
	 */
    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getSupportFragmentManager(), "Location Updates");
            }

            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Display the connection status

        if (mCurrentLocation != null) {
            Toast.makeText(this, "GPS location was found!", Toast.LENGTH_SHORT).show();
            LatLng latLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17);
            map.animateCamera(cameraUpdate);
        } else {
            Toast.makeText(this, "Current location was null, enable GPS on emulator!", Toast.LENGTH_SHORT).show();
        }
        if (checkPermission()) {
            //MapDemoActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
            startLocationUpdates();
        } else {
            requestPermission(MapsDemoActivity.this);
        }
    }
    //@NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        if (checkPermission()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            //mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            settingsClient.checkLocationSettings(locationSettingsRequest);
            //noinspection MissingPermission
            getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            onLocationChanged(locationResult.getLastLocation());
                        }
                    },
                    null);

        } else {
            requestPermission(MapsDemoActivity.this);
        }
    }
    public void onLocationChanged(Location location) {
        // GPS may be turned off
        if (location == null) {
            return;
        }

        // Report to the UI that the location was updated

        mCurrentLocation = location;
        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        double[] loc = {location.getLatitude(), location.getLongitude()};
        //new MyTask().execute(loc);
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        super.onSaveInstanceState(savedInstanceState);
    }

    // Define a DialogFragment that displays the error dialog
    public static class ErrorDialogFragment extends android.support.v4.app.DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        // Default constructor. Sets the dialog field to null
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        // Set the dialog to display
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        // Return a Dialog to the DialogFragment.
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }


    private void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
    }
    private boolean checkPermission() {
        int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION);
        int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
    }

    public class MyTask extends AsyncTask<Object, Integer, String> {
        ArrayList<LatLng> markerPoints= new ArrayList<LatLng>();

        double latitude, longitude;

        @Override
        protected String doInBackground(Object... params) {
            try {
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();

                double[] locations = (double[]) params[0];
                latitude = locations[0];
                longitude = locations[1];
                // TODO Insert Data


                //String[] lcParam = (String[]) params[0];
                //String lcQuery = lcParam[0].trim();
                //int lcStatus = (int)params[2];
                // StdInfo[] stdList = (StdInfo[]) params[1];
                String cuSysTime = (String) android.text.format.DateFormat.format("yyyy-MM-dd HH:mm:ss", new Date().getTime());
                LocationTask task = new LocationTask(latitude, longitude, cuSysTime);
                //realm.insertOrUpdate(task);


                /*for (int i = 0; i < results.size(); i++) {
                    LatLng latLng = new LatLng(latitude, longitude);
                    markerPoints.add(latLng);
                    // Creating MarkerOptions
                    MarkerOptions options = new MarkerOptions();

                    // Setting the position of the marker
                    options.position(latLng);
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    // Add new marker to the Google Map Android API V2
                    //map.addMarker(options);
                    GoogleMap xMap = map;
                    xMap.addMarker(options);
                }*/


                // Checks, whether start and end locations are captured
                /*if (markerPoints.size() < 2) {
                    realm.commitTransaction();
                    return "";
                }
                LatLng temp = markerPoints.get(0);
                for (int i = 1; i < markerPoints.size(); i++) {
                    //LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(i);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(temp, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }*/


                /*if(markerPoints.size() >= 2) {
                    LatLng origin = markerPoints.get(0);
                    LatLng dest = markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }*/



                realm.commitTransaction();

            } catch(Exception ex) {
                ex.getStackTrace();
                ex.getLocalizedMessage();
            } finally {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Realm xRealm = Realm.getDefaultInstance();
            RealmResults<LocationTask> results = xRealm.where(LocationTask.class).findAll();

            for (int i = 0; i < results.size(); i++) {
                LatLng latLng = new LatLng(results.get(i).getLatitude(), results.get(i).getLongitude());
                markerPoints.add(latLng);
                // Creating MarkerOptions
                final MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(latLng);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                // Add new marker to the Google Map Android API V2
                //map.addMarker(options);
                //xMap = map;
                xActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.addMarker(options);
                    }
                });
            }


            if (markerPoints.size() < 2) {
                return;
            }

            LatLng one = markerPoints.get(markerPoints.size()-2);
            LatLng two = new LatLng(results.get(markerPoints.size()-1).getLatitude(), results.get(markerPoints.size()-1).getLongitude());
            String url = getDirectionsUrl(one, two);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);




            /*LatLng temp = markerPoints.get(0);
            for (int i = 1; i < markerPoints.size(); i++) {
                //LatLng origin = markerPoints.get(0);
                LatLng dest = markerPoints.get(i);

                // Getting URL to the Google Directions API
                //String url = getDirectionsUrl(temp, dest);

                //DownloadTask downloadTask = new DownloadTask();

                // Start downloading json data from Google Directions API
                //downloadTask.execute(url);

                //LatLng origin = new LatLng(23.796228,90.373368);
                //LatLng dest = new LatLng(23.779025,90.378986);
                //LatLng dest = markerPoints.get(i);
                String url = getDirectionsUrl(temp, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);

                temp = dest;

                *//*LatLng origin = new LatLng(23.796228,90.373368);
                LatLng dest = new LatLng(23.779025,90.378986);
                String url = getDirectionsUrl(origin, dest);
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);*//*
            }*/
        }
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb  = new StringBuffer();

            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            Log.d("Ex downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>{

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try{
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes

            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.geodesic(false);
            for(int i=0;i<result.size();i++) {
                points = new ArrayList<LatLng>();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    //points.add(position);
                    lineOptions.add(position);
                }

                // Adding all the points in the route to LineOptions
                //lineOptions.addAll(points);

            }

            lineOptions.width(4);
            lineOptions.color(Color.RED);
            map.addPolyline(lineOptions);
            // Drawing polyline in the Google Map for the i-th route

        }
    }
    //GoogleMap xMap = null;

/**
 * realm.where(Book.class).findAll();
 * realm.where(Book.class).equalTo("id", id).findFirst();
 */


    public void textFunction() {
        ArrayList<LatLng> markerPoints = new ArrayList<LatLng>();
        Realm xRealm = Realm.getDefaultInstance();
        RealmResults<LocationTask> results = xRealm.where(LocationTask.class).findAll();

        LatLng xTemp = new LatLng(results.get(0).getLatitude(), results.get(0).getLongitude());
        for (int i = 1; i < results.size(); i++) {
            LatLng latLng = new LatLng(results.get(i).getLatitude(), results.get(i).getLongitude());
            markerPoints.add(latLng);
            // Creating MarkerOptions
            final MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(latLng);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            // Add new marker to the Google Map Android API V2
            //map.addMarker(options);
            //xMap = map;
            xActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    map.addMarker(options);
                }
            });

            LatLng xDest = new LatLng(results.get(i).getLatitude(), results.get(i).getLongitude());
            String url = getDirectionsUrl(xTemp, xDest);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
            xTemp = xDest;
        }


        /*if (markerPoints.size() < 2) {
            return;
        }

        LatLng one = markerPoints.get(markerPoints.size()-2);
        LatLng two = new LatLng(latitude,longitude);
        String url = getDirectionsUrl(one, two);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);*/
    }


    public void drawPolygon(){
        ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(23.782441, 90.368526);
        arrayPoints.add(point1);
        LatLng point2 = new LatLng(23.780075, 90.368143);
        arrayPoints.add(point2);
        LatLng point3 = new LatLng(23.780525, 90.369950);
        arrayPoints.add(point3);
        LatLng point4 = new LatLng(23.780832, 90.369971);
        arrayPoints.add(point4);
        LatLng point5 = new LatLng(23.782470, 90.370586);
        arrayPoints.add(point5);


        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(arrayPoints);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.strokeWidth(4);
        polygonOptions.fillColor(Color.TRANSPARENT);
        Polygon polygon = map.addPolygon(polygonOptions);


        //Read more: http://www.androidhub4you.com/2013/07/draw-polygon-in-google-map-version-2.html#ixzz53mfM1gou

    }
    public void drawPolygon1(){
        ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(23.776233, 90.378034);
        LatLng point2 = new LatLng(23.775880, 90.378047);
        LatLng point3 = new LatLng(23.775529, 90.378138);
        LatLng point4 = new LatLng(23.774870, 90.378234);
        LatLng point5 = new LatLng(23.774734, 90.378254);
        LatLng point6 = new LatLng(23.774832, 90.378595);
        LatLng point6a = new LatLng(23.774971, 90.378574);
        LatLng point7 = new LatLng(23.775004, 90.379193);
        LatLng point8 = new LatLng(23.775137, 90.379759);
        LatLng point8a = new LatLng(23.774928, 90.379786);
        LatLng point9 = new LatLng(23.775041, 90.380086);
        LatLng point10 = new LatLng(23.775088, 90.380740);
        LatLng point11 = new LatLng(23.775480, 90.380599);
        LatLng point12 = new LatLng(23.775838, 90.380371);
        LatLng point13 = new LatLng(23.776109, 90.380366);
        LatLng point14 = new LatLng(23.776232, 90.380326);
        LatLng point15 = new LatLng(23.776193, 90.380074);
        LatLng point16 = new LatLng(23.776160, 90.379837);
        LatLng point17 = new LatLng(23.776142, 90.379635);
        LatLng point18 = new LatLng(23.776299, 90.379610);
        LatLng point19 = new LatLng(23.776514, 90.379586);
        LatLng point20 = new LatLng(23.776445, 90.379260);
        LatLng point21 = new LatLng(23.776348, 90.378662);
        LatLng point22 = new LatLng(23.776299, 90.378342);


        arrayPoints.add(point1);
        arrayPoints.add(point2);
        arrayPoints.add(point3);
        arrayPoints.add(point4);
        arrayPoints.add(point5);
        arrayPoints.add(point6);
        arrayPoints.add(point6a);
        arrayPoints.add(point7);
        arrayPoints.add(point8);
        arrayPoints.add(point8a);
        arrayPoints.add(point9);
        arrayPoints.add(point10);
        arrayPoints.add(point11);
        arrayPoints.add(point12);
        arrayPoints.add(point13);
        arrayPoints.add(point14);
        arrayPoints.add(point15);
        arrayPoints.add(point16);
        arrayPoints.add(point17);
        arrayPoints.add(point18);
        arrayPoints.add(point19);
        arrayPoints.add(point20);
        arrayPoints.add(point21);
        arrayPoints.add(point22);


        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.addAll(arrayPoints);
        polygonOptions.strokeColor(Color.BLUE);
        polygonOptions.strokeWidth(4);
        polygonOptions.fillColor(Color.TRANSPARENT);
        Polygon polygon = map.addPolygon(polygonOptions);


        //Read more: http://www.androidhub4you.com/2013/07/draw-polygon-in-google-map-version-2.html#ixzz53mfM1gou

    }


}
