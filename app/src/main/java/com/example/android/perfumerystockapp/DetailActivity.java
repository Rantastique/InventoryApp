package com.example.android.perfumerystockapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;
import com.example.android.perfumerystockapp.utils.Utils;

/**
 * Created by sr on 20.07.17.
 */

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_FRAGRANCE_LOADER = 0; // Id for the fragrance loader
    private Uri currentFragranceUri;                        // Content Uri for the existing fragrance
    public static final String LOG_TAG = DetailActivity.class.getSimpleName();

    // Variables for the fields that need to be updated with data
    private ImageView fragranceImageView;
    private TextView nameTextView;
    private TextView brandTextView;
    private TextView concentrationTextView;
    private TextView priceTextView;
    private TextView purchasePriceTextView;
    private TextView inStockTextView;
    private TextView supplierMailTextView;
    private TextView descriptionTextView;
    private Button decrementButton;
    private Button incrementButton;
    private Button sendMailButton;

    // Variables for the values extracted from the cursor
    private String name;
    private String brand;
    private int concentration;
    private int price;
    private int purchase_price;
    private int stockInfo;
    private String supplier_mail;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();            // get intent that was used to open the activity
        currentFragranceUri = intent.getData(); // get currentFragraceUri from intent

        // Initialize a loader to read the fragrance data from the database
        // and display the current values in the detail activity
        getSupportLoaderManager().initLoader(EXISTING_FRAGRANCE_LOADER, null, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_detail.xml file.
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Edit" menu option
            case R.id.action_edit:
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                intent.setData(currentFragranceUri);    // Set the URI on the data field of the intent
                startActivity(intent);                  // Launch EditorActivity and display the data for the current fragrance in it
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "up" button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(DetailActivity.this); // Navigates back to the OverviewActivity
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that contains all columns from the fragrance table
        String[] projection = {
                FragranceEntry._ID,
                FragranceEntry.COLUMN_FRAGRANCE_IMAGE,
                FragranceEntry.COLUMN_FRAGRANCE_NAME,
                FragranceEntry.COLUMN_FRAGRANCE_BRAND,
                FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION,
                FragranceEntry.COLUMN_FRAGRANCE_PRICE,
                FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE,
                FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO,
                FragranceEntry.COLUMN_SUPPLIER_MAIL,
                FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION,
        };

        // Return new loader that will execute the ContentProvider's query on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentFragranceUri,    // Query the content URI for the current fragrance
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Return early if the cursor is null or if there's less than one row in the fragrance table
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move to the first row of the cursor and read data from it
        if (cursor.moveToFirst()) {
            // Find the columns of the relevant fragrance attributes
            // (id, image, brand, concentration, price, purchase price, stock info)
            int idColumnIndex = cursor.getColumnIndex(FragranceEntry._ID);
            int imageColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_IMAGE);
            int nameColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_NAME);
            int brandColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_BRAND);
            int concentrationColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION);
            int priceColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_PRICE);
            int purchasePriceColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE);
            int stockInfoColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO);
            int supplierMailColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_SUPPLIER_MAIL);
            int descriptionColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION);

            // Get data from the field at the given ColumnIndex
            final int currentFragranceId = cursor.getInt(idColumnIndex);
            final String uriString = cursor.getString(imageColumnIndex);
            name = cursor.getString(nameColumnIndex);
            brand = cursor.getString(brandColumnIndex);
            concentration = cursor.getInt(concentrationColumnIndex);
            price = cursor.getInt(priceColumnIndex);
            purchase_price = cursor.getInt(purchasePriceColumnIndex);
            stockInfo = cursor.getInt(stockInfoColumnIndex);
            supplier_mail = cursor.getString(supplierMailColumnIndex);
            description = cursor.getString(descriptionColumnIndex);

            // Find the views that need to be updated with values from the database
            fragranceImageView = (ImageView) findViewById(R.id.detail_fragrance_image);
            nameTextView = (TextView) findViewById(R.id.detail_fragrance_name);
            brandTextView = (TextView) findViewById(R.id.detail_fragrance_brand);
            concentrationTextView = (TextView) findViewById(R.id.detail_fragrance_concentration);
            priceTextView = (TextView) findViewById(R.id.detail_fragrance_price);
            purchasePriceTextView = (TextView) findViewById(R.id.detail_fragrance_purchase_price);
            inStockTextView = (TextView) findViewById(R.id.detail_fragrance_in_stock);
            supplierMailTextView = (TextView) findViewById(R.id.detail_fragrance_supplier_mail);
            descriptionTextView = (TextView) findViewById(R.id.detail_fragrance_description);
            decrementButton = (Button) findViewById(R.id.detail_decrement_stock_info);
            incrementButton = (Button) findViewById(R.id.detail_increment_stock_info);
            sendMailButton = (Button) findViewById(R.id.send_mail);

            // Update the views on the screen with the values from the database

            // Set ViewTreeObserver to fragranceImageView
            // This method was recommended by a forum mentor in the thread below
            // https://discussions.udacity.com/t/how-to-pick-an-image-from-the-gallery/314971/2
            ViewTreeObserver viewTreeObserver = fragranceImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // If version is below JELLY_BEAN
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                        // Use removeGlobalOnLayoutListener
                        fragranceImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        // Use removeOnGlobalLayoutListener
                        fragranceImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                    // if there is an image URI available
                    if (!TextUtils.isEmpty(uriString)) {
                        // Set the bitmap to fragranceImageView
                        fragranceImageView.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(uriString), DetailActivity.this, fragranceImageView));
                    }
                    else {
                        fragranceImageView.setImageResource(R.drawable.fragrance_dark);
                    }
                }
            });

            nameTextView.setText(name);
            brandTextView.setText(brand);
            supplierMailTextView.setText(supplier_mail);
            descriptionTextView.setText(description);
            if (TextUtils.isEmpty(supplier_mail)) {
                supplierMailTextView.setText(R.string.empty_mail);
            } else {
                supplierMailTextView.setText(supplier_mail);
            }

            if (TextUtils.isEmpty(description)) {
                descriptionTextView.setText(R.string.empty_description);
            } else {
                descriptionTextView.setText(description);
            }

            // Sets the concentrationTextView's text to the according concentration value received from the cursor
            switch (concentration) {
                case FragranceEntry.CONCENTRATION_UNKNOWN:
                    concentrationTextView.setText(R.string.concentration_unknwon);
                    break;
                case FragranceEntry.PARFUM:
                    concentrationTextView.setText(R.string.parfum);
                    break;
                case FragranceEntry.EAU_DE_PARFUM:
                    concentrationTextView.setText(R.string.eau_de_parfum);
                    break;
                case FragranceEntry.EAU_DE_TOILETTE:
                    concentrationTextView.setText(R.string.eau_de_toilette);
                    break;
                case FragranceEntry.EAU_DE_COLOGNE:
                    concentrationTextView.setText(R.string.eau_de_cologne);
                    break;
                case FragranceEntry.OTHER:
                    concentrationTextView.setText(R.string.other);
                    break;
                default:
                    concentrationTextView.setText(R.string.concentration_unknwon);
                    break;
            }

            // If the price is zero, it wasn't setted
            if (price == 0) {
                // Update the displayed text and set an underline to it
                priceTextView.setText(R.string.click_to_set_price);
                priceTextView.setPaintFlags(priceTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                // Set onClickListener on TextView which navigates to the EditorActivity
                priceTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editIntent = new Intent(DetailActivity.this, EditorActivity.class);
                        editIntent.setData(currentFragranceUri);
                        startActivity(editIntent);
                    }
                });
            }
            else {
                priceTextView.setText(String.valueOf(price/100.00) + "€");
            }

            // If the purchase price is zero, it wasn't setted
            if (purchase_price == 0) {
                // Update the displayed text and set an underline to it
                purchasePriceTextView.setText(R.string.click_to_set_purchase_price);
                purchasePriceTextView.setPaintFlags(purchasePriceTextView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                // Set onClickListener on TextView which navigates to the EditorActivity
                purchasePriceTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editIntent = new Intent(DetailActivity.this, EditorActivity.class);
                        editIntent.setData(currentFragranceUri);
                        startActivity(editIntent);
                    }
                });
            }
            else {
                purchasePriceTextView.setText(String.valueOf(purchase_price/100.00 + "€"));
            }

            // If stockInfo is zero
            if (stockInfo == 0) {
                inStockTextView.setText(R.string.not_in_stock);                  // Update the displayed text
                decrementButton.setVisibility(View.GONE);                // Set decrementButton's visibility to GONE
            }
            else {
                inStockTextView.setText(String.valueOf(stockInfo));     // update the displayed text
                decrementButton.setVisibility(View.VISIBLE);            // set the decrementButton's visibility to VISIBLE
            }

            // Set OnClickListeners to the decrement and increment buttons to execute the according methods

            decrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri currentFragranceUri = ContentUris.withAppendedId(FragranceEntry.CONTENT_URI, currentFragranceId);
                    decrementInStock(DetailActivity.this, currentFragranceUri, stockInfo);
                }
            });

            incrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Uri currentFragranceUri = ContentUris.withAppendedId(FragranceEntry.CONTENT_URI, currentFragranceId);
                    incrementInStock(DetailActivity.this, currentFragranceUri, stockInfo);
                }
            });

            // Set an OnClickListener to the sendMailButton and call sendMailOrder to send an email intent
            sendMailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendMailOrder(supplier_mail, name);
                }
            });

            }
        }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the output fields
        fragranceImageView.setImageResource(R.drawable.fragrance_dark);
        nameTextView.setText("");
        brandTextView.setText("");
        concentrationTextView.setText("");
        priceTextView.setText("");
        purchasePriceTextView.setText("");
        supplierMailTextView.setText("");
        inStockTextView.setText("");
        descriptionTextView.setText("");
    }

    // Helper method that displays a dialog to warn the user about deleting the fragrance
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder, set the message and  set OnClickListeners on the dialog buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the messages and onClickListeners
        builder.setMessage(R.string.item_delete_dialog_msg);
        builder.setPositiveButton(R.string.item_delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked on the Delete button -> delete fragrance
                deleteFragrance();
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

    // Helper method to delete the current fragrance from the database
    private void deleteFragrance() {
        // Call the ContentResolver to delete the fragrance at the given URI
        int rowsDeleted = getContentResolver().delete(currentFragranceUri, null, null);
        // Show a toast message depending on whether the action was successful or not
        if (rowsDeleted == 0) {
            // If no rows were deleted, there was an error with deleting the fragrance
            Toast.makeText(this, R.string.item_not_deleted,
                    Toast.LENGTH_SHORT).show();
        } else {
            // Else, deleting the fragrance was successful
            Toast.makeText(this, R.string.item_deleted,
                    Toast.LENGTH_SHORT).show();
        }
        // Close the activity
        finish();
        }

    // Helper method to decrement inStock
    private void decrementInStock(Context context, Uri fragranceUri, int inStock) {
        if (inStock > 0) {
            inStock -= 1; // decrement inStock if it's initial value is > 0
        }
        // Since the decrement button disappears if inStock == 0, there's no further option to decrement inStock

        // Update table with new stock of the product
        ContentValues contentValues = new ContentValues();
        contentValues.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, inStock);
        int numRowsUpdated = context.getContentResolver().update(fragranceUri, contentValues, null, null);
    }

    // Helper method to increment inStock
    private void incrementInStock(Context context, Uri fragranceUri, int inStock) {
        // decrement InStock
        inStock += 1;
        // Update table with new stock of the product
        ContentValues contentValues = new ContentValues();
        contentValues.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, inStock);
        int numRowsUpdated = context.getContentResolver().update(fragranceUri, contentValues, null, null);
    }

    // Helper method to define a mailIntent
    public void sendMailOrder (String supplierMailAddress, String orderSubject) {
        Intent mailIntent = new Intent(Intent.ACTION_SENDTO);
        mailIntent.setData(Uri.parse("mailto:")); // This should be handled by an email app
        mailIntent.putExtra(Intent.EXTRA_EMAIL, supplierMailAddress);
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, orderSubject);
        if (mailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mailIntent);
        }
    }
}


