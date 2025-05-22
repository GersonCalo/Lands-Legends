/**
 * Fragmento que muestra los resultados finales de una partida/juego.
 * Maneja la visualización de puntuación, tiempo y navegación al menú principal.
 */
package com.t4.jakin_mina.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.room.Room;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.t4.jakin_mina.R;
import com.t4.jakin_mina.databinding.FragmentFinBinding;
import com.t4.jakin_mina.model.AppDatabase;
import com.t4.jakin_mina.viewmodel.FinViewModel;

public class FinFragment extends Fragment {
    private FragmentFinBinding binding; // Binding para acceder a las vistas del layout
    private FinViewModel viewModel;     // ViewModel para lógica de negocio
    NavController navController;        // Controlador de navegación entre fragmentos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentFinBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Inflar el layout del fragmento
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = Navigation.findNavController(view); // Configurar navegación

        // Inicializar ViewModel y base de datos Room
        viewModel = new ViewModelProvider(this).get(FinViewModel.class);
        viewModel.inicializarDB(
                Room.databaseBuilder(requireContext(), AppDatabase.class, "datos-db").build()
        );

        configurarObservadores(); // Conectar UI con ViewModel

        // Obtener y procesar datos de la partida desde los argumentos
        Bundle args = getArguments();
        if (args != null) {
            viewModel.procesarDatos(
                    args.getString("email"),
                    args.getString("puntuacion"),
                    args.getString("horainiciodepartida")
            );
        }

        // Botón para volver al login
        binding.exitButton.setOnClickListener(v ->
                navController.navigate(R.id.action_finFragment_to_logInFragment));
    }

    /** Observa cambios en el ViewModel para actualizar la UI */
    private void configurarObservadores() {
        // Actualizar tiempo y puntuación en pantalla
        viewModel.getTiempo().observe(getViewLifecycleOwner(), tiempo -> {
            binding.timeSpentValue.setText(tiempo);
            binding.scoreValue.setText(getArguments().getString("puntuacion"));
        });

        // Mostrar errores con Toast
        viewModel.getError().observe(getViewLifecycleOwner(),
                error -> Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show());
    }
}