package com.example.android.perfumerystockapp.data;

/**
 * Created by sr on 20.07.17.
 */


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;

// Database helper for the PerfumeryStockApp
// Manages database creation and version management
public class FragranceDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = FragranceDbHelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "perfumery_stock.db";

    // Database version
    // If the schema is changed, the version number must be incremented
    private static final int DATABASE_VERSION = 1;

    // Constructor to create a new instance of FragranceDbHelper
    public FragranceDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // This method creates the fragrance table for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the fragrance table
        String SQL_CREATE_FRAGRANCE_TABLE =
                "CREATE TABLE " + FragranceEntry.TABLE_NAME + " ("
                + FragranceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FragranceEntry.COLUMN_FRAGRANCE_IMAGE + " TEXT, "
                + FragranceEntry.COLUMN_FRAGRANCE_NAME + " TEXT NOT NULL, "
                + FragranceEntry.COLUMN_FRAGRANCE_BRAND + " TEXT NOT NULL, "
                + FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION + " INTEGER NOT NULL DEFAULT 0, "
                + FragranceEntry.COLUMN_FRAGRANCE_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE + " INTEGER NOT NULL DEFAULT 0, "
                + FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO + " INTEGER NOT NULL DEFAULT 0, "
                + FragranceEntry.COLUMN_SUPPLIER_MAIL + " TEXT, "
                + FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION + " TEXT);";
        // Execute the SQL statement created above
        db.execSQL(SQL_CREATE_FRAGRANCE_TABLE);
    }

    // This method is called when the database should be upgraded
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the current table on update
        db.execSQL("DROP TABLE IF EXISTS " + FragranceEntry.TABLE_NAME);
        // Create a new table
        onCreate(db);
    }
}