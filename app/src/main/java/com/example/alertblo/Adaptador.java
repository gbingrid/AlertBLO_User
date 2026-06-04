package com.example.alertblo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class Adaptador extends RecyclerView.Adapter<Adaptador.ViewHolder> {

    // Una sola lista directa, sin complicaciones de filtros
    private final List<Alerta> listaAlertas = new ArrayList<>();

    // Constructor vacío: ahora es súper fácil de crear desde el MainActivity
    public Adaptador() {
    }

    // El "molde" que busca los componentes dentro de tu item_alerta.xml
    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView tarjeta;
        ImageView icono;
        TextView titulo;
        TextView descripcion;
        TextView fechaHora;

        ViewHolder(View v) {
            super(v);
            tarjeta = v.findViewById(R.id.tarjeta_alerta);
            icono = v.findViewById(R.id.icono_alerta);
            titulo = v.findViewById(R.id.txt_titulo);
            descripcion = v.findViewById(R.id.txt_descripcion);
            fechaHora = v.findViewById(R.id.txt_fechahora);
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
        // Obtenemos la alerta de la posición correspondiente
        Alerta alerta = listaAlertas.get(position);

        // Ponemos el título dinámico según el campo 'silencio' de tu JSON (0 = Crítica)
        if (alerta.getSilencio() == 0) {
            h.titulo.setText("¡ALERTA CRÍTICA!");
            h.icono.setImageResource(R.drawable.ic_alerta_critica); // Icono rojo
        } else {
            h.titulo.setText("AVISO MUNICIPAL");
            h.icono.setImageResource(android.R.drawable.ic_dialog_info); // Icono azul/info por defecto
        }

        // Unimos tus variables reales del objeto Alerta con los textos del diseño
        h.descripcion.setText(alerta.getDescripcion());
        h.fechaHora.setText(alerta.getFechaHora());
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }

    // Método sencillo para añadir alertas desde el MainActivity
    public void addAlerta(Alerta alerta) {
        listaAlertas.add(0, alerta); // La añade arriba del todo de la lista
        notifyItemInserted(0);       // Le avisa al RecyclerView que se dibuje
    }
}