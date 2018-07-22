package com.example.android.inventoryapp1.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * {@link ContentProvider} for Pets app.
 */
public class ProductProvider extends ContentProvider {

    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = ProductProvider.class.getSimpleName();
    /**
     * URI matcher code for the content URI for the products table
     */
    private static final int PRODUCTS = 100;
    /**
     * URI matcher code for the content URI for a single product in the products table
     */
    private static final int PRODUCT_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    /**
     * Database helper that will provide us access to the database
     */
    private ProductDbHelper mDbHelper;

    /**
     * Initialize the provider and the database helper object.
     */
    @Override
    public boolean onCreate() {

        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new ProductDbHelper(getContext());

        // Make sure the variable is a global variable, so it can be referenced from other
        // ContentProvider methods.
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // For the PRODUCTS code, query the products table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the products table.
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryapp1/products/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // This will perform a query on the products table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(ProductContract.ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a product into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertProduct(Uri uri, ContentValues values) {

        // Validations START

        // Check that the name is not null
        String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        if (name.isEmpty()) {
            Log.e(LOG_TAG, "Empty name");
            throw new IllegalArgumentException("Product requires a name");
        }

        // Check that the category is valid
        Integer category = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_CATEGORY);
        if (category == null || !ProductContract.ProductEntry.isValidCategory(category)) {
            throw new IllegalArgumentException("Product requires valid category");
        }

        // Check that the price is not negative
        Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        if (price != null && price < 0) {
            Log.e(LOG_TAG, "Price negative");
            throw new IllegalArgumentException("Product requires a valid price (0+)");
        }

        // Check that the quantity is not negative
        Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity != null && quantity < 0) {
            Log.e(LOG_TAG, "Quantity negative");
            throw new IllegalArgumentException("Product requires a valid quantity (0+)");
        }

        // Check that the supplier name is not null
        String supplier = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier.isEmpty()) {
            Log.e(LOG_TAG, "Supplier empty");
            throw new IllegalArgumentException("Product requires a supplier name");
        }

        // Check that the phone is not null
        String phone = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
        if (phone.isEmpty()) {
            Log.e(LOG_TAG, "Phone empty");
            throw new IllegalArgumentException("Product requires a supplier phone");
        }

        // Validations END

        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that the data has changed for the product content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    /**
     * Updates the data at the given selection and selection arguments, with the new ContentValues.
     */
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more products).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Validations START

        // If the {@link ProductEntry#COLUMN_PRODUCT_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            if (name.isEmpty()) {
                Log.e(LOG_TAG, "Empty name");
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_CATEGORY} key is present,
        // Check that the category is valid
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_CATEGORY)) {
            Integer category = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_CATEGORY);
            if (category == null || !ProductContract.ProductEntry.isValidCategory(category)) {
                throw new IllegalArgumentException("Product requires valid category");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_PRICE} key is present,
        // Check that the price is not negative
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            if (price != null && price < 0) {
                Log.e(LOG_TAG, "Price negative");
                throw new IllegalArgumentException("Product requires a valid price (0+)");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_QUANTITY} key is present,
        // Check that the quantity is not negative
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                Log.e(LOG_TAG, "Quantity negative");
                throw new IllegalArgumentException("Product requires a valid quantity (0+)");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER} key is present,
        // Check that the supplier name is not null
        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplier = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier.isEmpty()) {
                Log.e(LOG_TAG, "Supplier empty");
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        // If the {@link ProductEntry#COLUMN_PRODUCT_SUPPLIER_PHONE} key is present,
        // Check that the phone is not null

        if (values.containsKey(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE)) {
            String phone = values.getAsString(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER_PHONE);
            if (phone.isEmpty()) {
                Log.e(LOG_TAG, "Phone empty");
                throw new IllegalArgumentException("Product requires a supplier phone");
            }
        }

        // Validations END

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ProductContract.ProductEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows updated
        return rowsUpdated;
    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                // Delete a single row given by the ID in the URI
                selection = ProductContract.ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductContract.ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }


    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductContract.ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductContract.ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

}
