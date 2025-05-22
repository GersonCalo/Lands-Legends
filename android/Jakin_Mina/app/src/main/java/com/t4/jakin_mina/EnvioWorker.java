package com.t4.jakin_mina;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.t4.jakin_mina.model.AppDatabase;
import com.t4.jakin_mina.model.DatosPendientes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Esta clase es un Worker que se encarga de enviar datos pendientes al servidor
public class EnvioWorker extends Worker {
    public static final String SERVER_IP = "10.0.2.2"; // Cambiar por IP real
    public static final int SERVER_PORT = 12321; // Puerto del servidor
    public static final int TIMEOUT_MS = 5000; // Timeout para la conexión

    public EnvioWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    // Método principal que se ejecuta cuando se inicia el trabajo
    @NonNull
    @Override
    public Result doWork() {
        // Crear la base de datos y obtener los datos pendientes
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "datos-db").build();
        List<DatosPendientes> pendientes = db.datosPendientesDao().getAll();

        // Validación de datos corruptos
        for (DatosPendientes dato : new ArrayList<>(pendientes)) {
            if (dato.email == null || dato.puntuacion == null || dato.fechaHora == null) {
                Log.e("EnvioWorker", "Eliminando dato corrupto ID: " + dato.id);
                db.datosPendientesDao().delete(dato);
            }
        }

        // Obtener nuevamente la lista de datos válidos después de la limpieza
        List<DatosPendientes> datosValidos = db.datosPendientesDao().getAll();

        // Verificar la conectividad a Internet
        if (!NetworkUtils.tieneInternet(getApplicationContext())) {  // Usa NetworkUtils para comprobar la conexión
            return Result.retry(); // Reintentar más tarde si no hay conexión
        }

        // Intentar enviar cada dato válido al servidor
        for (DatosPendientes datos : datosValidos) {
            boolean exito = intentarEnvio(datos); // Intenta enviar el dato
            if (exito) {
                db.datosPendientesDao().delete(datos); // Eliminar el dato si se envió con éxito
                Log.i("EnvioWorker", "Dato ID " + datos.id + " enviado exitosamente");
            }
        }

        // Retornar resultado según si hay datos pendientes para enviar
        return datosValidos.isEmpty() ? Result.success() : Result.retry();
    }

    // Método para verificar si hay conexión a la red
    private boolean tieneConexion() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    // Método para intentar enviar un dato al servidor
    private boolean intentarEnvio(DatosPendientes datos) {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
            // Crear un socket y conectar al servidor
            socket = new Socket();
            socket.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), TIMEOUT_MS); // Conectar primero

            // Crear flujos de entrada y salida después de la conexión
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Enviar los datos
            String mensaje = datos.email + "|" + datos.puntuacion + "|" + datos.fechaHora;
            out.println(mensaje);

            // Leer respuesta del servidor
            socket.setSoTimeout(TIMEOUT_MS);
            String respuesta = in.readLine();

            return "okey".equalsIgnoreCase(respuesta); // Verificar la respuesta

        } catch (IOException e) {
            Log.e("EnvioWorker", "Error al enviar dato ID " + datos.id, e);
            return false; // Indicar fallo en el envío
        } finally {
            // Cerrar recursos manualmente
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                Log.e("EnvioWorker", "Error cerrando recursos", e);
            }
        }
    }
}
