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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class ProfileFragment extends Fragment {

    private Button buttonLogin, buttonLogout;
    private EditText fieldEmail, fieldPassword;
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


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> params = new HashMap<String, String>();
                params.put("email", fieldEmail.getText().toString());
                params.put("password", fieldPassword.getText().toString());

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        MainActivity.API_URL + "/api/auth",
                        new JSONObject(params),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.v("API", response.toString());

                                //TODO: save data

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
                    public Map<String, String> getHeaders() {
                        HashMap<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
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
}
