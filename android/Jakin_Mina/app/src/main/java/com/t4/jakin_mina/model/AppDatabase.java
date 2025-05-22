package com.t4.jakin_mina.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {DatosPendientes.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract DatosPendientesDao datosPendientesDao();
}
