package com.example.alertblo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class GestorGeofence {

    private static final double GeoLat = 40.418;
    private static final double GeoLon = 0.423;
    private static final double GeoRadio = 15000;
    private FusedLocationProviderClient fusedClient;
    private final Context contexto;

    public GestorGeofence(Context contexto){
        this.contexto = contexto;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(contexto);
    }

    public void comprobarNotificar(Alerta alerta){
        if(ContextCompat.checkSelfPermission(contexto,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

        fusedClient.getLastLocation().addOnSuccessListener(location -> {
            if(location == null) { // Si no hay ubicación reciente, se notifica como aviso
                MainActivity.mostrarNotificacioAlerta(contexto, alerta, "canal_aviso");
                return;
            }

            double distancia = calcularDistancia(location.getLatitude(), location.getLongitude(), GeoLat, GeoLon);

            if(distancia <= GeoRadio){
                MainActivity.mostrarNotificacioAlerta(contexto, alerta, "canal_alerta");
            } else {
                MainActivity.mostrarNotificacioAlerta(contexto, alerta, "canal_aviso");
            }
        });
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2){
        final double R = 6371000;
        double dlat = Math.toRadians(lat2 - lat1);
        double dlon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
