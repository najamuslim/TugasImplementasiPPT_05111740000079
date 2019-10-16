package com.example.googlemapsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private static final int TAG_CODE_PERMISSION_LOCATION = 1;
    private EditText etLatitude, etLongitude, etZoom, etAddress;
    private ImageButton btnGo, btnSearch;


    private GoogleMap mMap;
    private Marker marker;
    private float zoom = 15f;

    private TextView tvLatitude, tvLongitude;

    private LocationManager locationManager;
    private Location currentLocation;
    private MyLocationListener locationListener;
    private Marker currentMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initView();
        initOnClick();
        checkLocationPermission();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, locationListener);

        // Init current location
        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        tvLatitude.setText(String.valueOf(currentLocation.getLatitude()));
        tvLongitude.setText(String.valueOf(currentLocation.getLongitude()));


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initView() {

        tvLatitude = findViewById(R.id.tvLatitude);
        tvLongitude = findViewById(R.id.tvLongitude);

        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etZoom = findViewById(R.id.etZoom);
        etAddress = findViewById(R.id.etAddress);
        btnGo = findViewById(R.id.btnGo);
        btnSearch = findViewById(R.id.btnSearch);
    }

    private void initOnClick() {
        btnGo.setOnClickListener(this);
        btnSearch.setOnClickListener(this);
    }

    private void checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        TAG_CODE_PERMISSION_LOCATION);
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        currentMarker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .title("My Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), zoom));
    }

    public void searchAddress(String address) {
        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Address location = null;
        if (addresses != null && addresses.size() > 0) {
            location = addresses.get(0);
        } else {
            Toast.makeText(this, "Address not found!", Toast.LENGTH_SHORT).show();
        }

        if (location != null) {
            Toast.makeText(this, location.getAddressLine(0), Toast.LENGTH_SHORT).show();
            goToLocation(location.getLatitude(), location.getLongitude(), zoom);
        }
    }

    public void goToLocation(double latitude, double longitude, float zoom) {
        try {
            LatLng pos = new LatLng(latitude, longitude);
            if (marker != null)
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(pos).title("Current Marker"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoom));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnGo:
                String mLatitude = etLatitude.getText().toString();
                String mLongitude = etLongitude.getText().toString();
                String mZoom = etZoom.getText().toString();

                if (!mLatitude.isEmpty() && !mLongitude.isEmpty()) {
                    double lat = Double.parseDouble(etLatitude.getText().toString());
                    double lng = Double.parseDouble(etLongitude.getText().toString());

                    float z = zoom;
                    if (!mZoom.isEmpty())
                        z = Float.parseFloat(etZoom.getText().toString());

                    goToLocation(lat, lng, z);
                }

                break;

            case R.id.btnSearch:
                String address = etAddress.getText().toString();
                if (!address.isEmpty()) {
                    searchAddress(address);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;

            case R.id.hybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case R.id.terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case R.id.satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.none:
                mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            tvLatitude.setText(String.valueOf(currentLocation.getLatitude()));
            tvLongitude.setText(String.valueOf(currentLocation.getLongitude()));

            //mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())));

            if (currentMarker != null)
                currentMarker.remove();
            /*currentMarker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .title("My Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));*/
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}