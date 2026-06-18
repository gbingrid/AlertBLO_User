package com.example.alertblo;

import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Alerta {

    public enum Tipo{CRITICA, NORMAL};
    private final int id_Alerta;
    private final String descripcion;
    private int silencio = 0;
    private final String fecha_hora;
    private long timestamp = System.currentTimeMillis();

    public Alerta(int id_Alerta, String descripcion, int silencio) {
        this.id_Alerta = id_Alerta;
        this.descripcion = descripcion;
        this.silencio = silencio;
        this.fecha_hora = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
        this.timestamp = System.currentTimeMillis();
    }

    public void setTimestamp(long tiempo){
        this.timestamp = tiempo;
    }

    public long getTimestamp() {

        return timestamp;
    }

    public int getIdAlerta(){

        return id_Alerta;
    }

    public String getDescripcion(){

        return descripcion;
    }

    public int getSilencio(){

        return silencio;
    }

    public String getFechaHora(){

        return fecha_hora;
    }

    public Tipo getTipo(){
        return silencio == 0
                ? Tipo.CRITICA
                : Tipo.NORMAL;
    }

    public String getTitulo(){
        return getTipo() == Tipo.CRITICA
                ? "¡ALERTA CRÍTICA!"
                : "AVISO MUNICIPAL";
    }

    public static Alerta parsearAlerta(String alerta){
        try {
            if (alerta == null || alerta.equalsIgnoreCase("null")
                    || alerta.trim().isEmpty()){
                return null;
            }

            JSONObject json = new JSONObject(alerta);

            int id_alerta = json.optInt("ID_ALERTA", 0);
            String descripcion = json.optString("TEXT_ALERTA", "");
            int silencio = json.optInt("SILENCIO", 1);

            return new Alerta(id_alerta, descripcion, silencio);

        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}


