package com.example.proxecto_juliosb;

public class Album {
    private String IdAlbum;
    private String NomeAlbum;
    private String RutaImaxe;

    public Album(String idAlbum, String nomeAlbum, String rutaImaxe) {
        IdAlbum = idAlbum;
        NomeAlbum = nomeAlbum;
        RutaImaxe = rutaImaxe;
    }

    public String getIdAlbum() {
        return IdAlbum;
    }

    public void setIdAlbum(String idAlbum) {
        IdAlbum = idAlbum;
    }

    public String getNomeAlbum() {
        return NomeAlbum;
    }

    public void setNomeAlbum(String nomeAlbum) {
        NomeAlbum = nomeAlbum;
    }

    public String getRutaImaxe() {
        return RutaImaxe;
    }

    public void setRutaImaxe(String rutaImaxe) {
        RutaImaxe = rutaImaxe;
    }
}
