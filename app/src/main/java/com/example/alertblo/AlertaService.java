package com.example.alertblo;

import android.Manifest;
import android.app.*;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.*;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlertaService extends Service {
    private static final String CANAL_SERVEI   = "canal_servei";   // Canal per a la notificació persistent del servei
    private static final String CANAL_ALERTA   = "canal_alerta";   // Canal per a les alertes rebudes
    private static final String CANAL_AVISO = "canal_aviso";
    private static final int    INTERVAL_MS    = 20_000;           // Comprova cada 20 segons
    private static final int    NOTIF_SERVEI   = 1;
    private static final int    NOTIF_ALERTA   = 2;
    public static ExecutorService netGetThread = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Runnable que s'executa cada INTERVAL_MS i comprova si hi ha alertes noves
    private final Runnable comprovador = new Runnable() {
        @Override
        public void run() {
            netGetThread.submit(() -> {
                MainActivity.GetAlerta();
            });
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
                .setContentTitle("Servicio de alertas activo")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_MIN)   // Mínima per no molestar
                .build();
        startForeground(NOTIF_SERVEI, notifServei);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.removeCallbacks(comprovador);
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

            Uri rutaSonido = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +  "://" + getPackageName() + "/raw/sonidoalarma");
            AudioAttributes atributosSonido = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            // Canal silenciós per a la notificació del servei
            nm.createNotificationChannel(new NotificationChannel(
                    CANAL_SERVEI, "Servei en segon pla",
                    NotificationManager.IMPORTANCE_MIN));

            // Canal d'alta prioritat per a alertes (sona amb el mòbil en silenci)
            NotificationChannel canalAlerta = new NotificationChannel(
                    CANAL_ALERTA, "¡ALERTA CRÍTICA!",
                    NotificationManager.IMPORTANCE_HIGH);
            canalAlerta.setSound(rutaSonido, atributosSonido);
            canalAlerta.setBypassDnd(true);               // Ignora el mode No Molestar
            canalAlerta.enableVibration(true);
            nm.createNotificationChannel(canalAlerta);

            // Canal de baja prioridad para avisos (no emite sonido)
            NotificationChannel canalAviso = new NotificationChannel(
                    CANAL_AVISO, "AVISO MUNICIPAL",
                    NotificationManager.IMPORTANCE_LOW);
            canalAviso.enableVibration(false);
            nm.createNotificationChannel(canalAviso);

        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}