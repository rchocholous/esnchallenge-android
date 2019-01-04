package cz.chochy.esnchallenge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.chochy.esnchallenge.model.ProfileData;
import cz.chochy.esnchallenge.model.University;
import cz.chochy.esnchallenge.tools.GsonRequest;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class ProfileFragment extends Fragment {

    private Button buttonLogin, buttonLogout;
    private EditText fieldEmail, fieldPassword;
    private TextView textName, textEmail, textFirstName, textLastName,  textGender, textUniversity, textSection;
    private LinearLayout layoutLogin, layoutProfile;
    private RequestQueue queue;

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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        queue = Volley.newRequestQueue(this.getActivity());

        layoutLogin = this.getActivity().findViewById(R.id.linear_layout_login);
        layoutProfile = this.getActivity().findViewById(R.id.linear_layout_profile);

        buttonLogin = this.getActivity().findViewById(R.id.button_login);
        buttonLogout = this.getActivity().findViewById(R.id.button_logout);

        fieldEmail = this.getActivity().findViewById(R.id.edit_text_email);
        fieldPassword = this.getActivity().findViewById(R.id.edit_text_password);

        textName = this.getActivity().findViewById(R.id.text_name);
        textEmail = this.getActivity().findViewById(R.id.text_email);
        textFirstName = this.getActivity().findViewById(R.id.text_firstname);
        textLastName = this.getActivity().findViewById(R.id.text_lastname);
        textGender = this.getActivity().findViewById(R.id.text_gender);
        textUniversity = this.getActivity().findViewById(R.id.text_university);
        textSection = this.getActivity().findViewById(R.id.text_section);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                StringRequest request = new StringRequest(
                        Request.Method.POST,
                        MainActivity.API_URL + "/api/auth",
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String responseString) {
                                JSONObject response = null;
                                try {
                                    response = new JSONObject(responseString);
                                    ((MainActivity)getActivity()).setAccessToken(response.getString("access_token"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Log.v("API", response.toString());

                                //TODO: save data

                                loadProfileData();
                                layoutLogin.setVisibility(View.GONE);
                                layoutProfile.setVisibility(View.VISIBLE);
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
                };
                queue.add(request);


            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: logout - clear data
                layoutLogin.setVisibility(View.VISIBLE);
                layoutProfile.setVisibility(View.GONE);
            }
        });
    }

    private void loadProfileData() {

        GsonRequest<ProfileData> request = new GsonRequest<ProfileData>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/profile",
                ProfileData.class,
                new Response.Listener<ProfileData>() {
                    @Override
                    public void onResponse(ProfileData response) {
                        Log.v("API", response.toString());

                        populateProfileData(response);

                        Toast.makeText(getActivity(), response.toString(), Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.v("API", error.toString());
                        Toast.makeText(getActivity(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("X-Auth-Token", ((MainActivity) getActivity()).getAccessToken());
                return headers;
            }
        };
        queue.add(request);

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
        }
    }
}
