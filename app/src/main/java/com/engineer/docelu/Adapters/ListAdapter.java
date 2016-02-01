package com.engineer.docelu.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.engineer.docelu.R;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<String> {

    EditText editTextFinal;

    public ListAdapter(Context context, ArrayList<String> items, EditText editText) {
        super(context, 0, items);
        editTextFinal = editText;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final String string = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.input_hint_listview, parent, false);
        }

        final TextView input = (TextView) convertView.findViewById(R.id.input);

        input.setText(string);

        input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextFinal.setText(string);
            }
        });

        return convertView;
    }
}
