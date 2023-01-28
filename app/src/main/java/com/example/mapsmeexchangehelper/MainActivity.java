package com.example.mapsmeexchangehelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String selectedTariff;
    String selectedCurrency;
    String transitionAmount;
    Boolean isFeeCheckbox;


    ArrayList spinnerTariffArrayList;
    Spinner spinnerTariff;
    ArrayAdapter spinnerTariffAdapter;

    ArrayList spinnerCurrencyArrayList;
    Spinner spinnerCurrency;
    ArrayAdapter spinnerCurrencyAdapter;

    JSONArray jCurrencyArray;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createTariffSpinner();

        handleCurrencyJson();
        createCurrencySpinner();
    }

    void createTariffSpinner() {
        spinnerTariff = findViewById(R.id.spinnerTariff);
        spinnerTariff.setOnItemSelectedListener(this);

        spinnerTariffArrayList = new ArrayList();
        spinnerTariffArrayList.add("Free (3%, 0.5$ FX Fee)");
        spinnerTariffArrayList.add("Happy Camper (2.5%, 0.4$ FX Fee)");
        spinnerTariffArrayList.add("Digital Nomad (2%, 0.25$ FX Fee)");
        spinnerTariffArrayList.add("High Flyer (1.5%, 0.1$ FX Fee)");

        spinnerTariffAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerTariffArrayList);
        spinnerTariffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTariff.setAdapter(spinnerTariffAdapter);
    }

    void createCurrencySpinner() {
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setOnItemSelectedListener(this);

        spinnerCurrencyArrayList = new ArrayList();
        try {
            for (int i = 0; i < jCurrencyArray.length(); i++) {
                JSONObject js3 = jCurrencyArray.getJSONObject(i);
                spinnerCurrencyArrayList.add(js3.getString("name"));
            }
        } catch (JSONException x) {
            Log.d("JSON Error", String.valueOf(x));
        }


        spinnerCurrencyAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerCurrencyArrayList);
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(spinnerCurrencyAdapter);
    }

    void handleCurrencyJson() {
        InputStream inputStream = this.getResources().openRawResource(R.raw.currency);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();


        try {
            JSONObject jObject = new JSONObject(jsonString);
            jCurrencyArray = jObject.getJSONArray("currency");
        } catch (JSONException e) {
            Log.d("JSON", "Faral Error " + e);
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedTariff = spinnerTariff.getSelectedItem().toString();
        selectedCurrency = spinnerCurrency.getSelectedItem().toString();
        Log.d("spinner", "tariff: " + selectedTariff);
        Log.d("spinner", "currency" + selectedTariff);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void calculate(View view) {
        EditText userNumber = findViewById(R.id.editTextNumber);
        transitionAmount = userNumber.getText().toString();


        final CheckBox internationalFeeCheckbox = (CheckBox) findViewById(R.id.internationalFeeCheckbox);
        final CheckBox ATMcheckbox = (CheckBox) findViewById(R.id.checkBoxAtm);


        Log.d("calculate",
                transitionAmount + " isFee: " + internationalFeeCheckbox.isChecked() + " ATM: " + ATMcheckbox.isChecked());


    }
}