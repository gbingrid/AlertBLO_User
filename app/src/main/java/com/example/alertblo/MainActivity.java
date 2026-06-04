package com.example.alertblo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    public static String ID_DISPOSITIU;    // ID del dispositiu que s'enviarà al servidor
    public static final String IP_SERVIDOR = "http://13.63.226.223"; // IP del servidor

    private static Context contexto;
    private static Adaptador adaptador;
    private static RecyclerView alertasActivas;
    private TabLayout filtrosAlertas;
    private Chip todas, critica, aviso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertasActivas = findViewById(R.id.recyclerViewAlertas);
        alertasActivas.setLayoutManager(new LinearLayoutManager(this));

        adaptador = new Adaptador();
        alertasActivas.setAdapter(adaptador);

        filtrosAlertas = findViewById(R.id.tabLayoutAlertas);
        contexto = getApplicationContext();
        todas = findViewById(R.id.chipTodas);
        critica = findViewById(R.id.chipAlertaCritica);
        aviso = findViewById(R.id.chipAlertaNormal);
        PrepararApp();
    }

    // Consulta al servidor si hi ha una alerta nova per a aquest dispositiu.
    public static void GetAlerta() {
        String alerta = Servidor.getAlerta(MainActivity.ID_DISPOSITIU);

        // Verificar que alerta no está vacía y su contenido no es nulo
        if(alerta != null){
            Alerta objetoAlerta = Alerta.parsearAlerta(alerta);

            if(objetoAlerta != null && !objetoAlerta.getDescripcion().isEmpty()){

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    adaptador.addAlerta(objetoAlerta);

                    alertasActivas.smoothScrollToPosition(0);

                    if(objetoAlerta.getSilencio() == 0){ // Es Alerta crítica
                        mostrarNotificacioAlerta(contexto, objetoAlerta.getDescripcion(), "canal_alerta");
                    } else {
                        mostrarNotificacioAlerta(contexto, objetoAlerta.getDescripcion(), "canal_aviso");

                    }
                });


            }

        }
    }

    // Mostra una notificació amb el text de l'alerta rebuda, i sona encara que el mòbil estigui en silenci.
    private static void mostrarNotificacioAlerta(Context contexto, String textAlerta, String canal) {

        NotificationCompat.Builder creadorNotificacion = new NotificationCompat.Builder(contexto, canal)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle(canal.equals("canal_alerta") ? "¡ALERTA MUNICIPAL!" : "AVISO MUNICIPAL")
                .setContentText(textAlerta)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(textAlerta))
                .setAutoCancel(true);

        if("canal_alerta".equals(canal)){
            creadorNotificacion.setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM);
        } else {
            creadorNotificacion.setPriority(NotificationCompat.PRIORITY_LOW);
        }

        NotificationManagerCompat notificador = NotificationManagerCompat.from(contexto);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){

                notificador.notify(1001, creadorNotificacion.build());

            } else {
                ActivityCompat.requestPermissions((Activity) contexto, new String[]{
                        Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }

        } else {
            notificador.notify(1001, creadorNotificacion.build());
        }


    }


    // Funció que prepara l'app: obté l'ID del dispositiu, demana permisos i arranca el servei en segon pla.
    private void PrepararApp() {
        // Obté l'identificador únic d'aquest dispositiu
        ID_DISPOSITIU = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        // Demanar permís de notificacions a Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        // Arrancar el servei en segon pla que comprova alertes cada 20 segons.
        // startForegroundService garanteix que funcioni fins i tot amb la app tancada.
        Intent srv = new Intent(this, AlertaService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(srv);
        } else {
            startService(srv);
        }
    }
}