package tech.id.kasirapp.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


import androidx.room.ColumnInfo;

import androidx.room.Entity;

import androidx.room.PrimaryKey;

@Entity(tableName = "managers")
public class Manager {

    @PrimaryKey(autoGenerate = true)
    public long id;

    // ID dokumen Firebase
    public String firebaseId;

    // ID Firebase cabang
    public String branchFirebaseId;

    public long branchId;

    public String name;

    public String username;

    public String password;

    public String phone;

    public int syncStatus;
}