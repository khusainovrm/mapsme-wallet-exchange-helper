package com.example.mapsmeexchangehelper.model;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;


public class TariffPlan {
    public String name;
    public float fee;
    public float atmWithdrawalFee;
    public float fx;

    public TariffPlan(@NonNull JSONObject jObject) {
        try {
            this.name = jObject.getString("name");
            this.fee = Float.parseFloat(jObject.getString("fee"));
            this.atmWithdrawalFee = Float.parseFloat(jObject.getString("atmWithdrawalFee"));
            this.fx = Float.parseFloat(jObject.getString("fx"));
        } catch (JSONException e) {
            Log.d("LOG", "parse error: " + e);
        }

    }

    public TariffPlan() {
    }
}
