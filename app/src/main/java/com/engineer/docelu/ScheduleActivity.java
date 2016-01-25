package com.engineer.docelu;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Kostek on 2016-01-25.
 */
public class ScheduleActivity extends AppCompatActivity {

    private ArrayList<Departure> arrayOfDepartures = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        arrayOfDepartures = (ArrayList<Departure>) getIntent().getSerializableExtra("departureArray");
        ScheduleAdapter adapter = new ScheduleAdapter(ScheduleActivity.this, arrayOfDepartures);
        ListView departuresView = (ListView) findViewById(R.id.schedule);
        departuresView.setAdapter(adapter);

    }

    public class ScheduleAdapter extends ArrayAdapter<Departure> {

        public ScheduleAdapter(Context context, ArrayList<Departure> items) {
            super(context, 0, items);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Departure departure = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_main_listview, parent, false);
            }

            final TextView lineMenu = (TextView) convertView.findViewById(R.id.line_menu);
            final TextView line = (TextView) convertView.findViewById(R.id.line);
            final TextView minutesMenu = (TextView) convertView.findViewById(R.id.minutes_menu);
            final TextView minutes = (TextView) convertView.findViewById(R.id.minutes);

            line.setText(departure.getLine()+"");
            if (departure.getRealTime()){
                minutes.setTextColor(ColorStateList.valueOf(Color.parseColor("#177F42")));
                minutes.setText(departure.getMinutes()+"");
            } else {
                minutes.setText(departure.getDeparture().substring(11, 16)+"");
            }
            return convertView;
        }

        public Departure getItem(int position) {
            return arrayOfDepartures.get(position);
        }

        public final int getCount() {
            return arrayOfDepartures.size();
        }

        public final long getItemId(int position) {
            return position;
        }
    }
}
