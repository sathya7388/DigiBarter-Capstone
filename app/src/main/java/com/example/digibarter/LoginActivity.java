package com.example.digibarter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        skipLogin();
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
        findViewById(R.id.openSignups).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
            }
        });
    }

    private void skipLogin() {
        if(!SharedPreferenceHelper.getSharedPreferenceString(getApplicationContext(),"username","").equals(""))
        {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }


    private void attemptLogin() {
        final EditText emailET = findViewById(R.id.username);
        EditText passwordET = findViewById(R.id.password);

        if(emailET.getText().toString().length() ==0 || passwordET.getText().toString().length() ==0 )
        {
            Toast.makeText(getApplicationContext(),"Both fields are required!!",Toast.LENGTH_LONG).show();
            return;
        }
        String url = "https://digi-barter.herokuapp.com/login?email="+emailET.getText().toString()+"&password="+passwordET.getText().toString();
       /* final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loggin In...");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                       // pDialog.hide();
                        try {
                            if(response.getString("status").equals("success"))
                            {

                                Toast.makeText(getApplicationContext(),"Welcome",Toast.LENGTH_LONG).show();
                                SharedPreferenceHelper.setSharedPreferenceString(getApplicationContext(),"username",emailET.getText().toString());
                                SharedPreferenceHelper.setSharedPreferenceInt(getApplicationContext(),"id",response.getInt("id"));
                                SharedPreferenceHelper.setSharedPreferenceInt(getApplicationContext(),"rewardPoints",response.getInt("id"));

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"Invalid Credentials",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                // hide the progress dialog
              //  pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }
}