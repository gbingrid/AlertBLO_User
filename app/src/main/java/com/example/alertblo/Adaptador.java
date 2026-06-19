package com.example.alertblo;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

public class Adaptador extends RecyclerView.Adapter<Adaptador.ViewHolder> {

    private final List<Alerta> listaAlertas = new ArrayList<>();
    private final List<Alerta> todas = new ArrayList<>();

    public Adaptador() {
    }

    public void addAlerta(Alerta alerta) {
        todas.add(0, alerta);
        listaAlertas.add(0, alerta);
        notifyItemInserted(0);
        guardarAlertasEnDispositivo(MainActivity.alertasActivas.getContext());
    }

    public void filtrarAlerta(String tipo){
        listaAlertas.clear();
        for(Alerta a: todas){
            if(tipo.equals("todas")){
                listaAlertas.add(a);
            } else if(tipo.equals("critica") && a.getSilencio() == 0){
                listaAlertas.add(a);
            } else if(tipo.equals("aviso") && a.getSilencio() != 0){
                listaAlertas.add(a);
            }
        }
        notifyDataSetChanged();
    }

    public void filtrarRecientes(){
        long hace24h = System.currentTimeMillis() - (24 * 60 * 60 * 1000L);
        listaAlertas.clear();
        for(Alerta a: todas){
            if(a.getTimestamp() >= hace24h) listaAlertas.add(a);
        }
        notifyDataSetChanged();
    }

    public void guardarAlertasEnDispositivo(Context context){
        SharedPreferences prefs = context.getSharedPreferences("historial_alertas", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for(Alerta a: this.todas){
            try{
                org.json.JSONObject obj = new org.json.JSONObject();
                obj.put("ID_ALERTA", a.getIdAlerta());
                obj.put("TEXT_ALERTA", a.getDescripcion());
                obj.put("SILENCIO", a.getSilencio());
                obj.put("TIMESTAMP_GUARDADO", a.getTimestamp()); // Guardamos su tiempo real
                jsonArray.put(obj);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        prefs.edit().putString("lista_guardada", jsonArray.toString()).apply();
    }

    public void cargarAlertasDelDispositivo(Context contexto) {
        SharedPreferences prefs = contexto.getSharedPreferences("historial_alertas", Context.MODE_PRIVATE);
        String jsonString = prefs.getString("lista_guardada", null);

        if (jsonString != null) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                this.todas.clear();
                this.listaAlertas.clear(); // Tus listas del adaptador

                for (int i = 0; i < jsonArray.length(); i++) {
                    org.json.JSONObject obj = jsonArray.getJSONObject(i);
                    Alerta a = new Alerta(
                            obj.getInt("ID_ALERTA"),
                            obj.getString("TEXT_ALERTA"),
                            obj.getInt("SILENCIO")
                    );
                    // Le devolvemos su timestamp original para que no falle el filtro de 24h
                    a.setTimestamp(obj.optLong("TIMESTAMP_GUARDADO", System.currentTimeMillis()));

                    this.todas.add(a);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icono;
        TextView titulo;
        TextView descripcion;
        TextView fechaHora;
        com.google.android.material.card.MaterialCardView tarjetaAlerta;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            icono = itemView.findViewById(R.id.icono_alerta);
            titulo = itemView.findViewById(R.id.titulo);
            descripcion = itemView.findViewById(R.id.descripcion);
            fechaHora = itemView.findViewById(R.id.fechahora);
            tarjetaAlerta = itemView.findViewById(R.id.tarjeta_alerta);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_alerta, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Context context = h.itemView.getContext();
        Alerta alerta = listaAlertas.get(position);

        h.titulo.setText(alerta.getTitulo());
        h.descripcion.setText(alerta.getDescripcion());
        h.fechaHora.setText(alerta.getFechaHora());

        // Crear contenedor temporal para gestión de colores del tema
        TypedValue typedValue = new TypedValue();

        // Asignar colores e icono según el tipo de urgencia
        if (alerta.getTipo() == Alerta.Tipo.CRITICA) {
            h.icono.setImageResource(R.drawable.ic_alerta_critica);

            // Obtener colorAlertaCritica para icono y título
            context.getTheme().resolveAttribute(R.attr.colorAlertaCritica, typedValue, true);
            int colorTextAlertCritica = typedValue.data;
            h.icono.setColorFilter(colorTextAlertCritica);
            h.titulo.setTextColor(colorTextAlertCritica);

            // Obtener color naranja claro del borde para la tarjeta
            context.getTheme().resolveAttribute(R.attr.colorAlertaCriticaContainer, typedValue, true);
            h.tarjetaAlerta.setStrokeColor(typedValue.data);

        } else {
            h.icono.setImageResource(R.drawable.ic_notification);

            // Obtener colorAviso para icono y título
            context.getTheme().resolveAttribute(R.attr.colorAviso, typedValue, true);
            int colorTextAviso = typedValue.data;
            h.icono.setColorFilter(colorTextAviso);
            h.titulo.setTextColor(colorTextAviso);

            // Obtener color rojo claro del borde para la tarjeta
            context.getTheme().resolveAttribute(R.attr.colorAvisoContainer, typedValue, true);
            h.tarjetaAlerta.setStrokeColor(typedValue.data);

        }

        if(context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)){
            h.descripcion.setTextColor(typedValue.data);
        }

        if(context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValue, true)){
            h.fechaHora.setTextColor(typedValue.data);
        }

    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }


}