package cz.chochy.esnchallenge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.chochy.esnchallenge.model.LocationPoint;
import cz.chochy.esnchallenge.tools.GsonRequest;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    public static int locationsCount;

    private static final int DEFAULT_ZOOM = 15;
//    private RequestQueue queue;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private FusedLocationProviderClient fusedLocationClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location lastKnownLocation;

    private Button buttonCheck;

    private Map<String, LocationPoint> locations;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        locations = new HashMap<>();

        mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment == null) {
            FragmentManager manager = getFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            mapFragment = SupportMapFragment.newInstance();
            transaction.replace(R.id.map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // Construct a FusedLocationProviderClient.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());


        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        queue = MainActivity.getQueueInstance(this.getActivity());

        buttonCheck = this.getActivity().findViewById(R.id.button_check);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleyController.getInstance(getActivity().getApplicationContext()).getRequestQueue().cancelAll("MAP");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        loadLocations();
//        addAllLocations();

        buttonCheck.setVisibility(View.VISIBLE);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
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
                                    locationCheck(location);
                                } else {
                                    break;
                                }
                            }
                            if (!checked) {
                                Toast.makeText(getContext(), "Too far from any location.", Toast.LENGTH_LONG).show();
                            }
                        }

                    } else {
                        Toast.makeText(getContext(), "You need internet to load locations.", Toast.LENGTH_LONG).show();
                    }
                } else {
                }
            }
        });

        getLocationPermission();
        updateLocationUI();
        //getDeviceLocation();

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
//                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = fusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = (Location)task.getResult();
                            /*mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));*/
                        } else {
                            Log.d("LOCATION", "Current location is null. Using defaults.");
                            Log.e("LOCATION", "Exception: %s", task.getException());
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
//                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void addAllLocations() {
        for(LocationPoint location : locations.values()) {
            addLocation(location);
        }

        if(ProfileFragment.isLoggedIn() && ProfileFragment.profileData != null) {
            List<LocationPoint> checkedLocations = ProfileFragment.profileData.getCheckedLocations();
            if(checkedLocations != null) {
                for(LocationPoint location : checkedLocations) {
                    locations.get(location.getTitle()).check();
                }
            }

        }
    }

    private void addLocation(LocationPoint location) {
        if(locations.containsKey(location.getTitle())) {
            locations.get(location.getTitle()).setMarker(mMap.addMarker(location.buildMarkerOptions()));
            locations.get(location.getTitle()).setCircle(mMap.addCircle(location.buildCircleOptions(getResources())));
        }
    }

    private void loadLocations() {

        GsonRequest<LocationPoint[]> request = new GsonRequest<LocationPoint[]>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/location",
                LocationPoint[].class,
                new Response.Listener<LocationPoint[]>() {
                    @Override
                    public void onResponse(LocationPoint[] response) {
//                        Log.v("API", response.toString());
                        locations.clear();

                        //TODO: save data
                        for(LocationPoint location : response) {
                            locations.put(location.getTitle(),location);
                        }
                        locationsCount = locations.size();
                        addAllLocations();

                        Toast.makeText(getContext(),"Locations loaded.",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("API", error.toString());
                        Toast.makeText(getContext(),"Failed to load locations.",Toast.LENGTH_LONG).show();
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        request.setTag("MAP");
        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);

    }



    private void locationCheck(final LocationPoint location) {
        if(((MainActivity)getActivity()).getAccessToken() != null && !((MainActivity)getActivity()).getAccessToken().isEmpty()) {

            StringRequest request = new StringRequest(
                    Request.Method.POST,
                    MainActivity.API_URL + "/api/location",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String responseString) {
                            JSONObject response = null;
                            try {
                                response = new JSONObject(responseString);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
//                            Log.v("API", response.toString());

                            //TODO: Do something
                            Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
//                            Log.v("API", error.toString());
                            if(error instanceof NoConnectionError) {
                                Toast.makeText(getContext(),"No internet connection.",Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("X-Auth-Token", ((MainActivity) getActivity()).getAccessToken());
                    return headers;
                }

                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("location", String.valueOf(location.getId()));
                    return params;
                }
            };
            request.setTag("MAP");
            VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);

        } else {
            Toast.makeText(getActivity(), "Not logged in.", Toast.LENGTH_LONG).show();
        }
    }
}
