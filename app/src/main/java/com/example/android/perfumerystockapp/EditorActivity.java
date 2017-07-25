package com.example.android.perfumerystockapp;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;
import com.example.android.perfumerystockapp.utils.Utils;


/**
 * Created by sr on 20.07.17.
 */

public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_FRAGRANCE_LOADER = 0;                // Id for the fragrance loader
    private static final int PICK_IMAGE_REQUEST = 0;                       // Id for the pick image request
    private Uri currentFragranceUri;                                       // Content Uri for the existing fragrance (null if it's a new fragrance)

    // Variables for the input views
    private Uri imageUri = Uri.parse("");                                 // Initialise to prevent NullPointerException
    private EditText nameEditText;
    private EditText brandEditText;
    private Spinner concentrationSpinner;
    private EditText priceEditText;
    private EditText purchasePriceEditText;
    private EditText inStockEditText;
    private EditText supplierMailEditText;
    private EditText descriptionEditText;
    private ImageView fragranceImageView;
    private TextView imageTextView;

    // Variables for the user input
    private String uriString = imageUri.toString();                     // Initialise to prevent NullPointerException
    private String nameString;
    private String brandString;
    private String priceString;
    private String purchasePriceString;
    private String inStockString;
    private String supplierMailString;
    private String descriptionString;


    private String name;
    private String brand;
    private int concentrationInt = FragranceEntry.CONCENTRATION_UNKNOWN; // Initialise to default option
    private int price;
    private int purchase_price;
    private int stockInfo;
    private String supplier_mail;
    private String description;


    // Boolean that keeps track of whether input fields have been touched (true) or not (false)
    private boolean fragranceHasChanged = false;

    // OnTouchListener that listens for a touch on an input field
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            fragranceHasChanged = true;
            return false;
        }
    };

    // Boolean that keeps track of whether the ImageView has been touched (true) or not (false)
    private boolean imageHasChanged = false;
    // OnTouchListener that listens for a touch on the ImageView
    private View.OnTouchListener imageTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            imageHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();                // Get intent that was used to open the EditorActivity
        currentFragranceUri = intent.getData();     // Get the fragrance URI from the intent

        // If it is null, the Editor was opened to create a new fragrance
        if (currentFragranceUri == null) {
            supportInvalidateOptionsMenu();        // Invalidate options menu to hide the delete option
            setTitle(R.string.label_add);            // Change the activity label to "Add Fragrance"
            // Set the default image
            fragranceImageView = (ImageView) findViewById(R.id.edit_fragrance_image);
            fragranceImageView.setImageResource(R.drawable.fragrance_dark);
        } else {
            // Else, the Editor was opened to update a fragrance
            setTitle(R.string.label_edit);         // Change the activity label to "Edit Fragrance"

            // Initialize a loader to read the fragrance data from the database
            // and display the current values in the editor activity
            getSupportLoaderManager().initLoader(EXISTING_FRAGRANCE_LOADER, null, this);
        }

        // Find all relevant views to read user input from
        fragranceImageView = (ImageView) findViewById(R.id.edit_fragrance_image);
        nameEditText = (EditText) findViewById(R.id.edit_fragrance_name);
        brandEditText = (EditText) findViewById(R.id.edit_fragrance_brand);
        concentrationSpinner = (Spinner) findViewById(R.id.concentration_spinner);
        priceEditText = (EditText) findViewById(R.id.edit_fragrance_price);
        purchasePriceEditText = (EditText) findViewById(R.id.edit_fragrance_purchase_price);
        inStockEditText = (EditText) findViewById(R.id.edit_fragrance_in_stock);
        supplierMailEditText = (EditText) findViewById(R.id.edit_fragrance_supplier_mail);
        descriptionEditText = (EditText) findViewById(R.id.edit_fragrance_description);
        imageTextView = (TextView) findViewById(R.id.image_description);

        // Set OnTouchListeners on all input fields to determine whether they have been touched or not
        fragranceImageView.setOnTouchListener(imageTouchListener);
        nameEditText.setOnTouchListener(touchListener);
        brandEditText.setOnTouchListener(touchListener);
        concentrationSpinner.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        purchasePriceEditText.setOnTouchListener(touchListener);
        inStockEditText.setOnTouchListener(touchListener);
        supplierMailEditText.setOnTouchListener(touchListener);
        descriptionEditText.setOnTouchListener(touchListener);

        // Set OnClickListener to fragranceImageView to open gallery on click
        fragranceImageView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openImageSelector();
                    }
                });

        // Set up the concentrationSpinner
        setupSpinner();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                imageUri = resultData.getData();

                fragranceImageView.setImageBitmap(Utils.getBitmapFromUri(imageUri, this, fragranceImageView));
            }

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new fragrance, hide delete option in menu
        if (currentFragranceUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Insert fragrance into database
                saveFragrance();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Call onBackPressed()
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // This method is called when the back button is pressed
    @Override
    public void onBackPressed() {
        // If none of the input fields has been touched, continue handling the back button click
        if (!fragranceHasChanged && !imageHasChanged) {
            super.onBackPressed();
            return;
        }

       // Else, if there are unsaved changes, warn the user
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked discard button so continue handling the back button click
                        finish();
                    }
                };

        // Show dialog to warn user about unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
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
                currentFragranceUri,    // Query the content URI for the current pet
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
            final String uriString = cursor.getString(imageColumnIndex);
            name = cursor.getString(nameColumnIndex);
            brand = cursor.getString(brandColumnIndex);
            concentrationInt = cursor.getInt(concentrationColumnIndex);
            price = cursor.getInt(priceColumnIndex);
            purchase_price = cursor.getInt(purchasePriceColumnIndex);
            stockInfo = cursor.getInt(stockInfoColumnIndex);
            supplier_mail = cursor.getString(supplierMailColumnIndex);
            description = cursor.getString(descriptionColumnIndex);

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
                        fragranceImageView.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(uriString), EditorActivity.this, fragranceImageView));
                    }
                    else {
                        fragranceImageView.setImageResource(R.drawable.fragrance_dark);
                    }
                }
            });

            nameEditText.setText(name);
            brandEditText.setText(brand);
            priceEditText.setText(Integer.toString(price));
            purchasePriceEditText.setText(Integer.toString(purchase_price));
            inStockEditText.setText(Integer.toString(stockInfo));
            if (TextUtils.isEmpty(supplier_mail)) {
                supplierMailEditText.setText(R.string.empty_mail);
            } else {
                supplierMailEditText.setText(supplier_mail);

                if (TextUtils.isEmpty(description)) {
                    descriptionEditText.setText(R.string.empty_description);
                } else {
                    descriptionEditText.setText(description);
                }

                // Sets the concentrationTextView's text to concentration value received from the cursor
                switch (concentrationInt) {
                    case FragranceEntry.CONCENTRATION_UNKNOWN:
                        concentrationSpinner.setSelection(0);
                        break;
                    case FragranceEntry.PARFUM:
                        concentrationSpinner.setSelection(1);
                        break;
                    case FragranceEntry.EAU_DE_PARFUM:
                        concentrationSpinner.setSelection(2);
                        break;
                    case FragranceEntry.EAU_DE_TOILETTE:
                        concentrationSpinner.setSelection(3);
                        break;
                    case FragranceEntry.EAU_DE_COLOGNE:
                        concentrationSpinner.setSelection(4);
                        break;
                    case FragranceEntry.OTHER:
                        concentrationSpinner.setSelection(5);
                        break;
                    default: concentrationSpinner.setSelection(0);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the output fields
        fragranceImageView.setImageResource(R.drawable.fragrance_dark);
        nameEditText.setText("");
        brandEditText.setText("");
        concentrationSpinner.setSelection(0); // Select "Concentration unknown"
        priceEditText.setText("");
        purchasePriceEditText.setText("");
        inStockEditText.setText("");
        supplierMailEditText.setText("");
        descriptionEditText.setText("");
    }

    // Methods to define the activity the user came from and set ParentActivity accordingly
    // found at Stackoverflow, Link below
    // https://stackoverflow.com/questions/19184154/dynamically-set-parent-activity

    @Override
    public Intent getSupportParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    @Override
    public Intent getParentActivityIntent() {
        return getParentActivityIntentImpl();
    }

    private Intent getParentActivityIntentImpl() {
        Intent go_back;

        // Here you need to do some logic to determine from which Activity you came.
        // example: you could pass a variable through your Intent extras and check that.
        if (currentFragranceUri == null) {
            go_back = new Intent(EditorActivity.this, OverviewActivity.class);
        } else {
            go_back = new Intent(EditorActivity.this, DetailActivity.class);
            go_back.setData(currentFragranceUri);
        }
        return go_back;
    }

    // Helper method to get data from the input fields and insert them into the database
    private void saveFragrance() {

        // If all input fields are blank and the user hasn't touched any of the input fields
        // then there's no need to save a new and blank fragrance into the database
        if (currentFragranceUri == null &&
                !fragranceHasChanged &&
                !imageHasChanged &&
                TextUtils.isEmpty(uriString) &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(brandString) &&
                concentrationInt == FragranceEntry.CONCENTRATION_UNKNOWN &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(purchasePriceString) &&
                TextUtils.isEmpty(inStockString) &&
                TextUtils.isEmpty(supplierMailString) &&
                TextUtils.isEmpty(descriptionString)) {
            // No need to try to save the fragrance into the database, so return
            return;
        }

        if (checkInput()) {

            // Create a ContentValues object where column names are the keys and input data is the respective value
            ContentValues values = new ContentValues();
            // If the ImageView hasn't been touched, don't save the uriString because it would be empty anyway
            // (and would update the image URI String with an empty String)
            if (!imageHasChanged) {
                values.put(FragranceEntry.COLUMN_FRAGRANCE_NAME, nameString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_BRAND, brandString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION, concentrationInt);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_PRICE, priceString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE, purchasePriceString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, inStockString);
                values.put(FragranceEntry.COLUMN_SUPPLIER_MAIL, supplierMailString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION, descriptionString);
            } else {
                values.put(FragranceEntry.COLUMN_FRAGRANCE_IMAGE, uriString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_NAME, nameString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_BRAND, brandString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION, concentrationInt);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_PRICE, priceString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_PURCHASE_PRICE, purchasePriceString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, inStockString);
                values.put(FragranceEntry.COLUMN_SUPPLIER_MAIL, supplierMailString);
                values.put(FragranceEntry.COLUMN_FRAGRANCE_DESCRIPTION, descriptionString);
            }

            // If the currentFragranceUri is null, this is a new fragrance
            if (currentFragranceUri == null) {
                // Insert the new fragrance into the database and return the new content URI
                Uri newUri = getContentResolver().insert(FragranceEntry.CONTENT_URI, values);
                finish();

                // Show a toast message for whether or not saving the fragrance was successful
                if (newUri == null) {
                    // If the new URI is null, then the fragrance couldn't be inserted successfully
                    Toast.makeText(this, R.string.new_item_not_saved, Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, R.string.new_item_saved, Toast.LENGTH_SHORT).show();
                }
            } else {
                // This is an existing fragrance that should be updated
                int rowsAffected = getContentResolver().update(currentFragranceUri, values, null, null);
                finish();

                // Show a toast message for whether or not updating the fragrance was successful
                if (rowsAffected == 0) {
                    // If no rows were effected, the fragrance couldn't be updated
                    Toast.makeText(this, R.string.update_failed, Toast.LENGTH_SHORT).show();
                } else {
                    // Else, the update was successful
                    Toast.makeText(this, R.string.update_successful, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    // Helper method to set up the spinner that allows the user to select a fragrance concentration
    private void setupSpinner() {
        // Create adapter for spinner
        ArrayAdapter concentrationSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_concentration_options, android.R.layout.simple_spinner_item);

        concentrationSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);  // Specify dropdown layout style
        concentrationSpinner.setAdapter(concentrationSpinnerAdapter);                                      // set adapter to spinner

        // Set the texts for the according options
        concentrationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.parfum))) {
                        concentrationInt = FragranceEntry.PARFUM;
                    } else if (selection.equals(getString(R.string.eau_de_parfum))) {
                        concentrationInt = FragranceEntry.EAU_DE_PARFUM;
                    } else if (selection.equals(getString(R.string.eau_de_toilette))) {
                        concentrationInt = FragranceEntry.EAU_DE_TOILETTE;
                    } else if (selection.equals(getString(R.string.eau_de_cologne))) {
                        concentrationInt = FragranceEntry.EAU_DE_COLOGNE;
                    } else {
                        concentrationInt = FragranceEntry.CONCENTRATION_UNKNOWN;
                    }
                }
            }

            // Must be defined because this is an abstract class
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                concentrationInt = FragranceEntry.CONCENTRATION_UNKNOWN;
            }
        });
    }


    // Helper method to display a dialog that warns the user about unsaved changes
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder, set the message and  set OnClickListeners on the dialog buttons
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the messages and onClickListeners
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked on the Cancel button -> dismiss the dialog and go back to editing the fragrance
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
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
                // User clicked on the Cancel button -> dismiss the dialog and go back to editing the fragrance
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // Helper method to delete a fragrance
    private void deleteFragrance() {
        // Only perform this method if this is an existing fragrance
        if (currentFragranceUri != null) {
            // Call the ContentResolver to delete the fragrance at the given URI
            int rowsDeleted = getContentResolver().delete(currentFragranceUri, null, null);
            // Show a toast message depending for whether the action was successful or not
            if (rowsDeleted == 0) {
                // If no rows were deleted, there was an error with deleting the fragrance
                Toast.makeText(this, R.string.item_not_deleted, Toast.LENGTH_SHORT).show();
            } else {
                // Else, deleting the fragrance was successful
                Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
            }

            // Close the activity
            finish();
        }
    }

    // Helper method to open gallery and pick image from it
    // This method was recommended by a forum mentor in the thread below
    // https://discussions.udacity.com/t/how-to-pick-an-image-from-the-gallery/314971/2

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    public boolean checkInput() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        nameString = nameEditText.getText().toString().trim();
        brandString = brandEditText.getText().toString().trim();
        priceString = priceEditText.getText().toString().trim();
        purchasePriceString = purchasePriceEditText.getText().toString().trim();
        inStockString = inStockEditText.getText().toString().trim();
        supplierMailString = supplierMailEditText.getText().toString().trim();
        descriptionString = descriptionEditText.getText().toString().trim();


        if (imageUri == null || fragranceImageView.getDrawable() == null) {
            imageTextView.setText(R.string.add_image_error);
            return false;
        }
        else {
            uriString = imageUri.toString();
            imageTextView.setText(R.string.change_image);
        }


        if (TextUtils.isEmpty(nameString)) {
            nameEditText.requestFocus();
            nameEditText.setError(getString(R.string.name_error));
            return false;
        }

        if (TextUtils.isEmpty(brandString)) {
            brandEditText.requestFocus();
            brandEditText.setError(getString(R.string.brand_error));
            return false;
        }

        if (concentrationInt < 0 || concentrationInt > 5) {
            return false;
        }


        if (TextUtils.isEmpty(priceString) || Integer.valueOf(priceString) < 0) {
            priceEditText.requestFocus();
            priceEditText.setError(getString(R.string.price_error));
            return false;
        }

        if (TextUtils.isEmpty(purchasePriceString) || Integer.valueOf(purchasePriceString) < 0) {
            purchasePriceEditText.requestFocus();
            purchasePriceEditText.setError(getString(R.string.purchase_price_error));
            return false;
        }

        if (TextUtils.isEmpty(inStockString) || Integer.valueOf(inStockString) < 0) {
            inStockEditText.requestFocus();
            inStockEditText.setError(getString(R.string.in_stock_error));
            return false;
        }

        if ((!TextUtils.isEmpty(supplierMailString) && !supplierMailString.contains("@")) || (TextUtils.isEmpty(supplierMailString))) {
            supplierMailEditText.requestFocus();
            supplierMailEditText.setError(getString(R.string.supplier_mail_error));
            return false;
        }

        if (TextUtils.isEmpty(descriptionString)) {
            descriptionEditText.requestFocus();
            descriptionEditText.setError(getString(R.string.description_error));
            return false;
        }

        return true;
    }
}

