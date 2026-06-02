package com.example.adabill;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private Button buttonBack;
    private DatabaseHelper dbHelper;
    private ArrayList<Integer> idList;
    private ArrayList<String> displayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listViewRecords);
        buttonBack = findViewById(R.id.buttonBack);
        dbHelper = new DatabaseHelper(this);

        loadData();

        buttonBack.setOnClickListener(v -> finish());

        listView.setOnItemClickListener((parent, view, position, id) -> {
            int billId = idList.get(position);
            Intent intent = new Intent(ListActivity.this, DetailActivity.class);
            intent.putExtra("BILL_ID", billId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        idList = new ArrayList<>();
        displayList = new ArrayList<>();

        Cursor cursor = dbHelper.getAllBills();
        if (cursor.getCount() == 0) {
            displayList.add("📭 No records found. Please add data first.");
            Toast.makeText(this, "No records found. Please add data first.", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                String month = cursor.getString(1);
                double finalCost = cursor.getDouble(2);
                idList.add(id);
                displayList.add(month + "  →  RM" + String.format("%.2f", finalCost));
            }
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);
    }
}
