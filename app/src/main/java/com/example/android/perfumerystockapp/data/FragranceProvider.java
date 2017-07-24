package com.example.android.perfumerystockapp.data;

/**
 * Created by sr on 20.07.17.
 */

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.perfumerystockapp.R;
import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;

public class FragranceProvider extends ContentProvider {

    public static final String LOG_TAG = FragranceProvider.class.getSimpleName();

    // URI matcher code for the content URI for the whole fragrance table
    private static final int FRAGRANCES = 100;

    // URI matcher code for the content URI for a single fragrance
    private static final int FRAGRANCE_ID = 101;

    // URI matcher object that matches a content URI to a corresponding code
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // URI used to provide access to mutliple rows from the fragrance table
        sUriMatcher.addURI(FragranceContract.CONTENT_AUTHORITY, FragranceContract.PATH_FRAGRANCES, FRAGRANCES);

        // URI used to provide access to a single fragrance in the fragrance table
        sUriMatcher.addURI(FragranceContract.CONTENT_AUTHORITY, FragranceContract.PATH_FRAGRANCES + "/#", FRAGRANCE_ID);
    }

    // Database helper to perform methods on the database
    private FragranceDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new FragranceDbHelper(getContext());     // Initialise new dbHelper
        return true;
    }


    // Method to perform a query on the database
    // Returns a cursor with the table columns and rows the user asked for
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();       // Get readable database
        Cursor cursor;                                                  // This cursor will hold the result of the query

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case FRAGRANCES:
                // For the FRAGRANCES code, query the frarances table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the fragrances table.
                cursor = database.query(FragranceEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case FRAGRANCE_ID:
                // For the FRAGRANCE_ID code, extract the ID from the URI and perform a query on the respective fragrance row
                selection = FragranceEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                cursor = database.query(FragranceEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(R.string.matcher_iae + uri.toString());
        }

        // Notifies the ContentResolver if the there's a change in the given URI
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    // Method that calls the insert Helper method
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FRAGRANCES:
                return insertFragrance(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    // Method that calls the update Helper method and passes in the according values
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FRAGRANCES:
                return updateFragrance(uri, contentValues, selection, selectionArgs);
            case FRAGRANCE_ID:
                selection = FragranceEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateFragrance(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    // Method that calls the delete method and passes in the according values
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FRAGRANCES:
                rowsDeleted = database.delete(FragranceEntry.TABLE_NAME, selection, selectionArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                // Delete all rows that match the selection and selection args
                return rowsDeleted;
            case FRAGRANCE_ID:
                // Delete a single row given by the ID in the URI
                selection = FragranceEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(FragranceEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);}
                // Return the number of rows deleted
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    // Method to get Type of URI
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case FRAGRANCES:
                return FragranceEntry.CONTENT_LIST_TYPE;
            case FRAGRANCE_ID:
                return FragranceEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    // Helper method to insert a fragrance into the database with the given content values
    private Uri insertFragrance(Uri uri, ContentValues values) {
        // Check that the name is not null
        String name = values.getAsString(FragranceEntry.COLUMN_FRAGRANCE_NAME);
        if (name == null) {
            throw new IllegalArgumentException(R.string.name_iae + name);
        }

        // Check that the brand is not null
        String brand = values.getAsString(FragranceEntry.COLUMN_FRAGRANCE_BRAND);
        if (brand == null) {
            throw new IllegalArgumentException(R.string.brand_iae + brand);
        }

        // If there is a concentration provided, check if it's a valid concentration
        // (if no concentration is provided, the default value of 0 (= UNKNOWN) is set automatically
        // -> see FragranceDbHelper)
        Integer concentration = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION);
        if (concentration != null && !FragranceEntry.isValidConcentration(concentration)) {
            throw new IllegalArgumentException(R.string.concentration_iae + concentration.toString());
        }

        // If there is a price provided, check if it's a valid price (greater than or equal to 0)
        Integer price = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException(R.string.price_iae + price.toString());
        }

        // If there's a purchase price provided, check if it's a valid purchase price
        // If there is a price provided, check if it's a valid price (greater than or equal to 0)
        Integer purchase_price = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE);
        if (purchase_price != null && purchase_price < 0) {
            throw new IllegalArgumentException(R.string.purchase_price_iae + purchase_price.toString());
        }

        // If there is a stock_info provided, check if it's greater than or equal to 0
        // (if the stock_info is not provided/is null, the default value of 0 is set automatically
        // -> see FragranceDbHelper)
        Integer stock_info = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO);
        if (stock_info != null && stock_info < 0) {
            throw new IllegalArgumentException(R.string.stock_info_iae + stock_info.toString());
        }

        // No need to check the image, suppler mail and description because those can have any value or none

        SQLiteDatabase database = dbHelper.getWritableDatabase();           // Get writeable database

        // Insert the new fragrance with the given values
        long id = database.insert(FragranceEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    // Helper method to update a single fragrance
    private int updateFragrance(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the COLUMN_FRAGRANCE_NAME key is present, check that the name is not null
        // Do those checks on all non-nullable values
        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_NAME)) {
            String name = values.getAsString(FragranceEntry.COLUMN_FRAGRANCE_NAME);
            if (name == null) {
                throw new IllegalArgumentException(R.string.name_iae + name);
            }
        }

        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_BRAND)) {
            String brand = values.getAsString(FragranceEntry.COLUMN_FRAGRANCE_BRAND);
            if (brand == null) {
                throw new IllegalArgumentException(R.string.brand_iae + brand);
            }
        }

        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION)) {
            Integer concentration = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION);
            if (concentration != null && !FragranceEntry.isValidConcentration(concentration)) {
                throw new IllegalArgumentException(R.string.concentration_iae + concentration.toString());
            }
        }

        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_PRICE)) {
            Integer price = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException(R.string.price_iae + price.toString());
            }
        }

        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE)) {
            Integer purchase_price = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE);
            if (purchase_price != null && purchase_price < 0) {
                throw new IllegalArgumentException(R.string.purchase_price_iae + purchase_price.toString());
            }
        }

        if (values.containsKey(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO)) {
            Integer stock_info = values.getAsInteger(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO);
            if (stock_info != null && stock_info < 0) {
                throw new IllegalArgumentException(R.string.stock_info_iae + stock_info.toString());
            }
        }

        // No need to check the image, supplier mail and description because those can have any value or none

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(FragranceEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }
}