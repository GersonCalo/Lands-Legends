/**
 * Actividad principal de la aplicación. Maneja:
 * - Configuración inicial de la UI con Edge-to-Edge
 * - Solicitud de permisos para notificaciones y alarmas exactas
 * - Programación de recordatorios diarios a las 10:00 AM
 */
package com.t4.jakin_mina.view;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.t4.jakin_mina.DailyReminderReceiver;
import com.t4.jakin_mina.R;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static final int EXACT_ALARM_PERMISSION_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Habilitar diseño edge-to-edge
        setContentView(R.layout.activity_main);

        // Ajustar padding para evitar superposición con barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scheduleDailyReminder();  // Programar alarma recurrente
        checkNotificationPermission(); // Verificar permiso notificaciones
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkExactAlarmPermission(); // Revisar permiso alarmas exactas al volver a la app
    }

    /** Verifica si tiene permiso para alarmas exactas (requerido en Android 12+) */
    private void checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, R.string.Permisonecesarioparaalarmasexactas, Toast.LENGTH_LONG).show();
                requestExactAlarmPermission(); // Lanzar intent para configuración del sistema
            }
        }
    }

    /** Solicita permiso para alarmas exactas mediante pantalla de configuración */
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void requestExactAlarmPermission() {
        startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
    }

    /** Verifica/Solicita permiso para notificaciones (requerido en Android 13+) */
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    1001
            );
        }
    }

    /** Programa alarma diaria no repetitiva a las 10:00 AM */
    private void scheduleDailyReminder() {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this, 0,
                    new Intent(this, DailyReminderReceiver.class),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 10); // 10 AM
            calendar.set(Calendar.MINUTE, 0);
            // Si ya pasó la hora, programar para mañana
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Configurar alarma según versión Android
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        } catch (SecurityException e) {
            Toast.makeText(this, getString(R.string.Errordepermisos) + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Maneja respuesta de solicitud de permisos */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scheduleDailyReminder(); // Reprogramar si se concedió permiso
        }
    }
}