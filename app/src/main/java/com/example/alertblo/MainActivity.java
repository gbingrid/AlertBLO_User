package com.example.alertblo;

import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

    public static String ID_DISPOSITIU;    // ID del dispositiu que s'enviarà al servidor
    public static final String IP_SERVIDOR = "http://13.63.226.223"; // IP del servidor

    private static Context contexto;
    private RecyclerView alertasActivas;
    private TabLayout filtrosAlertas;
    private Chip todas, alertaRoja, alertaNaranja, alertaAmarilla;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PrepararApp();

        // Enlazar cada variable con su componente del XML
        alertasActivas = findViewById(R.id.recyclerViewAlertas);
        filtrosAlertas = findViewById(R.id.tabLayoutAlertas);
        contexto = getApplicationContext();
        todas = findViewById(R.id.chipTodas);
        alertaRoja = findViewById(R.id.chipAlertaRoja);
        alertaNaranja = findViewById(R.id.chipAlertaNaranja);
        alertaAmarilla = findViewById(R.id.chipAlertaAmarilla);
    }

    // Consulta al servidor si hi ha una alerta nova per a aquest dispositiu.
    public static void GetAlerta() {
        String alerta = Servidor.getAlerta(MainActivity.ID_DISPOSITIU);

        // Verificar que alerta no está vacía y su contenido no es nulo
        if(alerta != null && !alerta.isEmpty()){
            mostrarNotificacioAlerta(alerta);
        }
    }

    // Mostra una notificació amb el text de l'alerta rebuda, i sona encara que el mòbil estigui en silenci.
    private static void mostrarNotificacioAlerta(String textAlerta) {

        Uri ruta = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +  "://" + contexto.getPackageName() + "/raw/sonidoalarma");
        NotificationManager nm = (NotificationManager) contexto.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    // Crea una nova alerta al servidor SOLO PARA APP POLICIA
    /*
    private void crearAlerta() {
        // TODO
        String textAlerta = ""; // TODO

        // Cridar al servidor per crear l'alerta
        Servidor.crearAlerta(ID_DISPOSITIU, textAlerta);
    }*/


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