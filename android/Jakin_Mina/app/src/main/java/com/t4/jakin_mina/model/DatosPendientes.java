package com.t4.jakin_mina.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "datos_pendientes")
public class DatosPendientes {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String email;
    public String puntuacion;
    public String fechaHora;
}
