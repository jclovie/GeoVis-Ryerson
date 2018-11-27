package com.example.jake.startedarcproject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ItemAdapter extends BaseAdapter {

    LayoutInflater mInflater;
    String[] fields;
    String[] values;

    public ItemAdapter(Context c, String[] f, String[] v) {
        fields = f;
        values = v;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return fields.length;
    }

    @Override
    public Object getItem(int position) {
        return fields[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View x = mInflater.inflate(R.layout.table_layout, null);
        TextView fieldTextView = (TextView) x.findViewById(R.id.fieldTextView);
        TextView valueTextView = (TextView) x.findViewById(R.id.valueTextField);

        String field = fields[position];
        String value = values[position];

        fieldTextView.setText(field);
        valueTextView.setText(value);

        return x;
    }
}
