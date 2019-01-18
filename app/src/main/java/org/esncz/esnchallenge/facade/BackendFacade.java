package org.esncz.esnchallenge.facade;

import android.content.Context;

import com.android.volley.Request;

import org.esncz.esnchallenge.network.VolleyController;
import org.esncz.esnchallenge.model.HighScore;
import org.esncz.esnchallenge.model.LocationPoint;
import org.esncz.esnchallenge.model.ProfileData;
import org.esncz.esnchallenge.network.RequestBuilder;
import org.esncz.esnchallenge.network.VolleyCallback;
import org.json.JSONObject;

/**
 * @author chochy
 * Date: 2019-01-18
 */
public class BackendFacade {

    private Context context;
    private String contextTag;

    public BackendFacade(Context context, String contextTag) {
        this.context = context;
        this.contextTag = contextTag;
    }

    public void cancelRequests() {
        VolleyController.getInstance(this.context).getRequestQueue().cancelAll(this.contextTag);
    }

    public void sendGetHighScores(int limit, int offset, VolleyCallback<HighScore[]> callback) {
        RequestBuilder<HighScore[]> builder = new RequestBuilder<>(
                Request.Method.GET,
                Endpoint.LEADERBOARDS + "?limit=" + limit + "&offset=" + offset,
                HighScore[].class,
                callback)
                .withHeaders("Content-Type", "application/x-www-form-urlencoded")
                .withHeaders("Pragma", "no-cache")
                .withHeaders("Cache-Control", "no-cache, no store, must-revalidate")
                .withTag(this.contextTag);

        VolleyController.getInstance(this.context).addToRequestQueue(builder.build());
    }


    public void sendGetAccessToken(String email, String password, VolleyCallback<JSONObject> callback) {
        RequestBuilder<JSONObject> builder = new RequestBuilder<>(
                Request.Method.POST,
                Endpoint.AUTH,
                JSONObject.class,
                callback)
                .withHeaders("Content-Type", "application/x-www-form-urlencoded")
                .withParams("email", email)
                .withParams("password", password)
                .withPriority(Request.Priority.IMMEDIATE)
                .withTag(this.contextTag);

        VolleyController.getInstance(this.context).addToRequestQueue(builder.build());
    }

    public void sendGetProfile(String accessToken, VolleyCallback<ProfileData> callback) {
        RequestBuilder<ProfileData> builder = new RequestBuilder<>(
                Request.Method.GET,
                Endpoint.PROFILE,
                ProfileData.class,
                callback)
                .withHeaders("X-Auth-Token", accessToken)
                .withHeaders("Pragma", "no-cache")
                .withHeaders("Cache-Control", "no-cache, no store, must-revalidate")
                .withPriority(Request.Priority.IMMEDIATE)
                .withTag(this.contextTag);

        VolleyController.getInstance(this.context).addToRequestQueue(builder.build());
    }

    public void sendGetLocations(VolleyCallback<LocationPoint[]> callback) {
        RequestBuilder<LocationPoint[]> builder = new RequestBuilder<>(
                Request.Method.GET,
                Endpoint.LOCATION,
                LocationPoint[].class,
                callback)
                .withHeaders("Content-Type", "application/x-www-form-urlencoded")
                .withHeaders("Pragma", "no-cache")
                .withHeaders("Cache-Control", "no-cache, no store, must-revalidate")
                .withTag(this.contextTag);

        VolleyController.getInstance(this.context).addToRequestQueue(builder.build());
    }

    public void sendCheckLocation(String accessToken, LocationPoint location, VolleyCallback<JSONObject> callback) {
        RequestBuilder<JSONObject> builder = new RequestBuilder<>(
                Request.Method.POST,
                Endpoint.LOCATION,
                JSONObject.class,
                callback)
                .withHeaders("X-Auth-Token", accessToken)
                .withParams("location", String.valueOf(location.getId()))
                .withTag(this.contextTag);

        VolleyController.getInstance(this.context).addToRequestQueue(builder.build());
    }


    private interface ExternalSystem {
        String FIESTA = "https://fiesta.esncz.org";
        String CHALLENGE = "https://challenge.esncz.org";
    }

    private interface Endpoint {
        String AUTH = ExternalSystem.FIESTA + "/api/auth";
        String PROFILE = ExternalSystem.CHALLENGE + "/api/profile";
        String LOCATION = ExternalSystem.CHALLENGE + "/api/location";
        String LEADERBOARDS = ExternalSystem.CHALLENGE + "/api/leaderboards";
    }
}
