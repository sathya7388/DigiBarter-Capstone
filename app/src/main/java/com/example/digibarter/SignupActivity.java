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
import com.example.digibarter.helpers.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getSupportActionBar().hide();
        findViewById(R.id.openLogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });
        findViewById(R.id.login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUP();
            }
        });
    }


    private void attemptSignUP() {
        String email,password,c_password,name;
        email = ((EditText)findViewById(R.id.username)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();
        c_password = ((EditText)findViewById(R.id.password2)).getText().toString();
        name =  ((EditText)findViewById(R.id.name)).getText().toString();
        if(password.length() == 0 || c_password.length()==0 || email.length()==0 || name.length()==0)
        {
            Toast.makeText(getApplicationContext(),"All fields are required!!!",Toast.LENGTH_LONG).show();
            return;
        }
        if(!password.equals(c_password))
        {
            Toast.makeText(getApplicationContext(),"Passwords do not match!!!",Toast.LENGTH_LONG).show();
            return;
        }
        String url = "https://digi-barter.herokuapp.com/signUp?userName="+ name +"&email="+ email +"&password=" + password;
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Signing up...");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                  //      pDialog.hide();
                        try {
                            if(response.getString("status").equals("success"))
                            {
//                                JSONObject responseJson = new JSONObject(response.toString());
//                                JSONObject pollution = responseJson.getJSONObject("data").getJSONObject("current").getJSONObject("pollution");
//                                JSONObject weather = responseJson.getJSONObject("data").getJSONObject("current").getJSONObject("weather");

                                Toast.makeText(getApplicationContext(),"Please Login to continue",Toast.LENGTH_LONG).show();

                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),"User Already Registered",Toast.LENGTH_LONG).show();
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