package com.example.proxecto_juliosb;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomList extends ArrayAdapter<Cancion> {

    private ArrayList<Cancion> list;
    Activity context;

    public CustomList(Activity context, ArrayList<Cancion> lista) {
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

        titleText.setText(list.get(position).getTitulo());
        Cursor cursor = null;
        String sql ="SELECT Imaxe FROM ALBUM WHERE ID_ALBUM=?";
        cursor= Reproductor.bd.sqlLiteDB.rawQuery(sql,new String[]{list.get(position).getIdAlbum()});
        String imaxe="";
        if(cursor.moveToFirst()) {
            imaxe = cursor.getString(0);
        }
        cursor.close();
        Bitmap image= BitmapFactory.decodeFile(imaxe);
        imageView.setImageBitmap(image);
        cursor = null;
        sql = "SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
        cursor = Reproductor.bd.sqlLiteDB.rawQuery(sql, new String[]{list.get(position).getIdArtista()});
        String artista="";
        if (cursor.moveToFirst())
            artista=cursor.getString(0);
        subtitleText.setText(artista);
        cursor.close();
        return rowView;

    };
}