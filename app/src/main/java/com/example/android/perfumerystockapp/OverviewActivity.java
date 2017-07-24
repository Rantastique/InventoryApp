package com.example.android.perfumerystockapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class OverviewActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int FRAGRANCE_LOADER = 0;      // Identifier for the fragrance loader
    FragranceCursorAdapter cursorAdapter;               // Adapter for the ListView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OverviewActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView to populate the fragrance data with
        ListView fragranceListView = (ListView) findViewById(R.id.list);

        // Find and set empty state view for the list
        View emptyView = findViewById(R.id.empty_view);
        fragranceListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no fragrance data yet (until the loader finishes) so pass in null for the Cursor.
        cursorAdapter = new FragranceCursorAdapter(this, null);
        fragranceListView.setAdapter(cursorAdapter);

        // Setup the item click listener
        fragranceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to DetailActivity
                Intent intent = new Intent(OverviewActivity.this, DetailActivity.class);
                // Form the content URI for the fragrance that was clicked on
                Uri currentFragranceUri = ContentUris.withAppendedId(FragranceEntry.CONTENT_URI, id);
                // Set the URI on the data field of the intent
                intent.setData(currentFragranceUri);

                // Launch the DetailActivity to display the data for the current fragrance
                Log.v("Overview", "Intent");
                startActivity(intent);
            }
        });

        // Kick off the loader
        getSupportLoaderManager().initLoader(FRAGRANCE_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertFragrance();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                FragranceEntry._ID,
                FragranceEntry.COLUMN_FRAGRANCE_IMAGE,
                FragranceEntry.COLUMN_FRAGRANCE_NAME,
                FragranceEntry.COLUMN_FRAGRANCE_BRAND,
                FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION,
                FragranceEntry.COLUMN_FRAGRANCE_PRICE,
                FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO,
                };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                FragranceEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the FragranceAdapter with this new cursor containing updated fragrance data
        cursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        cursorAdapter.swapCursor(null);
    }

    // Helper method to delete all fragrances from the database
    private void deleteAllFragrances() {
        int rowsDeleted = getContentResolver().delete(FragranceEntry.CONTENT_URI, null, null);
        Log.v("OverviewActivity", rowsDeleted + " rows deleted from fragrance database");
    }

    // Helper method to insert hard-coded  fragrance into the database, use for debugging and testing
    private void insertFragrance() {
        ContentValues values = new ContentValues();
        values.put(FragranceEntry.COLUMN_FRAGRANCE_NAME, "Eau de Dummy");
        values.put(FragranceEntry.COLUMN_FRAGRANCE_BRAND, "Imaginary Brand");
        values.put(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION, 3);
        values.put(FragranceEntry.COLUMN_FRAGRANCE_PRICE, 6500);
        values.put(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE, 5550);
        values.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, 15);
        values.put(FragranceEntry.COLUMN_SUPPLIER_MAIL, "example@example.com");
        values.put(FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION, "This is just a dummy fragrance");

        Uri newUri = getContentResolver().insert(FragranceEntry.CONTENT_URI, values);
    }

    // Helper method that displays a dialog to warn the user about deleting the fragrance
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder, set the message and  set OnClickListeners on the dialog buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the messages and onClickListeners
        builder.setMessage(R.string.table_delete_dialog_msg);
        builder.setPositiveButton(R.string.table_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked on the Delete button -> delete fragrance
                deleteAllFragrances();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked on the Cancel button -> dismiss the dialog and go back
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}