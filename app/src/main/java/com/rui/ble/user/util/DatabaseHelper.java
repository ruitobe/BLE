package com.rui.ble.user.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by rhuang on 9/15/16.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, name, factory, version);
    }

    // Called when no database exists in disk and the helper class needs
    // to create a new one.
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        // One database with 2 tables
        db.execSQL(UserDatabaseAdapter.DATABASE_CREATE_SIGNEDUP);
        db.execSQL(UserDatabaseAdapter.DATABASE_CREATE_CURRENT);

    }

    // Called when there is a database version mismatch meaning that the version
    // of the database on disk needs to be upgraded to the current version.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Log the version upgrade.
        Log.e(TAG, "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data.");

        // Upgrade the existing database to conform to the new version. Multiple
        // previous versions can be handled by comparing _oldVersion and _newVersion
        // values.
        // The simplest case is to drop the old table and create a new one.

        db.execSQL("DROP TABLE IF EXISTS " + UserDatabaseAdapter.DATABASE_SIGNEDUP_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + UserDatabaseAdapter.DATABASE_CURRENT_TABLE);
        // Create a new one.
        onCreate(db);
    }

}
