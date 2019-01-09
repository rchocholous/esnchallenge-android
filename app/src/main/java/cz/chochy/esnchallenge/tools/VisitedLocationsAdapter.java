package cz.chochy.esnchallenge.tools;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cz.chochy.esnchallenge.R;
import cz.chochy.esnchallenge.model.LocationPoint;

/**
 * @author chochy
 * Date: 2019-01-09
 */
public class VisitedLocationsAdapter extends ArrayAdapter<LocationPoint> {
    private Context context;
    private ArrayList<LocationPoint> locations;
    private static LayoutInflater inflater = null;

    public VisitedLocationsAdapter(Context context, ArrayList<LocationPoint> locations) {
        super(context, R.layout.layout_score_row, locations);

        this.context = context;
        this.locations = locations;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = null;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.layout_visited_location_row, parent, false);

        // Displaying a textview


        TextView textViewRank = (TextView) rowView.findViewById(R.id.text_location_title);
        textViewRank.setText(locations.get(position).getTitle());


        return rowView;
    }
}
