package com.example.proxecto_juliosb;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    public static BaseDatos bd;
    String imaxe="";
    Cancion cancion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if(bd==null) {
            bd = Reproductor.bd;
            bd.sqlLiteDB = bd.getWritableDatabase();
        }

        if(Reproductor.cancions!=null) {
            cancion= Reproductor.cancions.get(Reproductor.actpos);

            EditText text1 = (EditText) findViewById(R.id.edit_title);
            EditText text2 = (EditText) findViewById(R.id.edit_artist);
            EditText text3 = (EditText) findViewById(R.id.edit_album);
            EditText text4 = (EditText) findViewById(R.id.edit_date);

            Cursor cursor;
            String sql ="SELECT Imaxe,Nome_Album FROM ALBUM WHERE ID_ALBUM=?";
            String album="";
            cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{cancion.getIdAlbum()});
            if(cursor.moveToFirst()) {
                imaxe = cursor.getString(0);
                album = cursor.getString(1);
            }
            cursor.close();
            Bitmap image= BitmapFactory.decodeFile(imaxe);
            sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
            String artista="";
            cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{cancion.getIdArtista()});
            if(cursor.moveToFirst()) {
                artista=cursor.getString(0);
            }
            cursor.close();
            ImageView view=(ImageView) findViewById(R.id.edit_image);
            view.setImageBitmap(image);
            text1.setText(cancion.getTitulo());
            text2.setText(artista);
            text3.setText(album);
            text4.setText(cancion.getData());
        }

    }

    public void onBackClick(View v){
        finish();
    }

    public void onEditClick(View v){
        EditText text1 = (EditText) findViewById(R.id.edit_title);
        EditText text2 = (EditText) findViewById(R.id.edit_artist);
        EditText text3 = (EditText) findViewById(R.id.edit_album);
        EditText text4 = (EditText) findViewById(R.id.edit_date);
        Cursor cursor;
        String sql ="SELECT ID_ALBUM,Imaxe,Nome_Album FROM ALBUM WHERE Nome_Album=?";
        String idalbum=randomID();
        cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{""+text3.getText()});
        if(!cursor.moveToFirst())
            bd.sqlLiteDB.execSQL("INSERT INTO ALBUM (ID_ALBUM,Nome_Album,Imaxe) VALUES ('" + idalbum + "','" + text3.getText() +"','"+ imaxe +"')");
        else
            idalbum=cursor.getString(0);
        cursor.close();
        sql ="SELECT ID_ARTISTA,Nome_Artista FROM ARTISTA WHERE Nome_Artista=?";
        String idartist=randomID();
        cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{""+text2.getText()});
        if(!cursor.moveToFirst())
            bd.sqlLiteDB.execSQL("INSERT INTO ARTISTA (ID_ARTISTA,Nome_Artista) VALUES ('" + idartist + "','" + text2.getText() +"')");
        else
            idartist=cursor.getString(0);
        cursor.close();
        bd.sqlLiteDB.execSQL("UPDATE CANCION SET Titulo='" + text1.getText() + "', Data='" + text4.getText() + "', IdAlbum='" + idalbum + "'," +
                "IdArtista='" + idartist + "' WHERE ID_CANCION='"+cancion.getIdCancion()+"'");

        Reproductor.cancions=bd.obterCancions();
        if(Reproductor.cancions!=null){
            Reproductor.lista= new ArrayList<>();
            Reproductor.rutas= new ArrayList<>();

            for(Cancion c:Reproductor.cancions) {
                sql ="SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=?";
                cursor= bd.sqlLiteDB.rawQuery(sql,new String[]{c.getIdArtista()});
                if(cursor.moveToFirst())
                    Reproductor.lista.add(cursor.getString(0) + " - " + c.getTitulo());
                else
                    Reproductor.lista.add(c.getTitulo());
                cursor.close();
                Reproductor.rutas.add(c.getRuta());
            }
            Reproductor.rutaActual=Reproductor.rutas.get(Reproductor.actpos);
        }
    }

    public String randomID(){
        return ""+ UUID.randomUUID().toString().replace("-", "").substring(0,15);
    }

    @Override
    protected void onResume(){
        super.onResume();

        if(Reproductor.created) {
            if (bd == null) {
                bd = new BaseDatos(getApplicationContext(), "DATOS", null, 1);
                bd.sqlLiteDB = bd.getWritableDatabase();
            }
        }

    }
}