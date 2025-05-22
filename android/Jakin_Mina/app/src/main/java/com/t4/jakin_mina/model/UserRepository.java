/**
 * Clase para manejar autenticación de usuarios mediante un servicio web.
 * Usa OkHttp para solicitudes HTTP y callbacks para resultados asíncronos.
 */
package com.t4.jakin_mina.model;

import java.io.IOException;
import java.net.URLEncoder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserRepository {
    // Interfaz para comunicar resultados del login
    public interface LoginCallback {
        void onSuccess(boolean userExists);
        void onError(String message);
    }

    private final OkHttpClient client = new OkHttpClient(); // Cliente HTTP reutilizable

    /**
     * Realiza login asíncrono codificando parámetros y consultando el servidor.
     * @param email Correo del usuario
     * @param company Nombre de la empresa
     * @param callback Maneja éxito/error de la operación
     */
    public void login(String email, String company, LoginCallback callback) {
        new Thread(() -> { // Ejecutar en subproceso secundario
            try {
                // Codificar parámetros para URL
                String encodedEmail = URLEncoder.encode(email, "UTF-8").replace("+", "%20");
                String encodedCompany = URLEncoder.encode(company, "UTF-8").replace("+", "%20");

                // Construir URL con parámetros codificados
                String url = "http://10.0.2.2:1111/employee/login/"
                        + encodedEmail + "/{companyName}?companyName=" + encodedCompany;

                Response response = client.newCall(new Request.Builder().url(url).build()).execute();

                if (response.isSuccessful()) {
                    // Convertir respuesta a booleano (true=usuario existe)
                    boolean userExists = Boolean.parseBoolean(response.body().string().trim());
                    callback.onSuccess(userExists);
                } else {
                    callback.onError("Error del servidor: " + response.code());
                }
            } catch (IOException e) { // Errores de red
                callback.onError("Error de conexión: " + e.getMessage());
            } catch (Exception e) { // Errores inesperados
                callback.onError("Error inesperado: " + e.getMessage());
            }
        }).start();
    }
}