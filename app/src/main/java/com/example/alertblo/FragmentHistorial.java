package com.example.alertblo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;

public class FragmentHistorial extends Fragment{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        Chip todas = view.findViewById(R.id.chipTodas);
        Chip critica = view.findViewById(R.id.chipAlertaCritica);
        Chip aviso = view.findViewById(R.id.chipAlertaNormal);

        todas.setOnClickListener(v -> {
            if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("todas");
        });
        critica.setOnClickListener(v -> {
            if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("critica");
        });
        aviso.setOnClickListener(v -> {
            if(MainActivity.adaptador != null) MainActivity.adaptador.filtrarAlerta("aviso");
        });

        return view;
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
            }

        }





    }
}
