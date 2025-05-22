/**
 * Fragmento que muestra preguntas en un cuestionario interactivo. Incluye:
 * - Soporte para Text-to-Speech (lectura de preguntas en voz alta)
 * - Manejo de navegación entre preguntas
 * - Conexión con ViewModel para lógica de negocio
 * - Sistema de puntuación basado en respuestas correctas
 */
package com.t4.jakin_mina.view;

import android.media.AudioManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.t4.jakin_mina.R;
import com.t4.jakin_mina.databinding.FragmentPreguntaBinding;
import com.t4.jakin_mina.model.Pregunta;
import com.t4.jakin_mina.viewmodel.PreguntaViewModel;
import java.util.List;
import java.util.Locale;

public class PreguntaFragment extends Fragment implements TextToSpeech.OnInitListener {
    private FragmentPreguntaBinding binding;       // Binding para acceso a vistas
    private PreguntaViewModel viewModel;           // Maneja lógica de preguntas y respuestas
    private String email;                          // Email del jugador
    private String horaInicioPartida;              // Hora de inicio de la partida
    private NavController navController;           // Controlador de navegación
    private String respuestaSeleccionada;          // Respuesta seleccionada por el usuario
    private TextToSpeech ttsEngine;                // Motor de texto a voz
    private boolean ttsInitialized = false;        // Bandera de inicialización TTS
    private String idioma = "es";                  // Idioma predeterminado

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPreguntaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        detectarIdiomaDispositivo();
        ttsEngine = new TextToSpeech(requireContext(), this);
        setupNavigation();
        obtenerArgumentos();
        inicializarViewModel();
        configurarUI();
        observarViewModel();
        cargarPreguntas();
        binding.questionText.setOnClickListener(v -> hablarPregunta());
    }

    /** Detecta el idioma del dispositivo para mostrar contenido localizado */
    private void detectarIdiomaDispositivo() {
        String langCode = Locale.getDefault().getLanguage();
        if (langCode.equals("es")) idioma = "es";
        else if (langCode.equals("eu")) idioma = "eu";
        else idioma = "en";
    }

    /** Configura el motor de Text-to-Speech con el idioma detectado */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int resultado = ttsEngine.setLanguage(Locale.forLanguageTag(idioma));
            if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(requireContext(), R.string.Idiomanosoportado, Toast.LENGTH_SHORT).show();
            } else {
                ttsInitialized = true;
            }
        } else {
            Toast.makeText(requireContext(), R.string.ErroralinicializarTTS, Toast.LENGTH_SHORT).show();
        }
    }

    /** Lee la pregunta actual usando TTS */
    private void hablarPregunta() {
        if (ttsInitialized) {
            Bundle params = new Bundle();
            params.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC);
            ttsEngine.speak(binding.questionText.getText().toString(), TextToSpeech.QUEUE_FLUSH, params, "tts_utterance");
        }
    }

    /** Configura la navegación entre fragments */
    private void setupNavigation() {
        navController = Navigation.findNavController(requireView());
    }

    /** Obtiene parámetros pasados desde el fragmento anterior */
    private void obtenerArgumentos() {
        email = getArguments().getString("email");
        horaInicioPartida = java.time.LocalDateTime.now().toString();
    }

    /** Inicializa el ViewModel para separar lógica de negocio */
    private void inicializarViewModel() {
        viewModel = new ViewModelProvider(this).get(PreguntaViewModel.class);
    }

    /** Configura los listeners de UI y componentes visuales */
    private void configurarUI() {
        configurarRadioButtons();
        binding.nextButton.setOnClickListener(v -> manejarBotonSiguiente());
    }

    /** Configura el comportamiento de los RadioButtons para selección única */
    private void configurarRadioButtons() {
        RadioButton[] radioButtons = {
                binding.radioButton1,
                binding.radioButton2,
                binding.radioButton3,
                binding.radioButton4
        };

        for (RadioButton radioButton : radioButtons) {
            radioButton.setOnClickListener(v -> manejarSeleccionRespuesta(radioButton));
        }
    }

    /** Maneja la selección de una respuesta deseleccionando las demás */
    private void manejarSeleccionRespuesta(RadioButton selectedRadio) {
        for (RadioButton rb : new RadioButton[]{binding.radioButton1, binding.radioButton2,
                binding.radioButton3, binding.radioButton4}) {
            if (rb != selectedRadio) rb.setChecked(false);
        }

        // Obtener texto de la opción seleccionada
        View parentLayout = (View) selectedRadio.getParent().getParent();
        TextView textView = null;

        if (parentLayout.getId() == R.id.option_1) textView = binding.optionText1;
        else if (parentLayout.getId() == R.id.option_2) textView = binding.optionText2;
        else if (parentLayout.getId() == R.id.option_3) textView = binding.optionText3;
        else if (parentLayout.getId() == R.id.option_4) textView = binding.optionText4;

        if (textView != null) respuestaSeleccionada = textView.getText().toString();
    }

    /** Configura observadores para cambios en el ViewModel */
    private void observarViewModel() {
        viewModel.getPreguntas().observe(getViewLifecycleOwner(), this::actualizarPregunta);
        viewModel.getCurrentIndex().observe(getViewLifecycleOwner(), this::manejarIndicePregunta);
        viewModel.getError().observe(getViewLifecycleOwner(), this::mostrarError);
        viewModel.getLoading().observe(getViewLifecycleOwner(), this::mostrarLoading);
        viewModel.respuestaCorrecta().observe(getViewLifecycleOwner(), correcta -> {
            if (correcta != null) {
                String mensaje = correcta ? getString(R.string.Correcto) : getString(R.string.Incorrecto);
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Solicita al ViewModel que cargue las preguntas desde la fuente de datos */
    private void cargarPreguntas() {
        viewModel.cargarPreguntas();
    }

    /** Actualiza la UI con nueva pregunta y opciones */
    private void actualizarPregunta(List<Pregunta> preguntas) {
        if (preguntas == null || preguntas.isEmpty()) return;

        Pregunta preguntaActual = preguntas.get(viewModel.getCurrentIndex().getValue());
        binding.questionNumber.setText(String.format("%d/5", viewModel.getCurrentIndex().getValue() + 1));
        binding.questionText.setText(preguntaActual.getPregunta(idioma));

        List<String> opciones = preguntaActual.getOpcionesPorIdioma(idioma);
        binding.optionText1.setText(opciones.get(0));
        binding.optionText2.setText(opciones.get(1));
        binding.optionText3.setText(opciones.get(2));
        binding.optionText4.setText(opciones.get(3));
    }

    /** Maneja cambios en el índice de la pregunta actual */
    private void manejarIndicePregunta(Integer indice) {
        if (indice == null || viewModel.getPreguntas().getValue() == null) return;

        if (indice < 5) {
            actualizarPregunta(viewModel.getPreguntas().getValue());
        } else {
            navegarAFragmentoFinal();
        }
    }

    /** Valida y procesa la respuesta antes de avanzar */
    private void manejarBotonSiguiente() {
        if (respuestaSeleccionada == null) {
            Toast.makeText(requireContext(), R.string.Seleccionaunarespuesta, Toast.LENGTH_SHORT).show();
            return;
        }

        Pregunta actual = viewModel.getPreguntas().getValue().get(
                viewModel.getCurrentIndex().getValue()
        );

        if (actual != null) {
            viewModel.verificarRespuesta(
                    respuestaSeleccionada.charAt(0),
                    actual.getCorrecta().charAt(0)
            );
        }

        resetearSeleccion();
    }

    /** Reinicia la selección de respuestas para la siguiente pregunta */
    private void resetearSeleccion() {
        respuestaSeleccionada = null;
        for (RadioButton rb : new RadioButton[]{binding.radioButton1, binding.radioButton2,
                binding.radioButton3, binding.radioButton4}) {
            rb.setChecked(false);
        }
    }

    /** Navega al fragmento final con los resultados de la partida */
    private void navegarAFragmentoFinal() {
        Bundle bundle = new Bundle();
        bundle.putString("email", email);
        bundle.putString("puntuacion", String.valueOf(viewModel.getPuntuacion().getValue()));
        bundle.putString("horainiciodepartida", horaInicioPartida);
        navController.navigate(R.id.action_preguntaFragment_to_finFragment, bundle);
    }

    /** Muestra/Oculta elementos durante la carga de datos */
    private void mostrarLoading(boolean mostrar) {
        binding.progressBar.setVisibility(mostrar ? View.VISIBLE : View.GONE);
        binding.tvLoading.setVisibility(mostrar ? View.VISIBLE : View.GONE);

        int visibility = mostrar ? View.GONE : View.VISIBLE;
        binding.questionNumber.setVisibility(visibility);
        binding.questionText.setVisibility(visibility);
        binding.option1.setVisibility(visibility);
        binding.option2.setVisibility(visibility);
        binding.option3.setVisibility(visibility);
        binding.option4.setVisibility(visibility);
        binding.nextButton.setVisibility(visibility);
    }

    /** Muestra errores y regresa a pantalla anterior */
    private void mostrarError(String mensaje) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show();
        navController.popBackStack();
    }

    /** Limpia recursos del TTS al destruir el fragmento */
    @Override
    public void onDestroyView() {
        if (ttsEngine != null) {
            ttsEngine.stop();
            ttsEngine.shutdown();
        }
        super.onDestroyView();
        binding = null;
    }
}