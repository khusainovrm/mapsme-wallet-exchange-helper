package com.example.mapsmeexchangehelper.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.mapsmeexchangehelper.R;
import com.example.mapsmeexchangehelper.model.TariffPlan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    String selectedTariff;
    String selectedCurrency;
    String transitionAmount;


    ArrayList spinnerTariffArrayList;
    Spinner spinnerTariff;
    ArrayAdapter spinnerTariffAdapter;

    ArrayList spinnerCurrencyArrayList;
    Spinner spinnerCurrency;
    ArrayAdapter spinnerCurrencyAdapter;

    JSONArray jCurrencyArray;
    HashMap<String, String> currencyMap;

    JSONObject jPlanObject;
    HashMap<String, TariffPlan> planMap;

    RequestQueue queue;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);

        currencyMap = new HashMap();
        planMap = new HashMap();

        handlePlanJson();
        handleCurrencyJson();


        createTariffSpinner();
        createCurrencySpinner();

        setListenerForNumberInput();
    }

    void createTariffSpinner() {
        spinnerTariff = findViewById(R.id.spinnerTariff);
        spinnerTariff.setOnItemSelectedListener(this);

        spinnerTariffArrayList = new ArrayList();

        for (TariffPlan tariff : planMap.values()) {
            spinnerTariffArrayList.add(tariff.name);
        }

        spinnerTariffAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerTariffArrayList);
        spinnerTariffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTariff.setAdapter(spinnerTariffAdapter);

        int savedTariffIndex = sharedPref.getInt(getString(R.string.tariff_index), 1);
        spinnerTariff.setSelection(savedTariffIndex);
    }

    void createCurrencySpinner() {
        spinnerCurrency = findViewById(R.id.spinnerCurrency);
        spinnerCurrency.setOnItemSelectedListener(this);

        spinnerCurrencyArrayList = new ArrayList();
        try {
            for (int i = 0; i < jCurrencyArray.length(); i++) {
                JSONObject js3 = jCurrencyArray.getJSONObject(i);
                String name = js3.getString("name");
                String currencyCode = js3.getString("cc");
                spinnerCurrencyArrayList.add(name);

                currencyMap.put(name, currencyCode);
            }
        } catch (JSONException x) {
            Log.d("LOG", String.valueOf(x));
        }


        spinnerCurrencyAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerCurrencyArrayList);
        spinnerCurrencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(spinnerCurrencyAdapter);

        int savedCurrencyIndex = sharedPref.getInt(getString(R.string.currency_index), 43);
        spinnerCurrency.setSelection(savedCurrencyIndex);
    }

    void handleCurrencyJson() {
        InputStream inputStream = this.getResources().openRawResource(R.raw.currency);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();


        try {
            JSONObject jObject = new JSONObject(jsonString);
            jCurrencyArray = jObject.getJSONArray("currency");
        } catch (JSONException e) {
            Log.d("LOG", "Faral Error " + e);
        }
    }

    void handlePlanJson() {
        InputStream inputStream = this.getResources().openRawResource(R.raw.plan);
        String jsonString = new Scanner(inputStream).useDelimiter("\\A").next();


        try {
            jPlanObject = new JSONObject(jsonString);
            Iterator<String> keys = jPlanObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                if (jPlanObject.get(key) instanceof JSONObject) {
                    planMap.put(key, new TariffPlan(jPlanObject.getJSONObject(key)));
                }
            }
        } catch (JSONException e) {
            Log.d("LOG", "Faral Error " + e);
        }
    }

    void sendRequest() {
        String url = "https://www.mastercard.com/settlement/currencyrate/conversion-rate?fxDate" +
                "=0000-00-00&transCurr=" + currencyMap.get(selectedCurrency) + "&crdhldBillCurr=GBP&bankFee=0&transAmt=" + transitionAmount;
        Log.d("LOG", url);

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                try {
                    JSONObject dataObject = response.getJSONObject("data");
                    String costInGBP = dataObject.getString("crdhldBillAmt");


                    fetchRateUSDCtoGBP(costInGBP);
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "Error: unexpected json format",
                            Toast.LENGTH_SHORT).show();
                } finally {
                    Button calculate = findViewById(R.id.calculate);
                    calculate.setEnabled(true);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });


        Button calculate = findViewById(R.id.calculate);
        calculate.setEnabled(false);

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    void fetchRateUSDCtoGBP(String costInGBP) {
        String url = "https://api.benqq.io/v1/rates/usdc-gbp";
        Log.d("LOG", url);

        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {


            @Override
            public void onResponse(JSONObject response) {

                try {
                    String rateUSDCtoGBP = response.getString("data");
                    try {
                        float GBPRate = 1 / Float.parseFloat(rateUSDCtoGBP);

                        float fee;

                        final CheckBox ATMcheckbox = findViewById(R.id.checkBoxAtm);
                        TariffPlan tariff = getTariff(selectedTariff);
                        if (ATMcheckbox.isChecked()) {
                            fee = tariff.atmWithdrawalFee;
                        } else fee = tariff.fee;

                        float fix = 0;
                        final CheckBox internationalFeeCheckbox = findViewById(R.id.internationalFeeCheckbox);
                        if (internationalFeeCheckbox.isChecked()) {
                            fix = tariff.fx;
                        }
                        float total = Float.parseFloat(costInGBP) * GBPRate * fee + fix;
                        TextView answer = findViewById(R.id.answer);
                        answer.setText(String.format("%.02f", total) + " $");
                    } catch (NumberFormatException e) {
                        Toast.makeText(MainActivity.this, String.valueOf(e),
                                Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, String.valueOf(e),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO: Handle error
            }
        });

        queue.add(stringRequest);
    }

    void setListenerForNumberInput() {
        EditText numberInput = findViewById(R.id.editTextNumber);
        numberInput.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    calculate(null);
                    return true;
                }
                return false;
            }
        });
    }

    void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    TariffPlan getTariff(String tariffFullName) {
        for (TariffPlan tariff : planMap.values()) {
            if (Objects.equals(tariff.name, tariffFullName)) {
                return tariff;
            }
        }
        return new TariffPlan();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.spinnerTariff) {
            selectedTariff = spinnerTariff.getSelectedItem().toString();
            editor.putInt(getString(R.string.tariff_index), position);
            editor.apply();
        } else if (parent.getId() == R.id.spinnerCurrency) {
            selectedCurrency = spinnerCurrency.getSelectedItem().toString();
            editor.putInt(getString(R.string.currency_index), position);
            editor.apply();
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void calculate(View view) {
        EditText userNumber = findViewById(R.id.editTextNumber);
        transitionAmount = userNumber.getText().toString();
        if (transitionAmount.length() == 0) {
            Toast.makeText(this, "Enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView answer = findViewById(R.id.answer);
        answer.setText("Loading...");

        hideKeyboard(this);
        sendRequest();
    }
}