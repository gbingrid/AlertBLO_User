package com.example.alertblo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import java.util.Locale;

public class GestorIdioma {

    // Nombre del archivo invisible en el dispositivo
    private static final String ARCHIVO_PREFS = "preferencias_idioma";
    // Nombre de la casilla donde se guarda  "es" o "ca"
    private static final String CLAVE_IDIOMA = "idioma_seleccionado";


    // GUARDA EL IDIOMA, APLICA LOS CAMBIOS Y DEVUELVE EL NUEVO CONTEXTO
    public static Context setIdioma(Context context, String codigoIdioma){
        // GUARDAR EL IDIOMA ELEGIDO
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(CLAVE_IDIOMA, codigoIdioma).apply();

        return actualizarIdioma(context, codigoIdioma);
    }

    // CARGA EL IDIOMA GUARDADO O EL QUE TIENE POR DEFECTO EL DISPOSITIVO
    public static Context cargarIdiomaGuardado(Context context){
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        String idioma = prefs.getString(CLAVE_IDIOMA, "es"); // Idioma por defecto
        return actualizarIdioma(context, idioma);
    }

    // APLICA LA CONFIGURACIÓN LOCALE AL CONTEXTO Y LO DEVUELVE
    public static Context actualizarIdioma(Context context, String codigoIdioma){
        Locale locale = new Locale(codigoIdioma);
        Locale.setDefault(locale);

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.createConfigurationContext(configuration);
        }else{
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    // DEVUELVE EL CÓDIGO DEL IDIOMA ACTUALMENTE GUARDADO
    public static String getIdiomaActual(Context context){
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(CLAVE_IDIOMA, "es");
    }
}
