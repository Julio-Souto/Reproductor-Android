package com.example.proxecto_juliosb;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListGroup extends ArrayAdapter<Group> {

    private final ArrayList<Group> list;
    Activity context;

    public CustomListGroup(Activity context, ArrayList<Group> lista) {
        super(context, R.layout.custom_list, lista);
        // TODO Auto-generated constructor stub
        list=lista;
        this.context=context;

    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.custom_list, null,true);

        TextView titleText = (TextView) rowView.findViewById(R.id.title);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView subtitleText = (TextView) rowView.findViewById(R.id.subtitle);

        titleText.setText(list.get(position).getTitle());
        imageView.setImageBitmap(list.get(position).getImage());
        subtitleText.setText(list.get(position).getSubtitle());

        return rowView;

    }
}