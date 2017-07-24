package com.example.android.perfumerystockapp.data;

/**
 * Created by sr on 20.07.17.
 */

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class FragranceContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private FragranceContract() { }
    public static final String CONTENT_AUTHORITY = "com.example.android.perfumerystockapp.data";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FRAGRANCES = "fragrances";

    // Inner class that defines constant values for the fragrance database table
    // each entry in the table represents a single fragrance
    public static final class FragranceEntry implements BaseColumns {

        // Content URI to access the fragrance data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FRAGRANCES);

        // MIME type of the content URI for a list of fragrances
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FRAGRANCES;

        // MIME type of the content URI for a single fragrance
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FRAGRANCES;

        // Name of the database for fragrances
        // Type: INTEGER
        public final static String TABLE_NAME = "fragrances";

        // Unique ID number for the fragrance
        // Type: INTEGER
        public final static String _ID = BaseColumns._ID;

        // Fragrance image in Bitmap
        // Type: TEXT
        public final static String COLUMN_FRAGRANCE_IMAGE = "image_bitmap";

        // Name of the fragrance
        // Type: TEXT
        public final static String COLUMN_FRAGRANCE_NAME ="name";

        // Brand of the fragrance
        // Type: TEXT
        public final static String COLUMN_FRAGRANCE_BRAND = "brand";

        // Concentration of the fragrance
        // possible values: UNKNOWN, PARFUM, EAU_DE_PARFUM, EAU DE TOILETTE, EAU DE COLOGNE, OTHER
        // Type: INTEGER
        public final static String COLUMN_FRAGRANCE_CONCENTRATION = "concentration";

        // Possible values for fragrance concentration
        public static final int CONCENTRATION_UNKNOWN = 0;
        public static final int PARFUM = 1;
        public static final int EAU_DE_PARFUM = 2;
        public static final int EAU_DE_TOILETTE = 3;
        public static final int EAU_DE_COLOGNE = 4;
        public static final int OTHER = 5;

        // Returns whether of not the given concentration is one of the valid values listed above
        public static boolean isValidConcentration(int concentration) {
            if (concentration == CONCENTRATION_UNKNOWN || concentration == PARFUM || concentration == EAU_DE_PARFUM ||
                    concentration == EAU_DE_TOILETTE || concentration == EAU_DE_COLOGNE ||
                    concentration == OTHER) {
                return true;
            }
            return false;
        }

        // Price the fragrance is sold at
        // Type: REAL
        public final static String COLUMN_FRAGRANCE_PRICE = "price";

        // Purchase price of the fragrance
        public final static String COLUMN_FRAGRANCE_PURCHASE_PRICE = "purchase_price";

        // Stock information for the fragrance -> how many bottles are in stock
        // Type: INTEGER
        public final static String COLUMN_FRAGRANCE_STOCK_INFO = "stock_info";

        // Supplier contact E-Mail
        public final static String COLUMN_SUPPLIER_MAIL = "supplier_mail";

        // Description of the fragrance
        // Type: TEXT
        public final static String COLUMN_FRAGRANCE_DESCRIPTION = "description";
    }

}
