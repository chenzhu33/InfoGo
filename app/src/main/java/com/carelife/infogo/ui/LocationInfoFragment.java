package com.carelife.infogo.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.carelife.infogo.R;
import com.carelife.infogo.dom.Photo;
import com.carelife.infogo.dom.Position;
import com.carelife.infogo.utils.Global;
import com.carelife.infogo.utils.LocationProducer;
import com.carelife.infogo.utils.Tools;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 添加,google map显示的activity
 */
public class LocationInfoFragment extends BaseInfoFragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    private static final String TAG = "Location";

    private static final int UPDATE_TRACK_LINE = 1000;

    private GoogleMap mMap;
    private MapView mapView;
    private LatLng lastLocation;
    private Marker previousMarker;
    private Marker selectedMarker;
    private GoogleApiClient mGoogleApiClient;
    private Location oldPoint;
    private Location newPoint;
    private Timer timer;
    private List<Geofence> mGeofenceList = new ArrayList<>();
    private PendingIntent mGeofencePendingIntent;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UPDATE_TRACK_LINE:
                    newPoint = LocationProducer.getInstance(getContext()).getLatestLocation();
                    if(newPoint == null){
                        newPoint = LocationProducer.getInstance(getContext()).getLastKnowLocation();
                    }
                    LatLng oldLatLng = new LatLng(oldPoint.getLatitude(),oldPoint.getLongitude());
                    LatLng newLatLng = new LatLng(newPoint.getLatitude(),newPoint.getLongitude());
                    if(Tools.getDistance(oldLatLng.latitude,oldLatLng.longitude,
                            newLatLng.latitude,newLatLng.longitude) < 50){
                        markLineOnMap(oldPoint, newPoint);
                    }
                    oldPoint = newPoint;
                    break;
            }
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        timer = new Timer();
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
        if(timer != null){
            timer.cancel();
        }
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
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(getActivity(), this)
                .build();
        initHot();
        return v;
    }

    private void requestLocation(){
        if(!LocationProducer.getInstance(getContext()).isLocating()){
            LocationProducer.getInstance(getContext()).start();
        }
    }

    private void markLineOnMap(Location oldPoint, Location newPoint){
        PolylineOptions options = new PolylineOptions();
        options.add(new LatLng(oldPoint.getLatitude(),oldPoint.getLongitude()),
                new LatLng(newPoint.getLatitude(),newPoint.getLongitude()))
                .width(10).color(Color.RED);
        mMap.addPolyline(options);
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
                oldPoint = newPoint = LocationProducer.getInstance(getContext()).getLastKnowLocation();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        handler.sendEmptyMessage(UPDATE_TRACK_LINE);
                    }
                },0,5000);
                Toast.makeText(getContext(),"start...",Toast.LENGTH_SHORT).show();
            }
        });

        Button stopTrackButton = (Button) v.findViewById(R.id.stop_track);
        stopTrackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timer != null){
                    timer.cancel();
                }
                Toast.makeText(getContext(),"stop...",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Location getCurrentLocation() {
        return LocationProducer.getInstance(getContext()).getLastKnowLocation();
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

        addGeofence(position);
        requestGeofence();
    }

    private void addGeofence(Position position) {
        mGeofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(position.getId() + "")
                .setCircularRegion(position.getLat(), position.getLon(), Global.GEOFENCE_RADIUS_IN_METERS)
                .setExpirationDuration(Global.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .build());
    }

    private void requestGeofence() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        GeofencingRequest request = builder.build();

        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                request,
                getGeofencePendingIntent()
        ).setResultCallback(this);
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(getActivity(), GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(getActivity(), 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    private void showPlaces(List<com.carelife.infogo.dom.Place> places) {
        LinearLayout linearLayoutMain = new LinearLayout(getContext());
        linearLayoutMain.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ListView listView = new ListView(getContext());
        listView.setFadingEdgeLength(0);

        List<Map<String, String>> dataList = new ArrayList<>();
        for (int m = 0; m < places.size(); m++) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("name", places.get(m).getName());
            dataMap.put("address", places.get(m).getAddress());
            dataList.add(dataMap);
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(),
                dataList, R.layout.place_item,
                new String[]{"name", "address"},
                new int[]{R.id.name, R.id.address});
        listView.setAdapter(adapter);

        linearLayoutMain.addView(listView);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Places").setView(linearLayoutMain)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
        dialog.show();
    }

    private void initHot() {
        mGeofenceList.clear();
        List<Position> positions = new Select().from(Position.class).where("isHot = ?", "1").execute();
        for (Position position : positions) {
            addGeofence(position);
            //requestGeofence();
        }
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
                Location location = new Location(LocationProducer.getInstance(getContext()).getProvider());
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                recordPosition(location);
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker.getTag() instanceof Photo) {
                    showDetailPhoto((Photo) marker.getTag());
                } else if (marker.getTag() instanceof Position){
                    selectedMarker = marker;
                }
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

        List<Photo> photos = new Select().from(Photo.class).execute();
        for(Photo photo : photos) {
            mMap.addMarker(
                    new MarkerOptions()
                            .position(new LatLng(photo.getLatitude(), photo.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromFile(photo.getThumbUrl())))
                    .setTag(photo);
        }
    }

    private boolean hasPermission() {
        return ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void showDetailPhoto(Photo photo) {
        // TODO show detail photo info
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG,connectionResult.getErrorMessage());
    }

    @Override
    public void onResult(@NonNull Status status) {

    }
}
