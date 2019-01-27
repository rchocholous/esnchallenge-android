package org.esncz.esnchallenge;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.esncz.esnchallenge.facade.BackendFacade;
import org.esncz.esnchallenge.network.VolleyCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esncz.esnchallenge.model.LocationPoint;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static String KEY_MAP_LOCATION = "MAP_LOCATION";

    private BackendFacade facade;

    public static int locationsCount;

    private static final float DEFAULT_ZOOM = 6.5f;
    public static final String RESPONSE_DIALOG_TAG = "simpleMessageDialog";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location lastKnownLocation;

    private ProgressBar progressBar;
    private Button buttonCheck;

    private Map<String, LocationPoint> locations;

    private CameraPosition cameraPosition;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            transaction.replace(R.id.map, mapFragment).commit();
        }

        mapFragment.getMapAsync(this);

        return view;
    }

    private void showProgressBar(boolean isShowed) {
        if(isShowed) {
            progressBar.setVisibility(View.VISIBLE);
            buttonCheck.setClickable(false);
            mMap.getUiSettings().setAllGesturesEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            buttonCheck.setClickable(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        if(savedInstanceState != null) {
            cameraPosition = savedInstanceState.getParcelable(KEY_MAP_LOCATION);
            if(mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        this.facade = new BackendFacade(getActivity().getApplicationContext(), "MAP");
        buttonCheck = this.getActivity().findViewById(R.id.button_check);
        progressBar = this.getActivity().findViewById(R.id.progress_bar_map);
        progressBar.setVisibility(View.GONE);

        callLocationsEndpoint();// load locations
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.facade.cancelRequests();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle stateToSave) {
        super.onSaveInstanceState(stateToSave);
        if(mMap != null) {
            stateToSave.putParcelable(KEY_MAP_LOCATION, mMap.getCameraPosition());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        updateMapLocations();

        buttonCheck.setVisibility(View.VISIBLE);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });

        getLocationPermission();
        updateLocationUI();

        LatLng defaultCameraLocation = new LatLng(49.7406922,15.3661319);
        if(cameraPosition != null) {
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultCameraLocation, DEFAULT_ZOOM));
        }
    }


    public void updateLocationsCheckedState() {
        if(locations != null) {
            if(ProfileFragment.isLoggedIn() && ProfileFragment.profileData != null) {
                List<LocationPoint> checkedLocations = ProfileFragment.profileData.getCheckedLocations();
                if(checkedLocations != null) {
                    for(LocationPoint location : checkedLocations) {
                        locations.get(location.getTitle()).check();
                    }
                }

            } else {
                for(LocationPoint location : locations.values()) {
                    location.uncheck();
                }
            }
        }
    }

    private void addLocationToMap(LocationPoint location) {
        if(locations.containsKey(location.getTitle()) && !locations.get(location.getTitle()).isShownOnMap()) {
            locations.get(location.getTitle()).setMarker(mMap.addMarker(location.buildMarkerOptions()));
            locations.get(location.getTitle()).setCircle(mMap.addCircle(location.buildCircleOptions(getResources())));
        }
    }

    private void updateMapLocations() {
        if(locations != null &&  !locations.isEmpty() && mMap != null) {
            updateLocationsCheckedState();

            for(LocationPoint location : locations.values()) {
                addLocationToMap(location);
            }
        }
    }

    private void callLocationsEndpoint() {
        this.facade.sendGetLocations(
                new VolleyCallback<LocationPoint[]>() {
                    @Override
                    public void onSuccess(LocationPoint[] result) throws JSONException {
                        if(locations == null) {
                            locations = new HashMap<>();
                        }

                        for(LocationPoint location : result) {
                            if(!locations.containsKey(location.getTitle())) {
                                locations.put(location.getTitle(),location);
                            }
                        }
                        locationsCount = locations.size();
                        updateMapLocations();
                    }

                    @Override
                    public void onError(String result) throws Exception {
                        Toast.makeText(getContext(),"Failed to load locations.",Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    private void showLocationCheckDialog(String message) {
        showProgressBar(false);
        SimpleMessageDialog dialog = (SimpleMessageDialog) getFragmentManager().findFragmentByTag(RESPONSE_DIALOG_TAG);
        if(dialog == null) {
            dialog = new SimpleMessageDialog();
            dialog.setResponseDetails( message);
            getFragmentManager().beginTransaction().add(dialog,RESPONSE_DIALOG_TAG).commit();
        } else {
            dialog.setResponseDetails(message);
            getFragmentManager().beginTransaction().show(dialog).commit();
        }
    }

    private void calculateLocationDistances() {
        //Find nearest location (50.3722211,17.1849737)
        final Location actual = lastKnownLocation; //new Location("");
//                actual.setLatitude(17.1849737);
//                actual.setLongitude(50.3722211);

        if(actual != null) {
            if(locations != null && !locations.isEmpty()) {
                List<LocationPoint> list = new ArrayList<>(locations.values());
                Collections.sort(list, new Comparator<LocationPoint>() {
                    @Override
                    public int compare(LocationPoint o1, LocationPoint o2) {
                        return (int) (o1.distanceTo(actual) - (o2.distanceTo(actual)));
                    }
                });

                for (LocationPoint l : list) {
                    Log.v("SORTED", l.getTitle() + ", distance: " + l.distanceTo(actual));
                }

                boolean checked = false;
                if (!list.isEmpty()) {
                    for (LocationPoint location : list) {
                        if (location.isInsideRadius(actual)) {//TODO: Works only if radius is always same
                            //TODO: locations.get(list.get(0).getTitle()).getCircle().setFillColor(0x7FA0A500);// Only if is inside range
                            checked = true;
                            callLocationCheckEndpoint(location);
//                            showLocationCheckDialog();
                        } else {
                            break;
                        }
                    }
                    if (!checked) {
                        showProgressBar(false);
                        showLocationCheckDialog("Too far from any location.");
                    }
                }

            } else {
                showProgressBar(false);
                Toast.makeText(getContext(), "You need internet to load locations.", Toast.LENGTH_LONG).show();
            }
        } else {
            showProgressBar(false);
            Toast.makeText(getContext(), "Failed to read your location.", Toast.LENGTH_LONG).show();
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        try {
            showProgressBar(true);
            if (mLocationPermissionGranted) {
                Task locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = (Location)task.getResult();
                            calculateLocationDistances();
                        } else {
                            showProgressBar(false);
                            Toast.makeText(getContext(), "Failed to get your location.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            showProgressBar(false);
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void callLocationCheckEndpoint(final LocationPoint location) {
        if(((MainActivity)getActivity()).getAccessToken() != null && !((MainActivity)getActivity()).getAccessToken().isEmpty()) {
            this.facade.sendCheckLocation(((MainActivity) getActivity()).getAccessToken(), location,
                    new VolleyCallback<JSONObject>() {
                        @Override
                        public void onSuccess(JSONObject result) throws JSONException {
                            if(location.check()) {
                                ProfileFragment.profileData.getCheckedLocations().add(location);
                            }
                            showProgressBar(false);
                            showLocationCheckDialog(result.getString("message"));
                        }

                        @Override
                        public void onError(String result) throws Exception {
                            showProgressBar(false);
                            showLocationCheckDialog("Error: Failed to check location.");
                        }
                    }
            );
        } else {
            showProgressBar(false);
            showLocationCheckDialog("You must log in to check the location and participate in challenge.");
        }
    }

}
