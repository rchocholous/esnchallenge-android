package org.esncz.esnchallenge;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
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

import org.esncz.esnchallenge.facade.BackendFacade;
import org.esncz.esnchallenge.network.VolleyCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import org.esncz.esnchallenge.model.LocationPoint;
import org.esncz.esnchallenge.model.ProfileData;
import org.esncz.esnchallenge.tools.VisitedLocationsAdapter;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class ProfileFragment extends Fragment implements ProfileChangedListener {

    private BackendFacade facade;
    public static ProfileData profileData;//TODO: static = ugly solution. Use "Bundle" instead

    private EditText fieldEmail, fieldPassword;
    private TextView textName, textEmail, textFirstName, textLastName,  textGender, textUniversity, textSection, textLocationCount;
    private ListView locationsListView;

    private LinearLayout layoutProfile, layoutSettings, layoutLogin;
    private ProgressBar progressBar;


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

        this.facade = new BackendFacade(getActivity().getApplicationContext(), "PROFILE");
        ((MainActivity)getActivity()).registerProfileChangedListener(this);

        drawLogo();//TODO: Why I need to do this??

        TextView link = (TextView) this.getActivity().findViewById(R.id.text_link_create);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        link = (TextView) this.getActivity().findViewById(R.id.text_link_reset);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link = (TextView) this.getActivity().findViewById(R.id.text_link_ig);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link = (TextView) this.getActivity().findViewById(R.id.text_link_www);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link = (TextView) this.getActivity().findViewById(R.id.text_link_terms);
        link.setMovementMethod(LinkMovementMethod.getInstance());
        link = (TextView) this.getActivity().findViewById(R.id.text_link_contact);
        link.setMovementMethod(LinkMovementMethod.getInstance());

        progressBar = this.getActivity().findViewById(R.id.progress_bar_profile);

        layoutLogin = this.getActivity().findViewById(R.id.layout_login);
        layoutProfile = this.getActivity().findViewById(R.id.linear_layout_profile);
        layoutSettings = this.getActivity().findViewById(R.id.layour_settings);


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


        Button buttonLogin = this.getActivity().findViewById(R.id.button_login);
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

        Button buttonLogout = this.getActivity().findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).setAccessToken(null);
                ((MainActivity)getActivity()).cleanProfileData();
                showLayout(LayoutEnum.LOGIN);
            }
        });

        ImageButton buttonSettingsOpen = this.getActivity().findViewById(R.id.button_settings_open);
        buttonSettingsOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayout(LayoutEnum.SETTINGS);
            }
        });

        ImageButton buttonSettingsOpen2 = this.getActivity().findViewById(R.id.button_settings_open2);
        buttonSettingsOpen2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLayout(LayoutEnum.SETTINGS);
            }
        });

        ImageButton buttonSettingsClose = this.getActivity().findViewById(R.id.button_settings_close);
        buttonSettingsClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((MainActivity)getActivity()).isLoggedIn()) {
                    showLayout(LayoutEnum.PROFILE);
                } else {
                    showLayout(LayoutEnum.LOGIN);
                }
            }
        });

        if(((MainActivity)getActivity()).isLoggedIn()) {
            ProfileData profile = ((MainActivity)getActivity()).getProfile();
            populateProfileData(profile);
            showLayout(LayoutEnum.PROFILE);
        } else {
            showLayout(LayoutEnum.LOGIN);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if(((MainActivity)getActivity()).isLoggedIn()) {
            ProfileData profile = ((MainActivity)getActivity()).getProfile();
            populateProfileData(profile);
            showLayout(LayoutEnum.PROFILE);
        } else {
            showLayout(LayoutEnum.LOGIN);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity)getActivity()).unregisterProfileChangedListener(this);
        this.facade.cancelRequests();
    }

    private void callAccessTokenEndpoint() {
        final String email = fieldEmail.getText().toString();
        final String password = fieldPassword.getText().toString();
        if(!email.isEmpty() && !password.isEmpty()) {
            this.facade.sendGetAccessToken(email, password,
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

                            ((MainActivity)getActivity()).loadProfileData();
                        }

                        @Override
                        public void onError(String result) throws Exception {
                            showLayout(LayoutEnum.LOGIN);
                            Toast.makeText(getContext(), "Error during login: " + result, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            showLayout(LayoutEnum.LOGIN);
            Toast.makeText(getContext(), "You need to fill the email and password.", Toast.LENGTH_LONG).show();
        }

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
                MainActivity.setListViewHeightBasedOnChildren(locationsListView);
            }

        }
    }

    private void showLayout(LayoutEnum layout) {
        MainActivity.hideKeyboard(getActivity());
        if(layout == null) {
            layout = LayoutEnum.NONE;
        }
        if(progressBar == null) {//bad solution
            return;
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

    @Override
    public void updateProfile(ProfileData data) {
        if(data != null) {
            populateProfileData(data);
            showLayout(LayoutEnum.PROFILE);
        }
    }

    private enum LayoutEnum {
        NONE,
        LOGIN,
        PROFILE,
        SETTINGS;
    }

}
