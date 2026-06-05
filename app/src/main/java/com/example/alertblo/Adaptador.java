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

    private final List<Alerta> listaAlertas = new ArrayList<>();
    private final List<Alerta> todas = new ArrayList<>();

    public Adaptador() {
    }

    public void addAlerta(Alerta alerta) {
        todas.add(0, alerta);
        listaAlertas.add(0, alerta);
        notifyItemInserted(0);
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

    public void mostrarTodas(){
        for(Alerta a: todas){

        }
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView tarjeta;
        ImageView icono;
        TextView titulo;
        TextView descripcion;
        TextView fechaHora;

        ViewHolder(View v) {
            super(v);
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

        Alerta alerta = listaAlertas.get(position);

        if (alerta.getSilencio() == 0) {
            h.titulo.setText("¡ALERTA CRÍTICA!");
            h.icono.setImageResource(R.drawable.ic_alerta_critica);
        } else {
            h.titulo.setText("AVISO MUNICIPAL");
            h.icono.setImageResource(android.R.drawable.ic_dialog_info);
        }

        h.descripcion.setText(alerta.getDescripcion());
        h.fechaHora.setText(alerta.getFechaHora());
    }

    @Override
    public int getItemCount() {
        return listaAlertas.size();
    }


}