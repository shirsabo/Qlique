package com.example.flightmobileapp;

import android.database.Cursor;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public final class Url_DAO_Impl implements Url_DAO {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfUrl_Entity;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfUrl_Entity;

  public Url_DAO_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUrl_Entity = new EntityInsertionAdapter<Url_Entity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `Url_Entity`(`url_location`,`URL`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Url_Entity value) {
        stmt.bindLong(1, value.getUrl_location());
        if (value.getUrl_string() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getUrl_string());
        }
      }
    };
    this.__deletionAdapterOfUrl_Entity = new EntityDeletionOrUpdateAdapter<Url_Entity>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `Url_Entity` WHERE `url_location` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, Url_Entity value) {
        stmt.bindLong(1, value.getUrl_location());
      }
    };
  }

  @Override
  public void saveUrl(Url_Entity url) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfUrl_Entity.insert(url);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteUrl(Url_Entity url) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfUrl_Entity.handle(url);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<Url_Entity> readUrl() {
    final 