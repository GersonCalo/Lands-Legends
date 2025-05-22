/**
 * Fragmento para el inicio de sesión de usuarios. Valida credenciales y navega a la pantalla de preguntas.
 * Implementa MVVM para separar lógica de negocio y UI.
 */
package com.t4.jakin_mina.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.t4.jakin_mina.R;
import com.t4.jakin_mina.databinding.FragmentLogInBinding;
import com.t4.jakin_mina.viewmodel.LogInViewModel;



public class LogInFragment extends Fragment {
    private FragmentLogInBinding binding; // Binding para acceso a vistas del layout
    private LogInViewModel viewModel;     // Maneja lógica de autenticación y estados

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLogInBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Inflar layout del fragmento
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(LogInViewModel.class); // Inicializar ViewModel
        setupObservers();       // Conectar UI con estados del ViewModel
        setupButtonClickListener(); // Configurar acción del botón de login
    }

    /** Configura observadores para cambios en el ViewModel */
    private void setupObservers() {
        // Controlar visibilidad de loading y estado del botón
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.button.setEnabled(!isLoading);
        });

        // Manejar resultado exitoso del login
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), success -> {
            if (success) navigateToQuestions();
            else showToast(getString(R.string.Credencialesinválidas));
        });

        // Mostrar mensajes de error
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(),
                error -> { if (!error.isEmpty()) showToast(error); });
    }

    /** Configura el click listener para el botón de login */
    private void setupButtonClickListener() {
        binding.button.setOnClickListener(v -> {
            String email = binding.editTextTextEmailAddress.getText().toString().trim();
            String company = binding.editTextCompany.getText().toString().trim();

            if (validateInputs(email, company)) {
                viewModel.loginUser(email, company); // Iniciar proceso de login
            }
        });
    }

    /** Valida que los campos no estén vacíos */
    private boolean validateInputs(String email, String company) {
        if (email.isEmpty() || company.isEmpty()) {
            showToast(getString(R.string.Rellenatodosloscampos));
            return false;
        }
        return true;
    }

    /** Navega al fragmento de preguntas llevando el email como argumento */
    private void navigateToQuestions() {
        Bundle bundle = new Bundle();
        bundle.putString("email", binding.editTextTextEmailAddress.getText().toString().trim());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_logInFragment_to_preguntaFragment, bundle);
    }

    /** Muestra mensajes toast con duración corta */
    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpiar binding para evitar leaks de memoria
    }
}