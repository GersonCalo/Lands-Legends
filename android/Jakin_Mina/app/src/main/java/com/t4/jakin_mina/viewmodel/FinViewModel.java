/**
 * ViewModel para manejar la lógica de la pantalla final del cuestionario:
 * - Cálculo del tiempo de partida
 * - Almacenamiento local de resultados
 * - Programación de envío de datos pendientes
 */
package com.t4.jakin_mina.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import com.t4.jakin_mina.EnvioWorker;
import com.t4.jakin_mina.NetworkUtils;
import com.t4.jakin_mina.model.AppDatabase;
import com.t4.jakin_mina.model.DatosPendientes;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

public class FinViewModel extends ViewModel {
    // LiveData para observar cambios en la UI
    private final MutableLiveData<String> _tiempo = new MutableLiveData<>(); // Tiempo transcurrido
    private final MutableLiveData<String> _error = new MutableLiveData<>();  // Mensajes de error
    private AppDatabase db; // Referencia a la base de datos Room

    public LiveData<String> getTiempo() { return _tiempo; }
    public LiveData<String> getError() { return _error; }

    /**
     * Inicializa la conexión con la base de datos
     * @param database Instancia de Room Database
     */
    public void inicializarDB(AppDatabase database) {
        this.db = database;
    }

    /**
     * Procesa los datos de la partida y calcula el tiempo transcurrido
     * @param email Correo del jugador
     * @param puntuacion Puntos obtenidos
     * @param horaInicio Marca temporal de inicio de partida
     */
    public void procesarDatos(String email, String puntuacion, String horaInicio) {
        NetworkUtils.executor.execute(() -> { // Ejecutar en hilo secundario
            try {
                // Cálculo de duración
                LocalDateTime inicio = LocalDateTime.parse(horaInicio);
                Duration duracion = Duration.between(inicio, LocalDateTime.now());

                // Formatear a MM:SS
                String diferencia = String.format("%02d:%02d",
                        duracion.toMinutes() % 60,
                        duracion.getSeconds() % 60
                );

                _tiempo.postValue(diferencia + "min"); // Actualizar UI
                guardarDatos(email, puntuacion, LocalDateTime.now().toString()); // Persistir datos
            } catch (Exception e) {
                _error.postValue("Error procesando datos: " + e.getMessage());
            }
        });
    }

    /**
     * Guarda los datos en la base de datos local y programa el envío
     */
    private void guardarDatos(String email, String puntuacion, String fechaHora) {
        DatosPendientes datos = new DatosPendientes();
        datos.email = email;
        datos.puntuacion = puntuacion;
        datos.fechaHora = fechaHora;

        try {
            db.datosPendientesDao().insert(datos); // Insertar en Room
            programarEnvio(); // Programar envío cuando haya conexión
        } catch (Exception e) {
            _error.postValue("Error guardando datos: " + e.getMessage());
        }
    }

    /**
     * Programa el envío de datos pendientes usando WorkManager con:
     * - Reintentos automáticos
     * - Requisito de conexión a internet
     */
    private void programarEnvio() {
        WorkRequest trabajo = new OneTimeWorkRequest.Builder(EnvioWorker.class)
                .setConstraints(NetworkUtils.getConstraints()) // Requerir conexión
                .setBackoffCriteria( // Política de reintentos
                        BackoffPolicy.LINEAR,
                        OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                        TimeUnit.MILLISECONDS
                )
                .build();

        WorkManager.getInstance().enqueue(trabajo);
    }
}