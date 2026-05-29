package com.example.alertblo;

import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.*;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlertaService extends Service {

    private static final String CANAL_SERVEI   = "canal_servei";   // Canal per a la notificació persistent del servei
    private static final String CANAL_ALERTA   = "canal_alerta";   // Canal per a les alertes rebudes
    private static final int    INTERVAL_MS    = 20_000;           // Comprova cada 20 segons
    private static final int    NOTIF_SERVEI   = 1;
    private static final int    NOTIF_ALERTA   = 2;

    private final Handler handler = new Handler(Looper.getMainLooper());

    // Runnable que s'executa cada INTERVAL_MS i comprova si hi ha alertes noves
    private final Runnable comprovador = new Runnable() {
        @Override
        public void run() {
            new Thread(() -> {
                MainActivity.GetAlerta();
            }).start();
            // Tornar a programar la comprovació
            handler.postDelayed(this, INTERVAL_MS);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanals();
        // Notificació persistent obligatòria per als ForegroundService
        Notification notifServei = new NotificationCompat.Builder(this, CANAL_SERVEI)
                .setContentTitle("Servei d'alertes actiu")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_MIN)   // Mínima per no molestar
                .build();
        startForeground(NOTIF_SERVEI, notifServei);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Iniciar el bucle de comprovació
        handler.post(comprovador);
        // START_STICKY: Android reinicia el servei si el mata
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Aturar el bucle quan el servei s'atura
        handler.removeCallbacks(comprovador);
        super.onDestroy();
    }

    // Crea els dos canals de notificació (obligatori a Android 8+)
    private void crearCanals() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);

            // Canal silenciós per a la notificació del servei
            nm.createNotificationChannel(new NotificationChannel(
                    CANAL_SERVEI, "Servei en segon pla",
                    NotificationManager.IMPORTANCE_MIN));

            // Canal d'alta prioritat per a alertes (sona amb el mòbil en silenci)
            NotificationChannel canalAlerta = new NotificationChannel(
                    CANAL_ALERTA, "Alertes municipals",
                    NotificationManager.IMPORTANCE_HIGH);
            canalAlerta.setBypassDnd(true);               // Ignora el mode No Molestar
            canalAlerta.enableVibration(true);
            nm.createNotificationChannel(canalAlerta);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}