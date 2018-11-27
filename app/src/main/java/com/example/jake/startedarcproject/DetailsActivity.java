package com.example.jake.startedarcproject;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity {

    ListView myListView;

    String[] a = {"a", "b", "c"};
    String[] c = {"1", "2", "3"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        myListView = (ListView) findViewById(R.id.myListView);

        ArrayList<String> fields = (ArrayList<String>) getIntent().getSerializableExtra("ITEM_INDEX");
        ArrayList<String> values = (ArrayList<String>) getIntent().getSerializableExtra("VALUE_INDEX");

        //int[] array = new int[list.size()];
        //for(int i = 0; i < list.size(); i++) array[i] = list.get(i);

        String[] fieldsArray = new String[fields.size()];
        String[] valuesArray = new String[values.size()];

        String[] CustomFields = getResources().getStringArray(R.array.Fields);

        for(int i = 0; i < fields.size(); i++) {
            fieldsArray[i] = fields.get(i);
            valuesArray[i] = values.get(i);
        }


        ItemAdapter itemAdapter = new ItemAdapter(this, CustomFields, valuesArray);
        myListView.setAdapter(itemAdapter);

    }
}
