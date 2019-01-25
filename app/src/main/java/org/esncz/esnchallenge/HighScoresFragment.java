package org.esncz.esnchallenge;

import android.os.Bundle;
import android.os.Parcelable;
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

import org.esncz.esnchallenge.facade.BackendFacade;
import org.esncz.esnchallenge.network.VolleyCallback;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.esncz.esnchallenge.model.HighScore;
import org.esncz.esnchallenge.tools.HighScoreAdapter;

/**
 * @author chochy
 * Date: 2019-01-02
 */
public class HighScoresFragment extends Fragment {

    private static String KEY_SCORE_LIST = "SCORE_LIST";

    private BackendFacade facade;

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

        this.facade = new BackendFacade(getActivity().getApplicationContext(), "SCORES");

        progressBar = this.getActivity().findViewById(R.id.progress_bar_score);
        layoutHighScores = this.getActivity().findViewById(R.id.layout_highscores);

        if(savedInstanceState != null && savedInstanceState.containsKey(KEY_SCORE_LIST)) {
//            Toast.makeText(getContext(), "Leader board loaded from memory. ", Toast.LENGTH_LONG).show();
            highscores = savedInstanceState.getParcelableArrayList(KEY_SCORE_LIST);

            final ListView scoreListView = (ListView) this.getActivity().findViewById(R.id.listview_scores);
            scoreAdapter = new HighScoreAdapter(getContext(), (ArrayList<HighScore>) highscores);
            scoreListView.setAdapter(scoreAdapter);
            callHighScoresEndpoint(true);
            scoreAdapter.notifyDataSetChanged();
        } else {
//            Toast.makeText(getContext(), "Leader board loading... ", Toast.LENGTH_LONG).show();
            callHighScoresEndpoint(false);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if(highscores != null) {
            outState.putParcelableArrayList(KEY_SCORE_LIST, (ArrayList<HighScore>) highscores);
//        Toast.makeText(getContext(), "Leader board saved to memory. ", Toast.LENGTH_LONG).show();
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        this.facade.cancelRequests();
    }

    private void callHighScoresEndpoint(final boolean silentUpdate) {
        if(!silentUpdate) {
            progressBar.setVisibility(View.VISIBLE);
            layoutHighScores.setVisibility(View.GONE);
        }
        final ListView scoreListView = (ListView) this.getActivity().findViewById(R.id.listview_scores);

        this.facade.sendGetHighScores(10,0,
                new VolleyCallback<HighScore[]>() {
                    @Override
                    public void onSuccess(HighScore[] result) throws JSONException {
                        if(highscores == null) {
                            highscores = new ArrayList<>(Arrays.asList(result));
                        } else {
                            highscores.clear();
                            highscores.addAll(Arrays.asList(result));
                        }
                        if(scoreAdapter == null) {
                            scoreAdapter = new HighScoreAdapter(getContext(), (ArrayList<HighScore>) highscores);
                            scoreListView.setAdapter(scoreAdapter);
                        } else {
                            scoreAdapter.notifyDataSetChanged();
//                            Toast.makeText(getContext(), "Silently reloaded.", Toast.LENGTH_LONG).show();
                        }

                        layoutHighScores.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
//                        if(!silentUpdate) {
//                            Toast.makeText(getContext(), "Leader board loaded. ", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(getContext(), "Silent - Leader board loaded. ", Toast.LENGTH_LONG).show();
//                        }
                    }

                    @Override
                    public void onError(String result) throws Exception {
                        layoutHighScores.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(),"Failed to load leader board.",Toast.LENGTH_LONG).show();
                    }
        });

    }

}
