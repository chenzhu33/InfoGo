package com.carelife.infogo.ui;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.carelife.infogo.R;
import com.carelife.infogo.dom.Position;
import com.carelife.infogo.utils.Global;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 添加,google map显示的activity
 */
public class LocationInfoFragment extends BaseInfoFragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "Lation";

    private GoogleMap mMap;
    private MapView mapView;
    private LocationManager locationManager;
    private LatLng lastLocation;
    private Marker previousMarker;
    private Marker selectedMarker;
    private GoogleApiClient mGoogleApiClient;

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

        initView(v);

        // Create a GoogleApiClient instance
        mGoogleApiClient = new GoogleApiClient
                .Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();

        return v;
    }

    private void initView(View v) {
        Button recordButton = (Button) v.findViewById(R.id.record);
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = getCurrentLocation();
                recordPosition(location);
            }
        });

        Button placeButton = (Button) v.findViewById(R.id.place_info);
        placeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPlace();
            }
        });

        final Button markHot = (Button) v.findViewById(R.id.mark_hot);
        markHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                markHot();
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
    }

    private Location getCurrentLocation() {
        String locationProvider = getProvider();
        if (locationProvider.isEmpty()) {
            return null;
        }
        if (!hasPermission()) {
            return null;
        }
        return locationManager.getLastKnownLocation(locationProvider);
    }

    private void requestPlace() {
        // Request cache
        List<com.carelife.infogo.dom.Place> places = queryCache(getCurrentLocation());
        if(!places.isEmpty()) {
            showPlaces(places);
            return;
        }
        // Request online
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                List<com.carelife.infogo.dom.Place> places = new ArrayList<>();
                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    places.add(parseAndCachePlace(placeLikelihood));
                }
                likelyPlaces.release();

                // Show
                showPlaces(places);
            }
        });
    }

    private void markHot() {
        if (selectedMarker == null) {
            return;
        }
        Position position = (Position) selectedMarker.getTag();
        position.setHot(true);
        position.save();
    }

    private void showPlaces(List<com.carelife.infogo.dom.Place> places) {
        // TODO

    }

    private List<com.carelife.infogo.dom.Place> queryCache(Location current) {
        List<com.carelife.infogo.dom.Place> nearbyPlaces = new ArrayList<>();
        if(current == null) {
            return nearbyPlaces;
        }

        List<com.carelife.infogo.dom.Place> places = new Select().from(com.carelife.infogo.dom.Place.class).execute();
        for(int i=0;i<places.size();i++) {
            com.carelife.infogo.dom.Place place = places.get(i);
            if(isNearby(place.getPosition().getLat(), place.getPosition().getLon(),
                    current.getLatitude(), current.getLongitude())) {
                nearbyPlaces.add(place);
            }
        }
        return nearbyPlaces;
    }

    private boolean isNearby(double x1, double y1, double x2, double y2) {
        return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) - Global.PLACE_RADIUS * Global.PLACE_RADIUS <= 0;
    }

    private com.carelife.infogo.dom.Place parseAndCachePlace(PlaceLikelihood placeLikelihood) {
        Place placeGoogle = placeLikelihood.getPlace();
        com.carelife.infogo.dom.Place place = new Select()
                .from(com.carelife.infogo.dom.Place.class)
                .where("name = ?", placeGoogle.getName().toString())
                .executeSingle();
        if (place != null) {
            return place;
        }
        place = new com.carelife.infogo.dom.Place();
        place.setName(placeGoogle.getName().toString());
        if (placeGoogle.getAddress() != null) {
            place.setAddress(placeGoogle.getAddress().toString());
        }
        if (placeGoogle.getAttributions() != null) {
            place.setDescription(placeGoogle.getAttributions().toString());
        }
        Position position = new Position();
        position.setLat(placeGoogle.getLatLng().latitude);
        position.setLon(placeGoogle.getLatLng().longitude);
        position.save();
        place.setPosition(position);
        place.save();
        return place;
    }

    private void recordPosition(Location location) {
        if (location != null) {
            Position position = new Position();
            position.setLat(location.getLatitude());
            position.setLon(location.getLongitude());
            position.setRecord(true);
            position.save();

            if(previousMarker != null) {
                mMap.addMarker(new MarkerOptions().position(lastLocation)).setTag(previousMarker.getTag());
                previousMarker.remove();
            }

            lastLocation = new LatLng(position.getLat(), position.getLon());
            previousMarker = mMap.addMarker(new MarkerOptions()
                    .position(lastLocation)
                    .title("Last recorded location")
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
            Location current = getCurrentLocation();
            if(current == null) {
                return;
            }
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                selectedMarker = marker;
                return false;
            }
        });
    }

    private void initMarker() {
        List<Position> positions = new Select().from(Position.class).where("record = ?", "1").execute();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,connectionResult.getErrorMessage());
    }
}
