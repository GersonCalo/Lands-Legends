package com.t4.jakin_mina.model;

import java.util.List;

public class Pregunta {
    private String correcta;
    private String en;
    private String es;
    private String eu;
    private int id;
    private Opciones opciones;
    // Constructor vacío obligatorio para Firebase
    public Pregunta() {}

    // Clase interna para las opciones
    public static class Opciones {
        private List<String> en;
        private List<String> es;
        private List<String> eu;

        public Opciones() {}

        // Getters
        public List<String> getEn() { return en; }
        public List<String> getEs() { return es; }
        public List<String> getEu() { return eu; }
    }

    public void setEn(String en) {
        this.en = en;
    }

    public void setCorrecta(String correcta) {
        this.correcta = correcta;
    }

    public void setEs(String es) {
        this.es = es;
    }

    public void setEu(String eu) {
        this.eu = eu;
    }

    public void setOpciones(Opciones opciones) {
        this.opciones = opciones;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Getters
    public String getCorrecta() { return correcta; }
    public String getEn() { return en; }
    public String getEs() { return es; }
    public String getEu() { return eu; }
    public int getId() { return id; }
    public Opciones getOpciones() { return opciones; }

    // Métodos útiles para obtener datos por idioma
    public String getPregunta(String idioma) {
        switch (idioma) {
            case "es": return es;
            case "eu": return eu;
            default: return en;
        }
    }

    public List<String> getOpcionesPorIdioma(String idioma) {
        switch (idioma) {
            case "es": return opciones.getEs();
            case "eu": return opciones.getEu();
            default: return opciones.getEn();
        }
    }

    @Override
    public String toString() {
        return "Pregunta{" +
                "correcta='" + correcta + '\'' +
                ", en='" + en + '\'' +
                ", es='" + es + '\'' +
                ", eu='" + eu + '\'' +
                ", id=" + id +
                ", opciones=" + opciones +
                '}';
    }
}
