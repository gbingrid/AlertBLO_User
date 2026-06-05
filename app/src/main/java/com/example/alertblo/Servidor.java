package com.example.alertblo;


import java.io.*;
import java.net.*;

// Classe estàtica amb les dues funcions de comunicació amb el servidor.
// Sempre s'han de cridar des d'un fil secundari (no des del fil de la UI).
public class Servidor {

    // Consulta si hi ha una alerta nova per a aquest dispositiu.
    // Retorna el text de l'alerta, o null si no n'hi ha.
    public static String getAlerta(String idDispositiu) {
        try {
            String enc = URLEncoder.encode(idDispositiu, "UTF-8");
            HttpURLConnection conn = obrir(MainActivity.IP_SERVIDOR + "/get_alerta_json.php?id=" + enc, "GET");
            String resp = llegir(conn);
            conn.disconnect();
            return (resp != null && !resp.isEmpty()) ? resp : null;
        } catch (Exception e) {
            return null;
        }
    }

    // Envia una nova alerta al servidor.
    // Retorna true si s'ha creat correctament.
    public static boolean crearAlerta(String idDispositiu, String textAlerta, int silencio) {
        try {
            String body = "ID_DISPOSITIU=" + URLEncoder.encode(idDispositiu, "UTF-8")
                    + "&TEXT_ALERTA="  + URLEncoder.encode(textAlerta,   "UTF-8")
                    + "&SILENCIO=" + silencio;
            HttpURLConnection conn = obrir(MainActivity.IP_SERVIDOR + "/crear_alerta_silencio.php", "POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.getOutputStream().write(body.getBytes("UTF-8"));
            //boolean ok = "OK".equals(llegir(conn));
            boolean ok = llegir(conn).replace("'", "").trim().equals("OK");
            conn.disconnect();
            return ok;
        } catch (Exception e) {
            android.util.Log.d("SERVIDOR", "ERROR: " + e.getClass().getName() + " - " + e.getMessage());
            return false;
        }
    }

    // Obre la connexió amb timeout de 5 s
    private static HttpURLConnection obrir(String url, String metode) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(metode);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        return conn;
    }

    // Llig la primera línia de la resposta
    private static String llegir(HttpURLConnection conn) throws Exception {
        // Leer la respuesta del servidor en caso de que este arroje error
        InputStream inputStream = (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                ? conn.getInputStream()
                : conn.getErrorStream();

        if(inputStream == null){
            return "";
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

        StringBuilder completo = new StringBuilder();
        String linea;

        while ((linea = br.readLine()) != null){
            completo.append(linea).append("\n");
        }

        br.close();
        //return resp;
        return completo.toString().trim();
    }
}