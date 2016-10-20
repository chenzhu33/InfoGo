package com.carelife.infogo.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.carelife.infogo.R;
import com.carelife.infogo.dom.Position;
import com.carelife.infogo.utils.Global;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

/**
 * 添加,google map显示的activity
 */
public class LocationInfoFragment extends BaseInfoFragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;
    private LocationManager locationManager;
    private LatLng lastLocation;
    private Marker previousMarker;


    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onLocationChanged(Location location) {
            // TODO

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_maps, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.map_view);
        mapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        requestLocation();

        Button recordButton = (Button) v.findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationProvider = getProvider();
                if (locationProvider.isEmpty()) {
                    return;
                }
                if (!hasPermission()) {
                    return;
                }
                Location location = locationManager.getLastKnownLocation(locationProvider);
                recordPosition(location);
            }
        });

        Button placeButton = (Button) v.findViewById(R.id.place_info);
        placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        Button startTrackButton = (Button) v.findViewById(R.id.start_track);
        startTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        Button stopTrackButton = (Button) v.findViewById(R.id.stop_track);
        stopTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
            }
        });

        return v;
    }

    private void recordPosition(Location location) {
        if (location != null) {
            Position position = new Position();
            position.setLat(location.getLatitude());
            position.setLon(location.getLongitude());
            position.save();

            if(previousMarker != null) {
                mMap.addMarker(new MarkerOptions().position(lastLocation)).setTag(previousMarker.getTag());
                previousMarker.remove();
            }

            lastLocation = new LatLng(position.getLat(), position.getLon());
            previousMarker = mMap.addMarker(new MarkerOptions()
                    .position(lastLocation)
                    .title("Last location")
                    .icon(BitmapDescriptorFactory.fromResource(R.mipmap.last_marker)));
            previousMarker.setTag(position);
        }
    }

    private void requestLocation() {
        //获取地理位置管理器
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = getProvider();
        if (locationProvider.isEmpty()) {
            return;
        }
        if (hasPermission()) {
            locationManager.requestLocationUpdates(locationProvider, Global.LOCATION_REQUEST_TIME, 0, locationListener);
        }
    }

    private String getProvider() {
        //获取所有可用的位置提供器
        List<String> providers = locationManager.getProviders(true);
        String locationProvider = "";
        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        }
        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        return locationProvider;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        if (hasPermission()) {
            mMap.setMyLocationEnabled(true);
            Location current = locationManager.getLastKnownLocation(getProvider());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(current.getLatitude(), current.getLongitude()), 13));
        }
        initListener();
        initMarker();
    }

    private void initListener() {
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(getActivity(), "Event location change to "+latLng.latitude+","+latLng.longitude, Toast.LENGTH_SHORT).show();
                Location location = new Location(getProvider());
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                recordPosition(location);
            }
        });
    }

    private void initMarker() {
        List<Position> positions = new Select().from(Position.class).execute();
        int length = positions.size();
        if(length <= 0) {
            return;
        }
        for (int i = 0; i < length - 1; i++) {
            mMap.addMarker(
                    new MarkerOptions().position(new LatLng(positions.get(i).getLat(), positions.get(i).getLon())))
                    .setTag(positions.get(i));
        }
        lastLocation = new LatLng(positions.get(length - 1).getLat(), positions.get(length - 1).getLon());

        previousMarker = mMap.addMarker(new MarkerOptions()
                .position(lastLocation)
                .title("Last location")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.last_marker)));
        previousMarker.setTag(positions.get(length - 1));
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
