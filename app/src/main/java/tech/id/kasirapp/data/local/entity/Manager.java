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

    public String firebaseId;

    public long branchId;

    public String name;

    public String username;

    public String password;

    public String phone;

    public int syncStatus;

    // 0 = belum sync

    // 1 = berhasil sync

    // 2 = gagal sync

}