package org.esncz.esnchallenge;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.esncz.esnchallenge.tools.RequestBuilder;
import org.esncz.esnchallenge.tools.VolleyCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.esncz.esnchallenge.model.HighScore;
import org.esncz.esnchallenge.tools.GsonRequest;
import org.esncz.esnchallenge.tools.HighScoreAdapter;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class HighScoresFragment extends Fragment {

    private List<HighScore> highscores;
    private HighScoreAdapter scoreAdapter;
    private ProgressBar progressBar;
    private LinearLayout layoutHighScores;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scores, null);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        progressBar = this.getActivity().findViewById(R.id.progress_bar);
        layoutHighScores = this.getActivity().findViewById(R.id.layout_highscores);

        callHighScoresEndpoint();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        VolleyController.getInstance(getActivity().getApplicationContext()).getRequestQueue().cancelAll("SCORES");
    }

    private void callHighScoresEndpoint() {
        progressBar.setVisibility(View.VISIBLE);
        layoutHighScores.setVisibility(View.GONE);
        final ListView scoreListView = (ListView) this.getActivity().findViewById(R.id.listview_scores);

        RequestBuilder<HighScore[]> builder = new RequestBuilder<>(
                Request.Method.GET,
                MainActivity.API_URL + "/api/leaderboards?limit=10&offset=0",
                HighScore[].class,
                new VolleyCallback<HighScore[]>() {
                    @Override
                    public void onSuccess(HighScore[] result) throws JSONException {
                        highscores = new ArrayList<>(Arrays.asList(result));
                        scoreListView.setAdapter(new HighScoreAdapter(getContext(), (ArrayList<HighScore>) highscores));

                        layoutHighScores.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Leader board loaded. ",Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String result) throws Exception {
                        layoutHighScores.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Failed to load leadersboard.",Toast.LENGTH_LONG).show();
                    }
                })
                .withHeaders("Content-Type", "application/x-www-form-urlencoded")
                .withHeaders("Pragma", "no-cache")
                .withHeaders("Cache-Control", "no-cache, no store, must-revalidate")
                .withTag("SCORES");

        VolleyController.getInstance(getActivity().getApplicationContext()).addToRequestQueue(builder.build());
    }

}
