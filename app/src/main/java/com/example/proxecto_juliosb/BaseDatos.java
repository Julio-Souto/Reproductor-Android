package com.example.proxecto_juliosb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class BaseDatos extends SQLiteOpenHelper {

    static String path="";
    public SQLiteDatabase sqlLiteDB;

    public BaseDatos(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        path=db.getPath();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public ArrayList<Album> obterAlbums() {
        ArrayList<Album> albums = null;

        Cursor datosConsulta = sqlLiteDB.rawQuery("SELECT ID_ALBUM,Nome_Album,Imaxe FROM ALBUM", null);
        if (datosConsulta.moveToFirst()) {
            albums=new ArrayList<Album>();
            String id;
            String nome;
            String imaxe;
            while (!datosConsulta.isAfterLast()) {
                id= datosConsulta.getString(0);
                nome= datosConsulta.getString(1);
                imaxe = datosConsulta.getString(2);
                albums.add(new Album(id, nome,imaxe));
                datosConsulta.moveToNext();
            }
        }
        datosConsulta.close();
        return albums;
    }

    public ArrayList<Artista> obterArtistas() {
        ArrayList<Artista> artistas = null;

        Cursor datosConsulta = sqlLiteDB.rawQuery("SELECT ID_ARTISTA,Nome_Artista FROM ARTISTA", null);
        if (datosConsulta.moveToFirst()) {
            artistas= new ArrayList<Artista>();
            String id;
            String nome;
            while (!datosConsulta.isAfterLast()) {
                id= datosConsulta.getString(0);
                nome= datosConsulta.getString(1);
                artistas.add(new Artista(id, nome));
                datosConsulta.moveToNext();
            }
        }
        datosConsulta.close();
        return artistas;
    }

    public ArrayList<Cancion> obterCancions() {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsNome() {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION ORDER BY Titulo ASC");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsOrdearData() {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION ORDER BY Data ASC");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsOrdearAlbum() {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION INNER JOIN ALBUM ON ID_ALBUM=IdAlbum ORDER BY Nome_Album ASC ");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsOrdearArtista() {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION INNER JOIN ARTISTA ON ID_ARTISTA=IdArtista ORDER BY Nome_Artista ASC ");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsData(String data) {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION WHERE DATA='"+data+"'");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsArtista(String artista) {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION WHERE IdArtista='"+artista+"'");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsAlbum(String album) {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION WHERE IdAlbum='"+album+"'");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsLista(String lista) {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION  INNER JOIN LISTACANCION ON IdCancion=ID_CANCION WHERE IdLista='"+lista+"'");
        return cancions;
    }

    public ArrayList<Cancion> obterCancionsQuery(String query) {
        ArrayList<Cancion> cancions = null;
        cancions=getSongs("SELECT ID_CANCION,Titulo,Data,Duracion,Ruta,IdArtista,IdAlbum FROM CANCION WHERE Titulo LIKE '%"+query+"%' OR Data LIKE '%"+query+"%' OR" +
                " (SELECT Nome_Artista FROM ARTISTA WHERE ID_ARTISTA=IdArtista) LIKE '%"+query+"%' OR (SELECT Nome_Album FROM ALBUM WHERE ID_ALBUM=IdAlbum) LIKE '%"+query+"%'");
        return cancions;
    }

    public ArrayList<Cancion> getSongs(String sql) {
        ArrayList<Cancion> cancions = null;

        Cursor datosConsulta = sqlLiteDB.rawQuery(sql, null);
        if (datosConsulta.moveToFirst()) {
            cancions = new ArrayList<Cancion>();
            String id;
            String titulo;
            String data;
            int duracion;
            String ruta;
            String idart;
            String idalb;
            while (!datosConsulta.isAfterLast()) {
                id= datosConsulta.getString(0);
                titulo= datosConsulta.getString(1);
                data= datosConsulta.getString(2);
                duracion= datosConsulta.getInt(3);
                ruta= datosConsulta.getString(4);
                idart= datosConsulta.getString(5);
                idalb= datosConsulta.getString(6);
                cancions.add(new Cancion(id, titulo, data, duracion, ruta, idart, idalb));
                datosConsulta.moveToNext();
            }
        }
        datosConsulta.close();
        return cancions;
    }
}
