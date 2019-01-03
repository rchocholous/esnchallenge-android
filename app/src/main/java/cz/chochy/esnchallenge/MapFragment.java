package cz.chochy.esnchallenge;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

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

    private RequestQueue queue;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    private Button buttonCheck;

    private Map<String,LocationPoint> locations;

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

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        queue = Volley.newRequestQueue(this.getActivity());

        buttonCheck = this.getActivity().findViewById(R.id.button_check);
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
                //Find nearest location (50.3722211,17.1849737)
                final Location actual = new Location("Actual");
                actual.setLatitude(50.3722211);
                actual.setLongitude(17.1849737);

                List<LocationPoint> list = new ArrayList<>(locations.values());
                Collections.sort(list, new Comparator<LocationPoint>() {
                    @Override
                    public int compare(LocationPoint o1, LocationPoint o2) {
                        return (int)(o1.distanceTo(actual)-(o2.distanceTo(actual)));
                    }
                });

                locations.get(list.get(0).getTitle()).getCircle().setFillColor(0x7FA0A500);// Only if is inside range

                for(LocationPoint l : list) {
                    Log.v("SORTED",l.getTitle() + ", distance: " + l.distanceTo(actual));
                }
            }
        });

        LatLng center = new LatLng(49.2136909999999971887518768198788166046142578125,  16.574813999999999936107997200451791286468505859375);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 17));
    }

    private void addAllLocations() {
        for(LocationPoint location : locations.values()) {
            addLocation(location);
        }
    }

    private void addLocation(LocationPoint location) {
        if(locations.containsKey(location.getTitle())) {
            locations.get(location.getTitle()).setMarker(mMap.addMarker(location.buildMarkerOptions()));
            locations.get(location.getTitle()).setCircle(mMap.addCircle(location.buildCircleOptions()));
        }
    }

    private void loadLocations() {
        /*
        LocationPoint zabovresky = new LocationPoint(1l,"Zabovresky", "District", 49.2136909999999971887518768198788166046142578125,  16.574813999999999936107997200451791286468505859375);
        locations.put(zabovresky.getTitle(),zabovresky);

        LocationPoint vidnava = new LocationPoint(2l,"Vidnava", "Town", 50.3711727,17.1877859);
        locations.put(vidnava.getTitle(),vidnava);

        LocationPoint poruba = new LocationPoint(3l,"Poruba", "District", 49.8310344,18.1642107);
        locations.put(poruba.getTitle(),poruba);
        */

        GsonRequest<LocationPoint[]> request = new GsonRequest<LocationPoint[]>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/location",
                LocationPoint[].class,
                new Response.Listener<LocationPoint[]>() {
                    @Override
                    public void onResponse(LocationPoint[] response) {
                        Log.v("API", response.toString());

                        //TODO: save data
                        for(LocationPoint location : response) {
                            locations.put(location.getTitle(),location);
                        }
                        addAllLocations();

                        Toast.makeText(getActivity(),response.toString(),Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("API", error.toString());
                        Toast.makeText(getActivity(),error.toString(),Toast.LENGTH_LONG).show();
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
        queue.add(request);

    }
}
