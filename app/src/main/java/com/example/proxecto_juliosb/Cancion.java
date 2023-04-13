package com.example.proxecto_juliosb;

public class Cancion {
    private String IdCancion;
    private String Titulo;
    private String data;
    private int duracion;
    private String ruta;
    private String IdArtista;
    private String IdAlbum;

    public Cancion(String idCancion, String titulo, String data, int duracion, String ruta, String idArtista, String idAlbum) {
        IdCancion = idCancion;
        Titulo = titulo;
        this.data = data;
        this.duracion = duracion;
        this.ruta = ruta;
        IdAlbum = idAlbum;
        IdArtista = idArtista;
    }

    public String getIdCancion() {
        return IdCancion;
    }

    public void setIdCancion(String idCancion) {
        IdCancion = idCancion;
    }

    public String getTitulo() {
        return Titulo;
    }

    public void setTitulo(String titulo) {
        Titulo = titulo;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public String getIdAlbum() {
        return IdAlbum;
    }

    public void setIdAlbum(String idAlbum) {
        IdAlbum = idAlbum;
    }

    public String getIdArtista() {
        return IdArtista;
    }

    public void setIdArtista(String idArtista) {
        IdArtista = idArtista;
    }
}
