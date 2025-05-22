/**
 * ViewModel para manejar la lógica del cuestionario:
 * - Carga de preguntas desde Firebase
 * - Gestión de puntuación y progreso
 * - Verificación de respuestas
 */
package com.t4.jakin_mina.viewmodel;


import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.t4.jakin_mina.model.Pregunta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreguntaViewModel extends ViewModel {
    // LiveData encapsulados
    private final MutableLiveData<List<Pregunta>> _preguntas = new MutableLiveData<>();
    private final MutableLiveData<Integer> _currentIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> _puntuacion = new MutableLiveData<>(50);
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _loading = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> _respuestaCorrecta = new MutableLiveData<>();

    // Exposición de LiveData inmutables
    public LiveData<List<Pregunta>> getPreguntas() { return _preguntas; }
    public LiveData<Integer> getCurrentIndex() { return _currentIndex; }
    public LiveData<Integer> getPuntuacion() { return _puntuacion; }
    public LiveData<String> getError() { return _error; }
    public LiveData<Boolean> getLoading() { return _loading; }
    public LiveData<Boolean> respuestaCorrecta() { return _respuestaCorrecta; }

    // Conexión a Firebase
    private final FirebaseDatabase database = FirebaseDatabase.getInstance(
            "https://landlegends-75e53-default-rtdb.europe-west1.firebasedatabase.app/"
    );

    /**
     * Carga las preguntas desde Firebase y selecciona 5 aleatorias
     */
    public void cargarPreguntas() {
        _loading.setValue(true);
        _preguntas.setValue(new ArrayList<>());
        DatabaseReference ref = database.getReference("preguntas");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Pregunta> preguntas = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Pregunta p = ds.getValue(Pregunta.class);
                    if (p != null) preguntas.add(p);
                }

                if (preguntas.isEmpty()) {
                    _error.postValue("No hay preguntas disponibles");
                } else {
                    _preguntas.postValue(seleccionarAleatorias(preguntas, 5));
                    _currentIndex.postValue(0);
                    _puntuacion.postValue(50);
                }
                _loading.postValue(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                _error.postValue("Error: " + error.getMessage());
                _loading.postValue(false);
            }
        });
    }

    /**
     * Verifica si la respuesta del usuario es correcta y actualiza la puntuación
     * @param respuestaUsuario Respuesta seleccionada por el usuario
     * @param correcta Respuesta correcta
     */
    public void verificarRespuesta(char respuestaUsuario, char correcta) {
        Integer actual = _puntuacion.getValue() != null ? _puntuacion.getValue() : 50;
        boolean esCorrecta = (respuestaUsuario == correcta);
        _respuestaCorrecta.postValue(esCorrecta);
        if (!esCorrecta) _puntuacion.postValue(Math.max(actual - 10, 0));
        avanzarPregunta();
    }

    /** Avanza al siguiente índice de pregunta */
    private void avanzarPregunta() {
        Integer actual = _currentIndex.getValue() != null ? _currentIndex.getValue() : 0;
        _currentIndex.postValue(actual + 1);
    }

    /**
     * Selecciona un subconjunto aleatorio de preguntas
     * @param fuente Lista completa de preguntas
     * @param cantidad Número de preguntas a seleccionar
     * @return Subconjunto aleatorio
     */
    private List<Pregunta> seleccionarAleatorias(List<Pregunta> fuente, int cantidad) {
        Collections.shuffle(fuente);
        return fuente.subList(0, Math.min(cantidad, fuente.size()));
    }
    /** Limpia los recursos cuando el ViewModel es destruido */
    @Override
    protected void onCleared() { super.onCleared();}
}
