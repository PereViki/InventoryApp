/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.inventoryapp1.data;

import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 */
public final class ProductContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ProductContract() {}

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     * Each table has to implement BaseColumns.
     */
    public static final class ProductEntry implements BaseColumns {

        /** Name of database table for products */
        public final static String TABLE_NAME = "products";

        /**
         * Unique ID number for the product (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_NAME ="name";

        /**
         * Category of the product.
         *
         * The only possible values are {@link #CATEGORY_UNKNOWN}, {@link #CATEGORY_1},
         * or {@link #CATEGORY_2}.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_CATEGORY = "category";

        /**
         * Price of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";

        /**
         * Quantity of the product.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        /**
         * Supplier of the product.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier";

        /**
         * Phone number of the supplier.
         *
         * Type: TEXT
         */
        public final static String COLUMN_PRODUCT_SUPPLIER_PHONE = "phone";


        /**
         * Possible values for the category of the product.
         */
        public static final int CATEGORY_UNKNOWN = 0;
        public static final int CATEGORY_1 = 1;
        public static final int CATEGORY_2 = 2;
    }

}

