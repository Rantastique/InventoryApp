package com.example.android.perfumerystockapp;

/**
 * Created by sr on 20.07.17.
 */

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.perfumerystockapp.data.FragranceContract.FragranceEntry;
import com.example.android.perfumerystockapp.utils.Utils;

public class FragranceCursorAdapter extends CursorAdapter {

    // Contructor for the FragranceCursorAdapter
    public FragranceCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    // Inflate a new (blank) list item view.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.list_item, parent ,false);
        FragranceViewHolder fragranceHolder = new FragranceViewHolder(view);
        view.setTag(fragranceHolder);

        return view;
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */

    // Bind the fragrance data to to the given list item layout.
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        final FragranceViewHolder fragranceHolder = (FragranceViewHolder)view.getTag();
        final Context mContext = context;

        // Find the columns of the relevant fragrance attributes (name, brand, concentration, stock info)
        int idColumnIndex = cursor.getColumnIndex(FragranceEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_NAME);
        int brandColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_BRAND);
        int concentrationColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_CONCENTRATION);
        int priceColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_PRICE);
        int stockInfoColumnIndex = cursor.getColumnIndex(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO);

        // Get data from the field at the given ColumnIndex

        final int currentFragranceId = cursor.getInt(idColumnIndex);
        final String uriString = cursor.getString(imageColumnIndex);
        String name = cursor.getString(nameColumnIndex);
        String brand = cursor.getString(brandColumnIndex);
        int concentration = cursor.getInt(concentrationColumnIndex);
        int price = cursor.getInt(priceColumnIndex);
        final int stockInfo = cursor.getInt(stockInfoColumnIndex);

        // Set data to the respective TextViews

        // Set ViewTreeObserver to fragranceImageView
        // This method was recommended by a forum mentor in the thread below
        // https://discussions.udacity.com/t/how-to-pick-an-image-from-the-gallery/314971/2

        ViewTreeObserver viewTreeObserver = fragranceHolder.imageView.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    fragranceHolder.imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    fragranceHolder.imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if (TextUtils.isEmpty(uriString)) {
                    fragranceHolder.imageView.setImageResource(R.drawable.fragrance_dark);
                }
                else {
                    fragranceHolder.imageView.setImageBitmap(Utils.getBitmapFromUri(Uri.parse(uriString), mContext, fragranceHolder.imageView));
                }
            }
        });
        fragranceHolder.nameView.setText(name);
        fragranceHolder.brandView.setText(brand);
        fragranceHolder.concentrationView.setText(String.valueOf(concentration));
        switch (concentration) {
            case FragranceEntry.CONCENTRATION_UNKNOWN:
                fragranceHolder.concentrationView.setText(R.string.concentration_unknwon);
                break;
            case FragranceEntry.PARFUM:
                fragranceHolder.concentrationView.setText(R.string.parfum);
                break;
            case FragranceEntry.EAU_DE_PARFUM:
                fragranceHolder.concentrationView.setText(R.string.eau_de_parfum);
                break;
            case FragranceEntry.EAU_DE_TOILETTE:
                fragranceHolder.concentrationView.setText(R.string.eau_de_toilette);
                break;
            case FragranceEntry.EAU_DE_COLOGNE:
                fragranceHolder.concentrationView.setText(R.string.eau_de_cologne);
                break;
            case FragranceEntry.OTHER:
                fragranceHolder.concentrationView.setText(R.string.other);
                break;
            default:
                fragranceHolder.concentrationView.setText(R.string.concentration_unknwon);
                break;
        }

        fragranceHolder.inStockView.setText(String.valueOf(stockInfo));
        if (stockInfo == 0) {
            fragranceHolder.inStockInfo.setText(R.string.not_in_stock);
            fragranceHolder.inStockInfo.setTextColor(ContextCompat.getColor(context, R.color.colorError));
        }
        else {
            fragranceHolder.inStockInfo.setText(R.string.in_stock);
            fragranceHolder.inStockInfo.setTextColor(ContextCompat.getColor(context, R.color.colorInStock));
        }

        fragranceHolder.decrementInStockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentFragranceUri = ContentUris.withAppendedId(FragranceEntry.CONTENT_URI, currentFragranceId);
                decrementInStock(context, currentFragranceUri, stockInfo);
            }
        });

        fragranceHolder.priceView.setText(String.valueOf(price/100) + "€");
    }

    // Viewholder that holds all views that should be populated
    private static class FragranceViewHolder {

        ImageView imageView;
        TextView nameView;
        TextView brandView;
        TextView concentrationView;
        TextView priceView;
        TextView inStockView;
        TextView inStockInfo;
        Button decrementInStockButton;

        // Find the fields to populate in inflated template
        private FragranceViewHolder(View view) {
            imageView = (ImageView) view.findViewById(R.id.fragrance_image);
            nameView = (TextView) view.findViewById(R.id.fragrance_name);
            brandView = (TextView) view.findViewById(R.id.fragrance_brand);
            concentrationView = (TextView) view.findViewById(R.id.fragrance_concentration);
            priceView = (TextView) view.findViewById(R.id.fragrance_price);
            inStockView = (TextView) view.findViewById(R.id.fragrance_in_stock);
            inStockInfo = (TextView) view.findViewById(R.id.fragrance_in_stock_info);
            decrementInStockButton = (Button) view.findViewById(R.id.fragrance_decrement_in_stock_button);
        }
    }

    // Helper method to decrement inStock
    private void decrementInStock(Context context, Uri fragranceUri, int inStock) {
        // decrement inStock
        if (inStock > 0) {
            inStock -= 1;
        }
        else {
            inStock = 0;
        }
        // Update the inStock value
        ContentValues contentValues = new ContentValues();
        contentValues.put(FragranceEntry.COLUMN_FRAGRANCE_STOCK_INFO, inStock);
        int numRowsUpdated = context.getContentResolver().update(fragranceUri, contentValues, null, null);

        }
    }

