package cz.chochy.esnchallenge.tools;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.chochy.esnchallenge.R;
import cz.chochy.esnchallenge.model.HighScore;

/**
 * @author chochy
 * Date: 2019-01-08
 */
public class HighScoreAdapter extends ArrayAdapter<HighScore> {
    private Context context;
    private ArrayList<HighScore> scores;
    private static LayoutInflater inflater = null;

    public HighScoreAdapter(Context context, ArrayList<HighScore> highScores) {
        super(context, R.layout.layout_score_row, highScores);

        this.context = context;
        this.scores = highScores;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.layout_score_row, parent, false);

        // Displaying a textview


        TextView textViewRank = (TextView) rowView.findViewById(R.id.text_rank);
        textViewRank.setText(scores.get(position).getRank().toString());

        TextView textViewUsername = (TextView) rowView.findViewById(R.id.text_username);
        textViewUsername.setText(scores.get(position).getName());


        TextView textViewSection = (TextView) rowView.findViewById(R.id.text_section);
        textViewSection.setText(scores.get(position).getSection().toString());


        TextView textViewPoints = (TextView) rowView.findViewById(R.id.text_points);
        textViewPoints.setText(scores.get(position).getPoint().toString());


        return rowView;
    }
}
