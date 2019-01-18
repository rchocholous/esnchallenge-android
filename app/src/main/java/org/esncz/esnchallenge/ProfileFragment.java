package org.esncz.esnchallenge;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
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

import org.esncz.esnchallenge.tools.RequestBuilder;
import org.esncz.esnchallenge.tools.VolleyCallback;
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

        TextView link = (TextView) this.getActivity().findViewById(R.id.text_link_create);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        link = (TextView) this.getActivity().findViewById(R.id.text_link_reset);
        link.setMovementMethod(LinkMovementMethod.getInstance());

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

                callAccessTokenEndpoint();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileFragment.profileData = null;
                ((MainActivity)getActivity()).setAccessToken(null);
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
            showLayout(LayoutEnum.PROFILE);
            populateProfileData(ProfileFragment.profileData);
        } else {
            showLayout(LayoutEnum.LOGIN);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleyController.getInstance(getActivity().getApplicationContext()).getRequestQueue().cancelAll("PROFILE");
    }

    private void callAccessTokenEndpoint() {
        RequestBuilder<JSONObject> builder = new RequestBuilder<>(
                Request.Method.POST,
                MainActivity.API_AUTH_URL + "/api/auth",
                JSONObject.class,
                new VolleyCallback<JSONObject>() {
                    @Override
                    public void onSuccess(JSONObject result) throws JSONException {
                        if(!result.isNull("access_token")) {
                            ((MainActivity)getActivity()).setAccessToken(result.getString("access_token"));
                        } else {
                            try {
                                this.onError("Error: access_token is null!");
                            } catch (Exception ignored) { }
                        }

                        callProfileEndpoint();
                    }

                    @Override
                    public void onError(String result) throws Exception {
                        showLayout(LayoutEnum.LOGIN);
                        Toast.makeText(getContext(), "Error during login." + result, Toast.LENGTH_LONG).show();
                    }
                })
                .withHeaders("Content-Type", "application/x-www-form-urlencoded")
                .withParams("email", fieldEmail.getText().toString())
                .withParams("password", fieldPassword.getText().toString())
                .withPriority(Request.Priority.IMMEDIATE)
                .withTag("PROFILE");

        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(builder.build());
    }

    private void callProfileEndpoint() {
        RequestBuilder<ProfileData> builder = new RequestBuilder<>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/profile",
                ProfileData.class,
                new VolleyCallback<ProfileData>() {
            @Override
            public void onSuccess(ProfileData result) {
                ProfileFragment.profileData = result;
                ProfileFragment.this.populateProfileData(ProfileFragment.profileData);
                showLayout(LayoutEnum.PROFILE);
                Toast.makeText(getContext(),"Successfully logged in.",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(String result) throws Exception {
                showLayout(LayoutEnum.LOGIN);
                Toast.makeText(getContext(), "Failed to load profile data.", Toast.LENGTH_LONG).show();
            }
        })
                .withHeaders("X-Auth-Token", ((MainActivity) getActivity()).getAccessToken())
                .withHeaders("Pragma", "no-cache")
                .withHeaders("Cache-Control", "no-cache, no store, must-revalidate")
                .withPriority(Request.Priority.IMMEDIATE)
                .withTag("PROFILE");

        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(builder.build());
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

    private enum LayoutEnum {
        NONE,
        LOGIN,
        PROFILE,
        SETTINGS;
    }

}
