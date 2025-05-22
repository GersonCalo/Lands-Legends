package com.t4.jakin_mina;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

// Esta clase es un Worker que se encarga de crear y mostrar notificaciones.
public class NotificationWorker extends Worker {
    private static final String CHANNEL_ID = "daily_reminder_channel"; // ID del canal de notificación

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    // Método principal que se ejecuta cuando se inicia el trabajo de notificación
    @NonNull
    @Override
    public Result doWork() {
        showNotification(); // Mostrar la notificación
        return Result.success(); // Retornar éxito al finalizar
    }

    // Método para configurar y mostrar la notificación
    private void showNotification() {
        createNotificationChannel(); // Crear el canal si no existe

        // Crear el constructor de la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(getApplicationContext().getString(R.string.notification_title)) // Título de la notificación
                .setContentText(getApplicationContext().getString(R.string.notification_text)) // Texto de la notificación
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad de la notificación
                .setAutoCancel(true); // Cancelar automáticamente al ser tocada

        // Obtener el NotificationManager para mostrar la notificación
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        // Mostrar la notificación con un ID único
        manager.notify(1, builder.build());
    }

    // Método para crear un canal de notificación (requerido en Android Oreo y versiones posteriores)
    private void createNotificationChannel() {
        // Comprobar si la versión es Oreo o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getApplicationContext().getString(R.string.channel_name); // Nombre del canal
            String description = getApplicationContext().getString(R.string.channel_description); // Descripción del canal
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description); // Establecer descripción del canal

            // Crear el canal en el NotificationManager
            NotificationManager manager = getApplicationContext().getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}