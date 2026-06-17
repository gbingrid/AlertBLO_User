package com.example.alertblo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    public static String ID_DISPOSITIU;    // ID del dispositiu que s'enviarà al servidor
    public static final String IP_SERVIDOR = "http://13.63.226.223"; // IP del servidor

    private static Context contexto;
    private static Adaptador adaptador;
    private static RecyclerView alertasActivas;
    private Spinner spnIdioma;
    private ImageButton btnSalir;
    private TabLayout filtrosAlertas;
    private Chip todas, critica, aviso;

    private static GestorGeofence geofence;
    private static final int LocRequestCode = 100;

    // Aplica el idioma antes de que se infle el layout
    @Override
    protected void attachBaseContext(Context base){
        super.attachBaseContext(GestorIdioma.cargarIdiomaGuardado(base));
    }

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
        spnIdioma = findViewById(R.id.spn_idioma);
        btnSalir = findViewById(R.id.btn_salir);
        todas.setOnClickListener(v -> adaptador.filtrarAlerta("todas"));
        critica.setOnClickListener(v -> adaptador.filtrarAlerta("critica"));
        aviso.setOnClickListener(v -> adaptador.filtrarAlerta("aviso"));
        btnSalir.setOnClickListener(v -> mostrarDialogoSalir());

        GestorIdioma.configurarSpinner(spnIdioma, this);

        filtrosAlertas.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        if(tab.getPosition() == 0){
                            adaptador.filtrarRecientes();
                        } else {
                            adaptador.filtrarAlerta("todas");
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {

                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {

                    }
                }
        );

        solicitarPermiso();
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
                });

                if(objetoAlerta.getSilencio() == 0){ // Es Alerta crítica
                    if(geofence != null){
                        geofence.comprobarNotificar(objetoAlerta);
                    } else { // Si la ubicación no está disponible, se notifica como alerta
                        MainActivity.mostrarNotificacioAlerta(contexto, objetoAlerta, "canal_alerta");
                    }

                } else {
                    mostrarNotificacioAlerta(contexto, objetoAlerta, "canal_aviso");
                }
            }
        }
    }

    // Mostra una notificació amb el text de l'alerta rebuda, i sona encara que el mòbil estigui en silenci.
    static void mostrarNotificacioAlerta(Context contexto, Alerta alerta, String canal) {

        String tituloNotificacion = alerta.getTitulo();
        String textAlerta = alerta.getDescripcion();
        int idAlerta = alerta.getIdAlerta();

        NotificationCompat.Builder creadorNotificacion = new NotificationCompat.Builder(contexto, canal)
                .setSmallIcon(R.drawable.notificationicon)
                .setContentTitle(tituloNotificacion)
                .setContentText(textAlerta)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(textAlerta))
                .setAutoCancel(true);

        if("canal_alerta".equals(canal)){
            creadorNotificacion.setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM);
            // Forzar el sonido de la alerta crítica
            try{
                MediaPlayer mediaPlayer = MediaPlayer.create(contexto, R.raw.sonidoalarma);

                if(mediaPlayer != null){
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);

                    AudioManager audioManager = (AudioManager) contexto.getSystemService(Context.AUDIO_SERVICE);
                    if(audioManager != null){
                        int maxVolumen = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
                        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolumen, 0);
                    }
                    mediaPlayer.start();
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        } else {
            creadorNotificacion.setPriority(NotificationCompat.PRIORITY_LOW);
        }

        NotificationManagerCompat notificador = NotificationManagerCompat.from(contexto);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(contexto, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                notificador.notify(idAlerta, creadorNotificacion.build());
            }
        } else {
            notificador.notify(idAlerta, creadorNotificacion.build());
        }
    }


    // Función que solicita permisos al usuario para geolocalización
    private void solicitarPermiso(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LocRequestCode);
        } else {
            geofence = new GestorGeofence(getApplicationContext());
        }
    }

    // ALERTDIALOG PARA MOSTRAR DIALOGO SALIR
    private void mostrarDialogoSalir(){
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.txt_salir))
                .setMessage(getString(R.string.txt_confirmar_salir))
                .setPositiveButton(getString(R.string.txt_si), (d, w) -> finishAffinity())
                .setNegativeButton(getString(R.string.txt_cancelar), null)
                .show();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LocRequestCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            geofence = new GestorGeofence(getApplicationContext());
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