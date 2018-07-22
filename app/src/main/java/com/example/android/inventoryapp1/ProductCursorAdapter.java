package com.example.android.inventoryapp1;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp1.data.ProductContract;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of product data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ProductCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the product data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        TextView saleButton = (TextView) view.findViewById(R.id.sale_button);

        // Find the columns of product attributes that we're interested in
        final long id = cursor.getLong(cursor.getColumnIndexOrThrow(ProductContract.ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the product attributes from the Cursor for the current product
        final String productName = cursor.getString(nameColumnIndex);
        final String productPrice = cursor.getString(priceColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);

        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        priceTextView.setText(String.valueOf(productPrice));
        summaryTextView.setText(productQuantity);

        // Upon clicking on the Sale button, one product will be deducted from the inventory
        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int quantity = Integer.parseInt(productQuantity);
                if (quantity > 0) {
                    quantity = quantity - 1;
                } else {
                    Toast.makeText(context, R.string.sale_product_out_of_stock, Toast.LENGTH_SHORT).show();
                }
                String newQuantity = String.valueOf(quantity);
                ContentValues values = new ContentValues();
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, productName);
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, productPrice);
                values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);
                context.getContentResolver().update(ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id), values, null, null);
            }
        });



    }
}
