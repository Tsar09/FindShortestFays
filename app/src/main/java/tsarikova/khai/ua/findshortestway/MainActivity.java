package tsarikova.khai.ua.findshortestway;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String LOG_TAG = "MainApp";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Marker currentLocationMarker;
    private Transport transport;
    private Location mLastLocation;
    private Route route;

    private TextView tvDetails;
    private GoogleMap m_map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        route = new Route();
        tvDetails = (TextView) findViewById(R.id.details);
        tvDetails.setVisibility(1);
        //  Button btnMap = (Button) findViewById(R.id.btnMap);

//        btnMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mapReady) {
//
//                }
//            }
//        });
//
//        Button btnSatelite = (Button) findViewById(R.id.btnSatelite);
//        btnSatelite.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (mapReady) {
//                    m_map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//                }
//            }
//        });


        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String pref = preferences.getString("list", "WALKING");
        transport = Transport.valueOf(pref);
        Log.i(LOG_TAG, "Pref " + transport);
    }

    //    @Override
//    protected void onStart() {
//        super.onStart();
//        if (mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
//        }
//    }

//    @Override
//    protected void onStop() {
//        mGoogleApiClient.disconnect();
//        super.onStop();
//    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.i(LOG_TAG, "Map is ready");
        m_map = googleMap;
        m_map.setMapType(GoogleMap.MAP_TYPE_HYBRID);


//        LatLng kharkov = new LatLng(49.988358, 36.232845);
//        CameraPosition target = CameraPosition.builder().target(kharkov).zoom(14).build();
//        m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

        buidGoogleApiClient();
        mGoogleApiClient.connect();
        m_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.i(LOG_TAG, "add marker " + latLng);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                String address = getAddress(latLng).get(0).getAddressLine(0);
                markerOptions.title(address);

                m_map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                m_map.addMarker(markerOptions);
                route.getPoints().add(latLng);

                RouteHelper helper = new RouteHelper(m_map, MainActivity.this, tvDetails);
                route = helper.drawRoute(route.getPoints(), route.getPolylines(), transport, RouteHelper.LANGUAGE_ENGLISH);

                tvDetails.setVisibility(0);
//                StringBuilder builder = new StringBuilder();
//                builder.append("Distance: " + route.getDistance() + "km; ");
//                builder.append("duration: " + route.getDuration() + "min");
//                tvDetails.setText(builder.toString());
            }
        });
    }

    private synchronized void buidGoogleApiClient() {
        Log.i(LOG_TAG, "buidGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.menu_item_main):
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(LOG_TAG, "onConnected");
        //
        if (mLastLocation == null) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.i(LOG_TAG, String.valueOf(mLastLocation.getLatitude()));
            LatLng position = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            CameraPosition target = CameraPosition.builder().target(position).zoom(14).build();
            m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        }

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);// LOW_POWER
        mLocationRequest.setInterval(5000); // update location every second
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleAppClient connection has benn suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "GoogleAppClient connection has failed");
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        } else {
            route.getPoints().add(position);
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(position);
        markerOptions.title("You are here");
        currentLocationMarker = m_map.addMarker(markerOptions);

        mLastLocation = location;
        Log.i(LOG_TAG, "location changed: " + String.valueOf(position));
    }

    private List<Address> getAddress(LatLng latLng) {
        List<Address> addresses = null;
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this);
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            String address = addresses.get(0).getAddressLine(0);
            String city = addresses.get(0).getAddressLine(1);
            String country = addresses.get(0).getAddressLine(2);
            Log.d(LOG_TAG, "address = " + address + ", city = " + city + ", country = " + country);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
