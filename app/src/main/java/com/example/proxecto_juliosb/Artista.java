package com.example.proxecto_juliosb;

public class Artista {
    private String IdArtista;
    private String NomeArtista;

    public Artista(String idArtista, String nomeArtista) {
        IdArtista = idArtista;
        NomeArtista = nomeArtista;
    }

    public String getIdArtista() {
        return IdArtista;
    }

    public void setIdArtista(String idArtista) {
        IdArtista = idArtista;
    }

    public String getNomeArtista() {
        return NomeArtista;
    }

    public void setNomeArtista(String nomeArtista) {
        NomeArtista = nomeArtista;
    }

    @Override
    public String toString() {
        return "Artista{" +
                "IdArtista='" + IdArtista + '\'' +
                ", NomeArtista='" + NomeArtista + '\'' +
                '}';
    }
}
