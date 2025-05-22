package com.t4.jakin_mina;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import androidx.work.Constraints;
import androidx.work.NetworkType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

// Esta clase proporciona métodos para verificar la conectividad de red y la disponibilidad de un servidor TCP.
public class NetworkUtils {
    public static final Executor executor = Executors.newFixedThreadPool(4); // Executor para tareas asíncronas

    // Método para verificar si hay conexión a Internet a través de un host específico y puerto
    public static boolean tieneInternet(Context context, String host, int port) {
        if (!tieneConexionRed(context)) return false; // Comprobar si hay conexión de red
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), 1500); // Intentar conectar al servidor
            return true; // Conexión exitosa
        } catch (IOException e) {
            return false; // Falla en la conexión
        }
    }

    // Método privado para comprobar la conexión de red
    private static boolean tieneConexionRed(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm == null) {
            return false; // El servicio de conectividad no está disponible
        }

        // Para Android 10+ (API 29+), usa NetworkCapabilities
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Network network = cm.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(network);
            // Verificar si hay transporte disponible (Wi-Fi, celular, Ethernet)
            return capabilities != null
                    && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            // Para versiones anteriores (obsoleto pero funcional)
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting(); // Comprobar conexión
        }
    }

    // Método para verificar la disponibilidad de un servidor TCP de manera asíncrona
    public static void servidorTcpDisponibleAsync(String ip, int puerto, int timeoutMs, Consumer<Boolean> callback) {
        executor.execute(() -> {
            boolean disponible = servidorTcpDisponible(ip, puerto, timeoutMs);
            callback.accept(disponible); // Llamar al callback con el resultado
        });
    }

    // Método privado para comprobar la disponibilidad del servidor TCP
    private static boolean servidorTcpDisponible(String ip, int puerto, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, puerto), timeoutMs); // Conectar al servidor
            return true; // Conexión exitosa
        } catch (IOException e) {
            return false; // Falla en la conexión
        }
    }

    // Método para obtener restricciones para trabajos de WorkManager que requieren conexión
    public static Constraints getConstraints() {
        return new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Necesita estar conectado
                .build();
    }

    // Método original con valores por defecto para comprobar la conectividad a Internet
    public static boolean tieneInternet(Context context) {
        return tieneInternet(context, "8.8.8.8", 53); // Usar DNS de Google como host por defecto
    }
}