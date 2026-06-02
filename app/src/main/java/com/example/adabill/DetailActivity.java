package com.example.adabill;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView textViewMonth, textViewUnit, textViewTotalCharges, textViewRebate, textViewFinalCost;
    private Button buttonEdit, buttonDelete, buttonBack;
    private DatabaseHelper dbHelper;
    private int billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textViewMonth = findViewById(R.id.textViewMonth);
        textViewUnit = findViewById(R.id.textViewUnit);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewRebate = findViewById(R.id.textViewRebate);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBack = findViewById(R.id.buttonBack);

        dbHelper = new DatabaseHelper(this);
        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadData();

        buttonEdit.setOnClickListener(v -> {
            Intent intent = new Intent(DetailActivity.this, EditActivity.class);
            intent.putExtra("BILL_ID", billId);
            startActivity(intent);
        });

        buttonDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Record")
                    .setMessage("Are you sure you want to delete this record?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        dbHelper.deleteBill(billId);
                        Toast.makeText(this, "Record deleted successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        buttonBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        Cursor cursor = dbHelper.getBillById(billId);
        if (cursor.moveToFirst()) {
            String month = cursor.getString(1);
            int unit = cursor.getInt(2);
            double totalCharges = cursor.getDouble(3);
            int rebatePercent = cursor.getInt(4);
            double finalCost = cursor.getDouble(5);

            textViewMonth.setText(month);
            textViewUnit.setText(unit + " kWh");
            textViewTotalCharges.setText("RM" + String.format("%.2f", totalCharges));
            textViewRebate.setText(rebatePercent + "%");
            textViewFinalCost.setText("RM" + String.format("%.2f", finalCost));
        }
        cursor.close();
    }
}
