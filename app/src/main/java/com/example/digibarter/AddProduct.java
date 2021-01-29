package com.example.digibarter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.digibarter.adapter.ProductImagesDataAdapters;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.ProductImages;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddProduct extends AppCompatActivity implements OnMapReadyCallback {
    TextView textViewTitle, textViewCategory, textViewDesc;
    EditText title, desc;
    Spinner spinnerCategory;
    ArrayList<String> categoryList = new ArrayList<>();
    String categoryValue;
    String mandatory = "<font color='#EE0000'>**</font>";
    String titleText = "Title", descText = "Description", spinnerText = "Category";
    int productId = -1;
    public static final int GALLERY_REQUEST_CODE = 101;
    ArrayList<ProductImages> productImages = new ArrayList<>();

    LocationManager locationManager;
    Location userLocation;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);
        productId = getIntent().getIntExtra("pid", -1);
        spinnerCategory = (Spinner) findViewById(R.id.spinnerCategory);
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);

        textViewTitle = findViewById(R.id.textViewTitle);
        textViewTitle.setText(Html.fromHtml(titleText + mandatory));
        textViewCategory = findViewById(R.id.textViewCategory);
        textViewCategory.setText(Html.fromHtml(spinnerText + mandatory));
        textViewDesc = findViewById(R.id.textViewDesc);
        textViewDesc.setText(Html.fromHtml(descText + mandatory));
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getCategories();
        updateSpinner();
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                categoryValue = String.valueOf(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        loadImagesView();
        findViewById(R.id.buttonAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addProduct();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        getAddressFromLocation();
        if (productId != -1) {
            getProductDetails();
        }
    }

    private void getCategories() {
        String url = "https://digi-barter.herokuapp.com/getCategories";
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    if (jsonArray.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            categoryList.add(obj.getString("name"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        });

        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void updateSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, categoryList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(arrayAdapter);
    }

    private void getAddressFromLocation() {

        Location gps_loc = null, network_loc = null;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        try {
            gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (gps_loc != null) {
            userLocation = gps_loc;
        } else if (network_loc != null) {
            userLocation = network_loc;
        } else {
            userLocation.setLongitude(43.777702);
            userLocation.setLongitude(-79.233238);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 20.0f));
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 20.0f));

    }

    void loadImagesView() {
        findViewById(R.id.addImgBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickFromGallery();
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recyclerViewImgs);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        ArrayList<ProductImages> imageUrlList = productImages;
        ProductImagesDataAdapters dataAdapter = new ProductImagesDataAdapters(AddProduct.this, imageUrlList);
        recyclerView.setAdapter(dataAdapter);
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData returns the content URI for the selected Image
                    Uri selectedImage = data.getData();
                    uploadImageFile(getPath(selectedImage));
                    break;
            }
    }

    public boolean uploadImageFile(String imagePath) {

        byte[] audioBytes = getByteArr(imagePath);
        final String imageString = Base64.encodeToString(audioBytes, Base64.DEFAULT);
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading the Image...");
        pDialog.show();*/
        StringRequest request = new StringRequest(Request.Method.POST, "http://alllinks.online/andproject/fileUpload.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    // pDialog.hide();
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getString("status").equals("success")) {
                        Toast.makeText(AddProduct.this, "Uploaded Successful", Toast.LENGTH_LONG).show();

                        productImages.add(new ProductImages(-1, jsonObject.getString("path")));
                        loadImagesView();
                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // pDialog.hide();
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        }) {
            //adding parameters to send
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("fileToUpload", imageString);
                parameters.put("type", "jpg");
                return parameters;
            }
        };

        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
        return true;
    }

    public void removeImageFromList(String removeKey) {
        for (int i = 0; i < productImages.size(); i++) {
            if (productImages.get(i).url.equals(removeKey)) {
                productImages.remove(i);
            }
        }
        loadImagesView();
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    byte[] getByteArr(String path) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(path));

            int read;
            byte[] buff = new byte[1024];
            while ((read = in.read(buff)) > 0) {
                out.write(buff, 0, read);
            }
            out.flush();
            return out.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private void addProduct() throws JSONException {
        String url = "https://digi-barter.herokuapp.com/addProduct";
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Uploading Ad...");*/
        //pDialog.show();
        EditText title, desc;
        title = findViewById(R.id.title);
        desc = findViewById(R.id.desc);

        if (title.getText().toString().equalsIgnoreCase("")) {
            title.setError("Please enter title");
            title.setHint("Please enter title");
            title.requestFocus();
            return;
        }
        if (desc.getText().toString().equalsIgnoreCase("")) {
            desc.setError("Please enter description");
            desc.setHint("Please enter description");
            desc.requestFocus();
            return;
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", title.getText().toString());
        jsonObject.put("description", desc.getText().toString());
        jsonObject.put("userId", SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", 0));
        jsonObject.put("userType", 0);
        jsonObject.put("categoryId", 6);
        JSONArray jsonArray = new JSONArray();

        for (ProductImages pi : productImages) {
            jsonArray.put(new JSONObject().put("imageLink", pi.url));
        }
        jsonObject.put("images", jsonArray);
        jsonObject.put("lat", String.valueOf(userLocation.getLatitude()));
        jsonObject.put("longt", String.valueOf(userLocation.getLongitude()));
        final String requestBody = jsonObject.toString();

        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                try {
                    //pDialog.hide();
                    JSONObject jsonObject = new JSONObject(s);
                    if (jsonObject.getInt("userId") == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", 0)) {
                        Toast.makeText(AddProduct.this, "Uploaded Successful", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(AddProduct.this, "Error Occurred", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // pDialog.hide();
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        }) {
            //adding parameters to send
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }


        };
        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }


    public void getProductDetails() {
        String url = "https://digi-barter.herokuapp.com/getProduct?productId=" + productId;
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Details");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject obj) {
                // pDialog.hide();
                try {
                    JSONArray imgObjArr = obj.getJSONArray("images");
                    String[] imgArr = new String[imgObjArr.length()];
                    for (int j = 0; j < imgObjArr.length(); j++) {
                        imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                    }
                    title.setText(obj.getString("title"));
                    desc.setText(obj.getString("description"));
                    if(!obj.getString("lat").isEmpty() && !obj.getString("longt").isEmpty()){
                        userLocation.setLatitude(Double.parseDouble(obj.getString("lat")));
                        userLocation.setLatitude(Double.parseDouble(obj.getString("longt")));
                    }
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(), userLocation.getLongitude()), 20.0f));
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Sorry, Some error occured", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Sorry, Some error occured", Toast.LENGTH_LONG).show();
                // hide the progress dialog
                // pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }
}
