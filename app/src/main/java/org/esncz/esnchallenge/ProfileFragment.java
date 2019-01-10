package org.esncz.esnchallenge;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.esncz.esnchallenge.model.LocationPoint;
import org.esncz.esnchallenge.model.ProfileData;
import org.esncz.esnchallenge.model.University;
import org.esncz.esnchallenge.tools.GsonRequest;
import org.esncz.esnchallenge.tools.VisitedLocationsAdapter;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class ProfileFragment extends Fragment {

    private Button buttonLogin, buttonLogout;
    private ImageButton buttonSettingsOpen, buttonSettingsClose;
    private EditText fieldEmail, fieldPassword;
    private TextView textName, textEmail, textFirstName, textLastName,  textGender, textUniversity, textSection, textLocationCount;
    private LinearLayout layoutProfile, layoutSettings;
    private ConstraintLayout layoutLogin;
    private ProgressBar progressBar;


    private ListView locationsListView;
//    private RequestQueue queue;

    public static ProfileData profileData;//TODO: static = ugly solution. Investigate on how to use "Bundle"

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_profile, null);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ((MainActivity)this.getActivity()).getSupportActionBar().setTitle("Profile");
//        ((MainActivity)this.getActivity()).getSupportActionBar().show();
    }

    @Deprecated
    private void drawLogo() {
        ImageView imageLogo = (ImageView) this.getActivity().findViewById(R.id.imageLogo);
        imageLogo.setImageResource(0);
        Drawable draw = getResources().getDrawable(R.drawable.logo);
        imageLogo.setImageDrawable(draw);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        drawLogo();//TODO: Why I need to do this??

//        queue = MainActivity.getQueueInstance(this.getActivity());

        progressBar = this.getActivity().findViewById(R.id.progress_bar);

        layoutLogin = this.getActivity().findViewById(R.id.layout_login);
        layoutProfile = this.getActivity().findViewById(R.id.linear_layout_profile);
        layoutSettings = this.getActivity().findViewById(R.id.layour_settings);

        buttonLogin = this.getActivity().findViewById(R.id.button_login);
        buttonLogout = this.getActivity().findViewById(R.id.button_logout);
        buttonSettingsOpen = this.getActivity().findViewById(R.id.button_settings_open);
        buttonSettingsClose = this.getActivity().findViewById(R.id.button_settings_close);

        fieldEmail = this.getActivity().findViewById(R.id.edit_text_email);
        fieldPassword = this.getActivity().findViewById(R.id.edit_text_password);

        textName = this.getActivity().findViewById(R.id.text_name);
        textEmail = this.getActivity().findViewById(R.id.text_email);
        textFirstName = this.getActivity().findViewById(R.id.text_firstname);
        textLastName = this.getActivity().findViewById(R.id.text_lastname);
        textGender = this.getActivity().findViewById(R.id.text_gender);
        textUniversity = this.getActivity().findViewById(R.id.text_university);
        textSection = this.getActivity().findViewById(R.id.text_section);

        textLocationCount = this.getActivity().findViewById(R.id.text_checked_count);

        locationsListView = (ListView) this.getActivity().findViewById(R.id.listview_visited_locations);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showLayout(LayoutEnum.NONE);
                progressBar.setVisibility(View.VISIBLE);
                View view = getView();
                if(view != null) {//TODO: !
                    MainActivity.hideKeyboardFrom(getActivity(),view);
                } else {
                    MainActivity.hideKeyboard(getActivity());
                }

                loadAccessToken();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment.profileData = null;
                ((MainActivity)getActivity()).setAccessToken(null);
//                showLoginView();
                showLayout(LayoutEnum.LOGIN);
            }
        });

        buttonSettingsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayout(LayoutEnum.SETTINGS);
            }
        });

        buttonSettingsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayout(LayoutEnum.PROFILE);
            }
        });

        showLayout(LayoutEnum.LOGIN);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isLoggedIn()) {
//            showProfileView();
            showLayout(LayoutEnum.PROFILE);
            populateProfileData(ProfileFragment.profileData);
        } else {
//            showLoginView();
            showLayout(LayoutEnum.LOGIN);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleyController.getInstance(getActivity().getApplicationContext()).getRequestQueue().cancelAll("PROFILE");
    }

    private void showLayout(LayoutEnum layout) {
        if(layout == null) {
            layout = LayoutEnum.NONE;
        }
        progressBar.setVisibility(View.GONE);
        switch (layout) {
            case PROFILE:
                layoutLogin.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.VISIBLE);
                layoutSettings.setVisibility(View.GONE);
                break;
            case SETTINGS:
                layoutLogin.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.GONE);
                layoutSettings.setVisibility(View.VISIBLE);
                break;
            case LOGIN:
                layoutLogin.setVisibility(View.VISIBLE);
                layoutProfile.setVisibility(View.GONE);
                layoutSettings.setVisibility(View.GONE);
                break;
            default:
                layoutLogin.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.GONE);
                layoutSettings.setVisibility(View.GONE);
                break;
        }
    }

    private void showProfileView() {
        layoutLogin.setVisibility(View.GONE);
        layoutProfile.setVisibility(View.VISIBLE);
    }

    private void showLoginView() {
        layoutLogin.setVisibility(View.VISIBLE);
        layoutProfile.setVisibility(View.GONE);
    }

    private void loadAccessToken() {

        StringRequest request = new StringRequest(
                Request.Method.POST,
                MainActivity.API_AUTH_URL + "/api/auth",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String responseString) {
                        JSONObject response;
                        try {
                            response = new JSONObject(responseString);
                            ((MainActivity)getActivity()).setAccessToken(response.getString("access_token"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            showLayout(LayoutEnum.LOGIN);
                            Toast.makeText(getContext(),"Login failed.",Toast.LENGTH_LONG).show();
                        }
//                        Log.v("API", response.toString());

                        loadProfileData();

//                        showProfileView();
//                        Toast.makeText(getContext(),"Successfully logged in.",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.v("API", error.toString());
                        showLayout(LayoutEnum.LOGIN);
                        if(error instanceof NoConnectionError) {
                            Toast.makeText(getContext(),"No internet connection.",Toast.LENGTH_LONG).show();
                        } else if(error instanceof AuthFailureError) {
                            Toast.makeText(getContext(),"Incorrect credentials.",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", fieldEmail.getText().toString());
                params.put("password", fieldPassword.getText().toString());
                return params;
            }
            @Override
            public Priority getPriority() {
                return Priority.IMMEDIATE;
            }
        };
        request.setTag("PROFILE");
        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);

    }


//    private void makeRequest(String url, VolleyCallback callback) {
//
//    }

    private void loadProfileData() {

        GsonRequest<ProfileData> request = new GsonRequest<ProfileData>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/profile",
                ProfileData.class,
                new Response.Listener<ProfileData>() {
                    @Override
                    public void onResponse(ProfileData response) {
//                        Log.v("API", response.toString());

                        ProfileFragment.profileData = response;
                        ProfileFragment.this.populateProfileData(ProfileFragment.profileData);


                        showLayout(LayoutEnum.PROFILE);
//                        Toast.makeText(getContext(), "Profile data loaded.", Toast.LENGTH_LONG).show();
                        Toast.makeText(getContext(),"Successfully logged in.",Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.v("API", error.toString());
                        showLayout(LayoutEnum.LOGIN);
                        Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", ((MainActivity) getActivity()).getAccessToken());
                return headers;
            }
        };
        request.setPriority(Request.Priority.IMMEDIATE);
        request.setTag("PROFILE");
        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(request);

    }

    private void populateProfileData(ProfileData profile) {
        if(profile != null) {
            textName.setText(String.format("Howdy %s!", profile.getFirstname()));
            textEmail.setText(profile.getEmail());
            textFirstName.setText(profile.getFirstname());
            textLastName.setText(profile.getLastname());
            textGender.setText(profile.getGender());
            if(profile.getUniversity() != null) {
                textUniversity.setText(profile.getUniversity().getName());
                textSection.setText(profile.getUniversity().getSectionShort());
            }
            if(profile.getCheckedLocations() != null) {
                textLocationCount.setText(String.format("Check location: %d/%d", profile.getCheckedLocations().size(), MapFragment.locationsCount));
                locationsListView.setAdapter(new VisitedLocationsAdapter(getContext(), (ArrayList<LocationPoint>) profile.getCheckedLocations()));
            }

        }
    }

    public static boolean isLoggedIn() {
        return ProfileFragment.profileData != null;
    }

    private enum LayoutEnum {
        NONE,
        LOGIN,
        PROFILE,
        SETTINGS;
    }
}
