package org.esncz.esnchallenge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.esncz.esnchallenge.facade.BackendFacade;
import org.esncz.esnchallenge.model.LocationPoint;
import org.esncz.esnchallenge.model.ProfileData;
import org.esncz.esnchallenge.network.VolleyCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public String accessToken;

    private final Fragment scoresFragment = new HighScoresFragment();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment profileFragment = new ProfileFragment();

//    private Fragment activeFragment;
    private ViewPager viewpager;

    // Global data
    private BackendFacade facade;
    private ProfileData profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.facade = new BackendFacade(this.getApplicationContext(), "ACTIVITY");

        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        viewpager = findViewById(R.id.viewpager);
        setupViewPager();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
        MenuItem menuItemToSelect = navigation.getMenu().getItem(1);
        menuItemToSelect.setChecked(true);
        onNavigationItemSelected(menuItemToSelect);

//        activeFragment = mapFragment;
//        if (savedInstanceState != null) {
//            //Restore the fragment's instance
//            activeFragment = getSupportFragmentManager().getFragment(savedInstanceState, "savedMapFragment");
//        }
        loadProfileData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        getSupportFragmentManager().putFragment(outState,"savedMapFragment", activeFragment);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(scoresFragment);
        adapter.addFragment(mapFragment);
        adapter.addFragment(profileFragment);

        viewpager.setAdapter(adapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        hideKeyboard(this);

        switch (menuItem.getItemId()) {
            case R.id.navigation_scores:
                viewpager.setCurrentItem(0);
                break;
            case R.id.navigation_map:
//                ((ProfileFragment)profileFragment).notifyFragmentLoaded();
//                ((MapFragment)mapFragment).notifyFragmentLoaded(this.profile);
                viewpager.setCurrentItem(1);
                break;

            case R.id.navigation_profile:
                viewpager.setCurrentItem(2);
                break;
        }
        return true;
    }



    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void rateApplication(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + this.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

    public String getAccessToken() {
        SharedPreferences preferences = getSharedPreferences("ESNChallengePreferences", MODE_PRIVATE);
        return preferences.getString("ACCESS_TOKEN",null);
        //return accessToken;
    }

    public void setAccessToken(String accessToken) {
        SharedPreferences preferences = getSharedPreferences("ESNChallengePreferences", MODE_PRIVATE);
        preferences.edit().putString("ACCESS_TOKEN", accessToken).commit();
        //this.accessToken = accessToken;
    }

    public ProfileData getProfile() {
        return this.profile;
    }

    public void loadProfileData() {
        SharedPreferences preferences = getSharedPreferences("ESNChallengePreferences", MODE_PRIVATE);
        final String accessToken = preferences.getString("ACCESS_TOKEN",null);
        if(accessToken != null) {
            callProfileEndpoint(accessToken);
        }
    }

    public void cleanProfileData() {
        this.profile = null;
    }

    private void callProfileEndpoint(String accessToken) {
        this.facade.sendGetProfile(accessToken,
                new VolleyCallback<ProfileData>() {
                    @Override
                    public void onSuccess(ProfileData result) {
                        MainActivity.this.profile = result;
                        notifyProfileChanged();
                        Toast.makeText(getApplicationContext(),"Successfully logged in.",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String result) throws Exception {
                        Toast.makeText(getApplicationContext(), "Failed to load profile data.", Toast.LENGTH_LONG).show();
                    }
                }
        );

    }

    private List<ProfileChangedListener> profileChangedListener;

    public void registerProfileChangedListener(ProfileChangedListener listener) {
        if(profileChangedListener == null) {
            profileChangedListener = new LinkedList<>();
        }
        profileChangedListener.add(listener);
        notifyProfileChanged();
    }

    public void unregisterProfileChangedListener(ProfileChangedListener listener) {
        if(profileChangedListener == null) {
            profileChangedListener = new LinkedList<>();
        }
        profileChangedListener.remove(listener);
    }

    public void notifyProfileChanged() {
        if(profileChangedListener == null) {
            profileChangedListener = new LinkedList<>();
        }
        for(ProfileChangedListener listener : profileChangedListener) {
            listener.updateProfile(this.profile);
        }
    }

    public void addCheckedLocation(LocationPoint location) {
        if(this.profile != null) {
            if(this.profile.getCheckedLocations() == null) {
                this.profile.setCheckedLocations(new ArrayList<LocationPoint>());
            }
            this.profile.getCheckedLocations().add(location);
        }
        notifyProfileChanged();
    }


    public boolean isLoggedIn() {
        return this.profile != null;
    }
}
