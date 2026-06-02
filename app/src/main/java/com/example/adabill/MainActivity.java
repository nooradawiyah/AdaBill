package com.example.adabill;

import android.content.Intent;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    private Spinner spinnerMonth;
    private EditText editTextUnit;
    private SeekBar seekBarRebate;
    private TextView textViewRebateValue, textViewTotalCharges, textViewFinalCost;
    private Button buttonCalculate, buttonSave, buttonViewRecords, buttonAbout;
    private DatabaseHelper dbHelper;

    private String selectedMonth;
    private int unit, rebatePercent;
    private double totalCharges, finalCost;
    private DecimalFormat df = new DecimalFormat("0.00");

    private String[] months = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        spinnerMonth = findViewById(R.id.spinnerMonth);
        editTextUnit = findViewById(R.id.editTextUnit);
        seekBarRebate = findViewById(R.id.seekBarRebate);
        textViewRebateValue = findViewById(R.id.textViewRebateValue);
        textViewTotalCharges = findViewById(R.id.textViewTotalCharges);
        textViewFinalCost = findViewById(R.id.textViewFinalCost);
        buttonCalculate = findViewById(R.id.buttonCalculate);
        buttonSave = findViewById(R.id.buttonSave);
        buttonViewRecords = findViewById(R.id.buttonViewRecords);
        buttonAbout = findViewById(R.id.buttonAbout);

        // Setup Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        spinnerMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                selectedMonth = months[position];
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
                if (editTextUnit.getText().toString().trim().length() > 0) {
                    calculateBill();
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Calculate button
        buttonCalculate.setOnClickListener(v -> calculateBill());

        // Save button
        buttonSave.setOnClickListener(v -> saveToDatabase());

        // View Records button
        buttonViewRecords.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ListActivity.class);
            startActivity(intent);
        });

        // About button
        buttonAbout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });
    }

    private void calculateBill() {
        String unitStr = editTextUnit.getText().toString().trim();

        if (unitStr.isEmpty()) {
            Toast.makeText(this, "⚠️ Please enter electricity usage (kWh)", Toast.LENGTH_SHORT).show();
            return;
        }

        unit = Integer.parseInt(unitStr);

        if (unit < 1 || unit > 1000) {
            Toast.makeText(this, "⚠️ Unit must be between 1 and 1000 kWh", Toast.LENGTH_SHORT).show();
            return;
        }

        totalCharges = calculateTotalCharges(unit);
        finalCost = totalCharges - (totalCharges * rebatePercent / 100);

        textViewTotalCharges.setText("Total Charges: RM" + df.format(totalCharges));
        textViewFinalCost.setText("Final Cost: RM" + df.format(finalCost));

        Toast.makeText(this, "✅ Calculation completed!", Toast.LENGTH_SHORT).show();
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

    private void saveToDatabase() {
        if (totalCharges == 0 && finalCost == 0) {
            Toast.makeText(this, "⚠️ Please calculate first before saving", Toast.LENGTH_SHORT).show();
            return;
        }

        if (unit < 1 || unit > 1000) {
            Toast.makeText(this, "⚠️ Invalid unit value", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertBill(selectedMonth, unit, totalCharges, rebatePercent, finalCost);

        if (result != -1) {
            Toast.makeText(this, "✅ Data saved successfully!", Toast.LENGTH_SHORT).show();
            editTextUnit.setText("");
            seekBarRebate.setProgress(0);
            textViewTotalCharges.setText("Total Charges: RM0.00");
            textViewFinalCost.setText("Final Cost: RM0.00");
            totalCharges = 0;
            finalCost = 0;
        } else {
            Toast.makeText(this, "❌ Failed to save data", Toast.LENGTH_SHORT).show();
        }
    }
}