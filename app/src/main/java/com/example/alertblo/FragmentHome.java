package com.example.alertblo;

import android.view.View;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FragmentHome extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Se infla el diseño XML de la pantalla de inicio
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle saverInstanceState){
        super.onViewCreated(view, saverInstanceState);

        ViewGroup contenedorLista = view.findViewById(R.id.contenedor)
    }
    @Override
    public void onResume() {
        super.onResume();
        // Filtrar nuevamente al regresar a la pantalla por si pasó el tiempo
        if (adaptador != null) {
            adaptador.filtrarRecientes();
        }
    }
}
