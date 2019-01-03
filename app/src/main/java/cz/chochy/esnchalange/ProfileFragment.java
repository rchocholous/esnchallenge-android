package cz.chochy.esnchalange;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class ProfileFragment extends Fragment {

    public Button buttonLogin, buttonLogout;
    public LinearLayout layoutLogin, layoutProfile;

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

        layoutLogin = this.getActivity().findViewById(R.id.linear_layout_login);
        layoutProfile = this.getActivity().findViewById(R.id.linear_layout_profile);

        buttonLogin = this.getActivity().findViewById(R.id.button_login);
        buttonLogout = this.getActivity().findViewById(R.id.button_logout);


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: login - call API
                layoutLogin.setVisibility(View.GONE);
                layoutProfile.setVisibility(View.VISIBLE);
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
