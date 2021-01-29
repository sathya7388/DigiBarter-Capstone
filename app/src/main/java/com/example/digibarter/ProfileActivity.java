package com.example.digibarter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {
    TextView name,email,phone,address,location,rewards;
    JSONObject profileObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        name = findViewById(R.id.name);
        email=findViewById(R.id.email);
        phone=findViewById(R.id.phone);
        address=findViewById(R.id.address);
        location = findViewById(R.id.location);
        rewards = findViewById(R.id.rewards);
        getProfileDetails();
        findViewById(R.id.myListings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, MyListings.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });
    }


    void getProfileDetails()
    {
        String url = "https://digi-barter.herokuapp.com/getUser?userId="+SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(),"id",-1);
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching details..");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                pDialog.hide();
                try {
                    JSONObject obj = new JSONObject(s);
                    profileObj = obj;

                    name.setText(obj.getString("id"));
                    email.setText(obj.getString("email"));
                    rewards.setText("Rewards : "+obj.getString("rewardPoints"));

                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                // hide the progress dialog
                pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }

    private void logout() {
        SharedPreferenceHelper.setSharedPreferenceString(getApplicationContext(), "username", "");
        SharedPreferenceHelper.setSharedPreferenceInt(getApplicationContext(), "id", -1);
        SharedPreferenceHelper.setSharedPreferenceInt(getApplicationContext(), "rewardPoints", -1);
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
    }
}