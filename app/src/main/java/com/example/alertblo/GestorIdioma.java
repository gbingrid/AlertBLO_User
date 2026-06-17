package com.example.alertblo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import android.widget.Spinner;

/**
 * Esta clase centraliza todo lo relacionado con el idioma de la app:
 * -Guardar la preferencia del usuario
 * -Aplicar el idioma guardado al iniciar cualquier Activity
 * -Gestionar el Spinner que permite cambiar la Activity desde la pantalla
 */
public class GestorIdioma{

    private static final String ARCHIVO_PREFS = "preferencias_idioma"; // Nombre del archivo donde se guarda la preferencia en el dispositivo
    private static final String CLAVE_IDIOMA = "idioma_seleccionado"; // Nombre de la casilla dentro de ARCHIVO_PREFS donde se guarda el código del idioma ("ca"/"es")
    private static final String[] CODIGOS_IDIOMA = {"es", "ca"}; // Códigos de idiomas soportados por la app, se mantiene el mismo orden que el string-array "idiomas" de strings.xml


    /**
     * Guarda el idioma seleccionado por el usuario en SharedPreferences y devuelve un Context ya configurado con ese idioma
     *
     * @param context
     * @param codigoIdioma
     * @return context
     */
     public static Context setIdioma(Context context, String codigoIdioma){
        // Abrir o crear si no existe el archivo de preferencias y escribir el idioma seleccionado
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putString(CLAVE_IDIOMA, codigoIdioma).apply();
        return actualizarIdioma(context, codigoIdioma);
    }

    /**
     * Lee el idioma que quedó guardado de una sesión anterior o castellano si es la primera vez que se abre la app
     * y aplica esa configuración al Context recibido.
     * Se usa en AttachBaseContext() de cada Activity, antes de que se infle el layout para que los textos
     * se muestren en el idioma correcto.
     *
     * @param context
     * @return context
     */

    public static Context cargarIdiomaGuardado(Context context){
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        String idioma = prefs.getString(CLAVE_IDIOMA, "es"); // Idioma por defecto
        return actualizarIdioma(context, idioma);
    }

    /**
     * Construye una nueva configuración de idioma(Locale) y la aplica al Context
     *
     * @param context
     * @param codigoIdioma
     * @return context nuevo
     */
    public static Context actualizarIdioma(Context context, String codigoIdioma){
        Locale locale = new Locale(codigoIdioma);
        Locale.setDefault(locale);

        // Se copia la configuración actual del dispositivo y solo se le cambia el idioma
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            // Forma moderna: devuelve un Context nuevo, no modifica el original
            return context.createConfigurationContext(configuration);
        }else{
            // Forma antigüa (Android < 7): modifica los recursos del Context existente
            context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    /**
     * Devuelve el código del idioma("es": Castellano o "ca": Catalán/Valenciano) que está guardado actualmente
     * Posteriormente se utiliza el código devuelto para comparar el idioma actual de la app con el seleccionado por el usuario, para no realizar cambios innecesarios.
     *
     * @param context
     * @return string
     */
    // DEVUELVE EL CÓDIGO DEL IDIOMA ACTUALMENTE GUARDADO
    public static String getIdiomaActual(Context context){
        SharedPreferences prefs = context.getSharedPreferences(ARCHIVO_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(CLAVE_IDIOMA, "es");
    }

    /**
     * Configura el Spinner de idioma de una Activity:
     * -Pone textos visibles
     * -Deja marcado el idioma activo
     * -Aplica los cambios seleccionados por el usuario
     *
     * @param spinner
     * @param mainActivity
     */
    public static void configurarSpinner(Spinner spinner, AppCompatActivity mainActivity){

        // Crea los textos del desplegable a partir del string-array "idiomas" de strings.xml
        ArrayAdapter<CharSequence> adapterIdioma = ArrayAdapter.createFromResource(
                mainActivity, R.array.idiomas, android.R.layout.simple_spinner_item);
        adapterIdioma.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapterIdioma);

        // Calcula qué posición del desplegable corresponde al idioma ya guardado para que el Spinner se abra mostrando la opción correcta
        String idiomaActual = getIdiomaActual(mainActivity);
        int posActual = idiomaActual.equals("ca") ? 1 : 0;
        spinner.setSelection(posActual);

        spinner.post(() -> spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
                // Se traduce la posición seleccionada en el desplegable(0 ,1) al código de idioma("es", "ca")
                String nuevoIdioma = CODIGOS_IDIOMA[position];

                // Solo se cambia algo si el idioma seleccionado es distinto al actual para no realizar cambios innecesarios
                if(!nuevoIdioma.equals(getIdiomaActual(mainActivity))){
                    setIdioma(mainActivity, nuevoIdioma);
                    // recreate() destruye y vuelve a crear la Activity entera, se ejecuta nuevamente AttachBaseContext() y se muestran los textos en el nuevo idioma
                    mainActivity.recreate();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent){}
        }));
    }
}
