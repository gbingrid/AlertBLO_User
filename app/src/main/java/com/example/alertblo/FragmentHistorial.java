package com.example.alertblo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;

public class FragmentHistorial extends Fragment{

    private Chip todas, critica, aviso;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        todas = view.findViewById(R.id.chipTodas);
        critica = view.findViewById(R.id.chipAlertaCritica);
        aviso = view.findViewById(R.id.chipAlertaNormal);

        if(todas != null){
            todas.setOnClickListener(v -> {
                actualizarEstadosFiltros(todas);
                if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("todas");
            });
        }

        if(critica != null){
            critica.setOnClickListener(v -> {
                actualizarEstadosFiltros(critica);
                if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("critica");
            });
        }

        if(aviso != null){
            aviso.setOnClickListener(v -> {
                actualizarEstadosFiltros(aviso);
                if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("aviso");
            });
        }

        if(todas != null){
            actualizarEstadosFiltros(todas);
        }

        // Configuración del botón Salir
        ImageButton btnSalir = view.findViewById(R.id.btn_salir);

        // Al hacer click, se llama al método público creado en MainActivity
        btnSalir.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).mostrarDialogoSalirPublico();
            }
        });

        return view;
    }

    // Actualizar estado de los filtros
    private void actualizarEstadosFiltros(Chip chipActivo) {
        if (getContext() == null || todas == null || critica == null || aviso == null) return;

        int colorApagadoTexto = ContextCompat.getColor(requireContext(), R.color.gris_claro);
        int colorEncendidoTexto = ContextCompat.getColor(requireContext(), R.color.negro);

        todas.setChipBackgroundColorResource(R.color.gris_oscuro);
        todas.setTextColor(colorApagadoTexto);

        critica.setChipBackgroundColorResource(R.color.gris_oscuro);
        critica.setTextColor(colorApagadoTexto);

        aviso.setChipBackgroundColorResource(R.color.gris_oscuro);
        aviso.setTextColor(colorApagadoTexto);

        chipActivo.setChipBackgroundColorResource(R.color.amarillo);
        chipActivo.setTextColor(colorEncendidoTexto);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        ViewGroup contenedorLista = view.findViewById(R.id.contenedor_lista_historial);

        if(MainActivity.alertasActivas != null && MainActivity.alertasActivas.getParent() != null){
            ((ViewGroup) MainActivity.alertasActivas.getParent()).removeView(MainActivity.alertasActivas);
        }

        if(MainActivity.alertasActivas != null){

            contenedorLista.addView(MainActivity.alertasActivas);

            if(MainActivity.adaptador != null){
                MainActivity.adaptador.filtrarAlerta("todas");
                MainActivity.adaptador.notifyDataSetChanged();
            }

        }





    }
}
