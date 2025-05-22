package com.t4.jakin_mina;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;

// Esta clase es un receptor de difusión que recibe alarmas diarias y gestiona notificaciones y reprogramaciones de alarmas.
public class DailyReminderReceiver extends BroadcastReceiver {

    // Este método se invoca cuando se recibe la alarma programada.
    @Override
    public void onReceive(Context context, Intent intent) {
        // Crear una tarea de trabajo para mostrar la notificación.
        OneTimeWorkRequest workRequest =
                new OneTimeWorkRequest.Builder(NotificationWorker.class).build();
        WorkManager.getInstance(context).enqueue(workRequest);

        // Llamar al método para programar la siguiente alarma.
        scheduleNextAlarm(context);
    }

    // Método privado para programar la alarma para el próximo día.
    private void scheduleNextAlarm(Context context) {
        try {
            // Obtener el servicio de AlarmManager.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent newIntent = new Intent(context, DailyReminderReceiver.class);

            // Crear un PendingIntent para el receptor.
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    newIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Configurar el horario para la próxima alarma (10:00 AM del siguiente día).
            Calendar nextDay = Calendar.getInstance();
            nextDay.set(Calendar.HOUR_OF_DAY, 10);
            nextDay.set(Calendar.MINUTE, 0);
            nextDay.set(Calendar.SECOND, 0);
            nextDay.add(Calendar.DAY_OF_YEAR, 1);

            // Programar la alarma según la versión de Android.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    // Para las versiones más recientes, usar el método que permite alarmas exactas.
                    alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextDay.getTimeInMillis(),
                            pendingIntent
                    );
                }
            } else {
                // Para versiones anteriores, usar el método estándar para establecer la alarma exacta.
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextDay.getTimeInMillis(),
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            // Mostrar un mensaje de error si ocurre una excepción relacionada con la seguridad.
            Toast.makeText(context, context.getString(R.string.Erroralreprogramar) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}