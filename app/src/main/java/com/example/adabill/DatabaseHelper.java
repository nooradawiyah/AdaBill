package com.example.adabill;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "electricity.db";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    public static final String TABLE_BILL = "bills";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_MONTH = "month";
    public static final String COLUMN_UNIT = "unit";
    public static final String COLUMN_TOTAL_CHARGES = "total_charges";
    public static final String COLUMN_REBATE_PERCENT = "rebate_percent";
    public static final String COLUMN_FINAL_COST = "final_cost";

    // Create table SQL
    private static final String CREATE_TABLE_BILL =
            "CREATE TABLE " + TABLE_BILL + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_MONTH + " TEXT NOT NULL, " +
                    COLUMN_UNIT + " INTEGER NOT NULL, " +
                    COLUMN_TOTAL_CHARGES + " REAL NOT NULL, " +
                    COLUMN_REBATE_PERCENT + " INTEGER NOT NULL, " +
                    COLUMN_FINAL_COST + " REAL NOT NULL" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BILL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILL);
        onCreate(db);
    }

    // INSERT data
    public long insertBill(String month, int unit, double totalCharges, int rebatePercent, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNIT, unit);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_REBATE_PERCENT, rebatePercent);
        values.put(COLUMN_FINAL_COST, finalCost);
        return db.insert(TABLE_BILL, null, values);
    }

    // GET ALL bills (for ListView)
    public Cursor getAllBills() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_ID + ", " + COLUMN_MONTH + ", " + COLUMN_FINAL_COST +
                " FROM " + TABLE_BILL + " ORDER BY " + COLUMN_ID + " DESC", null);
    }

    // GET single bill by ID
    public Cursor getBillById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BILL + " WHERE " + COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    // UPDATE bill
    public int updateBill(int id, String month, int unit, double totalCharges, int rebatePercent, double finalCost) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MONTH, month);
        values.put(COLUMN_UNIT, unit);
        values.put(COLUMN_TOTAL_CHARGES, totalCharges);
        values.put(COLUMN_REBATE_PERCENT, rebatePercent);
        values.put(COLUMN_FINAL_COST, finalCost);
        return db.update(TABLE_BILL, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // DELETE bill
    public int deleteBill(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_BILL, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}
