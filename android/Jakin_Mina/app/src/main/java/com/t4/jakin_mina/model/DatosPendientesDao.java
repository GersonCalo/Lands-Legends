package com.t4.jakin_mina.model;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

// DatosPendientesDao.java
@Dao
public interface DatosPendientesDao {
    @Insert
    void insert(DatosPendientes datos);

    @Query("SELECT * FROM datos_pendientes")
    List<DatosPendientes> getAll();

    @Delete
    void delete(DatosPendientes datos);
}
