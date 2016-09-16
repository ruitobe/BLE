package com.rui.ble.user.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Created by rhuang on 9/15/16.
 */
public class UserDatabaseAdapter {

    private static final String TAG = "UserDatabaseAdapter";
    static final String DATABASE_NAME = "user.db";
    static final int DATABASE_VERSION = 1;

    // SQL Statement to create a new database.
    public static final String DATABASE_CURRENT_TABLE = "current";
    public static final String DATABASE_SIGNEDUP_TABLE = "signedup";

    static final String DATABASE_CREATE_SIGNEDUP = "CREATE TABLE IF NOT EXISTS " + DATABASE_SIGNEDUP_TABLE +
            " (" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, " + "USERNAME text, EMAIL text, PASSWORD text);";

    static final String DATABASE_CREATE_CURRENT = "CREATE TABLE IF NOT EXISTS " + DATABASE_CURRENT_TABLE +
            " (" + "_id" + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ "USERNAME text, EMAIL text, PASSWORD text);";

    // Variable to hold the adapter instance
    private static UserDatabaseAdapter mUserDatabaseAdapter;

    // Variable to hold the database instance
    public SQLiteDatabase mDb;

    private Context mContext;

    private DatabaseHelper mDbHelper;

    public  UserDatabaseAdapter() {
        mUserDatabaseAdapter = this;
    }

    public UserDatabaseAdapter(Context context) {

        mContext = context;
        mDbHelper = new DatabaseHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public UserDatabaseAdapter open() throws SQLException {

        mDb = mDbHelper.getWritableDatabase();
        //Log.e(TAG, "mDb = " + mDb);
        mUserDatabaseAdapter = this;
        return this;
    }

    public static UserDatabaseAdapter getInstance() {

        if (mUserDatabaseAdapter == null) {
            mUserDatabaseAdapter = new UserDatabaseAdapter();
        }

        return mUserDatabaseAdapter;
    }

    public void close() {

        mDb.close();
    }

    public SQLiteDatabase getDatabaseInstance() {

        return mDb;
    }

    public boolean isTableExisted(String table) {

        Cursor cursor = null;
        boolean tableExisted = false;

        try {
            cursor = mDb.query(table, null, null, null, null, null, null);
            tableExisted = true;
        } catch (Exception e) {
            Log.d(TAG, "Table in database do not exist!");
        }
        return tableExisted;
    }

    public long getEntryCountInTable(String table) {

        return DatabaseUtils.queryNumEntries(mDb, table);
    }

    public void insertEntry(String table, String userName, String email, String password) {

        ContentValues newValues = new ContentValues();

        newValues.put("USERNAME", userName);
        newValues.put("EMAIL", email);
        newValues.put("PASSWORD", password);

        long rowId = mDb.insert(table, null, newValues);
        // Debug
        String savedName = getSingleEntryByName(table, userName);
        String savedPassword = getSingleEntryByPassword(table, password);
        Log.e(TAG, "insert rowId = " + rowId + "savedName = " + savedName + "savedPassword = " + savedPassword);
    }

    public int deleteEntry(String table, String userName) {

        String where = "USERNAME=?";
        int numberOfEntriesDeleted = mDb.delete(table, where, new String[]{userName});

        return numberOfEntriesDeleted;
    }

    public String getSingleEntryByName(String table, String name) {

        Cursor cursor = mDb.query(table, null, "USERNAME=?", new String[]{name}, null, null, null);


        if(cursor.getCount() < 1) {
            cursor.close();
            return "NO EXIST";
        }

        cursor.moveToFirst();
        String username = cursor.getString(cursor.getColumnIndex("USERNAME"));
        cursor.close();

        return username;
    }

    public String getSingleEntryByPassword(String table, String password) {

        Cursor cursor = mDb.query(table, null, "PASSWORD=?", new String[]{password}, null, null, null);

        if(cursor.getCount() < 1) {

            cursor.close();
            return "NO EXIST";
        }

        cursor.moveToFirst();
        String pw = cursor.getString(cursor.getColumnIndex("PASSWORD"));
        cursor.close();

        return pw;
    }


    public void updateEntry(String table, String userName, String email, String password) {

        ContentValues updatedValues = new ContentValues();

        updatedValues.put("USERNAME", userName);
        updatedValues.put("EMAIL", email);
        updatedValues.put("PASSWORD", password);

        String where = "USERNAME = ?";
        mDb.update(table, updatedValues, where, new String[]{userName});
    }

}
