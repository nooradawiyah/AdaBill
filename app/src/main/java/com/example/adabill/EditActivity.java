package com.example.adabill;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.DecimalFormat;

public class EditActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnit;
    private SeekBar seekBarRebate;
    private TextView textViewRebateValue, textViewTotalCharges, textViewFinalCost;
    private Button buttonUpdate, buttonCancel;
    private DatabaseHelper dbHelper;
    private int billId;
    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};
    private DecimalFormat df = new DecimalFormat("0.00");
    private int unit, rebatePercent;
    private double totalCharges, finalCost;
    private String selectedMonth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initialize views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnit = findViewById(R.id.editTextUnit);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        textViewRebateValue = findViewById(R.id.textViewRebateValue);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonCancel = findViewById(R.id.buttonCancel);

        dbHelper = new DatabaseHelper(this);
        billId = getIntent().getIntExtra("BILL_ID", -1);

        if (billId == -1) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Load existing data
        loadData();

        // Spinner listener
        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = months[position];
                calculateBill();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedMonth = months[0];
            }
        });

        // SeekBar listener
        seekBarRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rebatePercent = progress;
                textViewRebateValue.setText("Rebate: " + rebatePercent + "%");
                calculateBill();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Update button
        buttonUpdate.setOnClickListener(v -> updateData());

        // Cancel button
        buttonCancel.setOnClickListener(v -> finish());
    }

    private void loadData() {
        Cursor cursor = dbHelper.getBillById(billId);
        if (cursor.moveToFirst()) {
            selectedMonth = cursor.getString(1);
            unit = cursor.getInt(2);
            totalCharges = cursor.getDouble(3);
            rebatePercent = cursor.getInt(4);
            finalCost = cursor.getDouble(5);

            // Set values to UI
            int monthPosition = getMonthPosition(selectedMonth);
            spinnerMonth.setSelection(monthPosition);
            editTextUnit.setText(String.valueOf(unit));
            seekBarRebate.setProgress(rebatePercent);
            textViewRebateValue.setText("Rebate: " + rebatePercent + "%");
            textViewTotalCharges.setText("Total Charges: RM" + df.format(totalCharges));
            textViewFinalCost.setText("Final Cost: RM" + df.format(finalCost));
        }
        cursor.close();
    }

    private int getMonthPosition(String month) {
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) return i;
        }
        return 0;
    }

    private void calculateBill() {
        String unitStr = editTextUnit.getText().toString().trim();
        if (unitStr.isEmpty()) return;

        unit = Integer.parseInt(unitStr);
        if (unit < 1 || unit > 1000) return;

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebatePercent / 100);

        textViewTotalCharges.setText("Total Charges: RM" + df.format(totalCharges));
        textViewFinalCost.setText("Final Cost: RM" + df.format(finalCost));
    }

    private double calculateTotalCharges(int unit) {
        double rate1 = 0.218, rate2 = 0.334, rate3 = 0.516, rate4 = 0.546;
        if (unit <= 200) {
            return unit * rate1;
        } else if (unit <= 300) {
            return 200 * rate1 + (unit - 200) * rate2;
        } else if (unit <= 600) {
            return 200 * rate1 + 100 * rate2 + (unit - 300) * rate3;
        } else {
            return 200 * rate1 + 100 * rate2 + 300 * rate3 + (unit - 600) * rate4;
        }
    }

    private void updateData() {
        String unitStr = editTextUnit.getText().toString().trim();
        if (unitStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter electricity usage", Toast.LENGTH_SHORT).show();
            return;
        }

        unit = Integer.parseInt(unitStr);
        if (unit < 1 || unit > 1000) {
            Toast.makeText(this, "⚠️ Unit must be between 1-1000 kWh", Toast.LENGTH_SHORT).show();
            return;
        }

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebatePercent / 100);

        dbHelper.updateBill(billId, selectedMonth, unit, totalCharges, rebatePercent, finalCost);
        Toast.makeText(this, "✅ Data updated successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }
}