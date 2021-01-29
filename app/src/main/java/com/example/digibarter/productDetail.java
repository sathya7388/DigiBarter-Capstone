package com.example.digibarter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.digibarter.adapter.ViewProductImagesDataAdapters;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Category;
import com.example.digibarter.model.ChatUser;
import com.example.digibarter.model.Product;
import com.example.digibarter.model.ProductImages;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class productDetail extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {
    GoogleMap mapView;
    int productId = -1;
    int userId = -1;
    int userType = -1;
    TextView category, title, desc, user, location, storeValue;
    boolean mapready = false;
    Product product = null;
    ExtendedFloatingActionButton chat, bart, buy;
    boolean favFlag = false;
    int wishListId;

    //chat code
    String chatUserName;
    int chatUserId;
    Button btnChat;
    private DatabaseReference userReference1;
    private DatabaseReference userReference2;

    ArrayList<Product> productsOfMe = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        getSupportActionBar().hide();
        productId = getIntent().getIntExtra("pid", -1);

        userType = getIntent().getIntExtra("userType", -1);
        //if (userType == 1) {
        //     userId = -1;
        //  } else {
        userId = getIntent().getIntExtra("userId", -1);
        //    }
        title = findViewById(R.id.priceText);
        category = findViewById(R.id.productNameText);
        desc = findViewById(R.id.descriptionText);
        user = findViewById(R.id.sellerName);
        storeValue = findViewById(R.id.storeValue);
        storeValue.setVisibility(View.GONE);
        location = findViewById(R.id.location);
        chat = findViewById(R.id.chat);
        bart = findViewById(R.id.bart);
        buy = findViewById(R.id.buy);

        chat.setVisibility(View.GONE);
        bart.setVisibility(View.GONE);
        buy.setVisibility(View.GONE);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyProducts();
            }
        });
        ((ImageButton) findViewById(R.id.button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favFlag) {
                    removeFav(wishListId);
                } else {
                    addToFav();
                }
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        //chat code
        btnChat = findViewById(R.id.chat);

        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentUserId = SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1);
                String currentUserName = SharedPreferenceHelper.getSharedPreferenceString(getApplicationContext(), "username", "unknown");
                userReference1 = FirebaseDatabase.getInstance().getReference(String.valueOf(currentUserId));
                userReference2 = FirebaseDatabase.getInstance().getReference(String.valueOf(chatUserId));

                ChatUser chatUser1 = new ChatUser();
                chatUser1.setUserId(chatUserId);
                chatUser1.setUserName(chatUserName);

                ChatUser chatUser2 = new ChatUser();
                chatUser2.setUserId(currentUserId);
                chatUser2.setUserName(currentUserName);

                userReference1.child(String.valueOf(chatUserId)).setValue(chatUser1);
                userReference2.child(String.valueOf(currentUserId)).setValue(chatUser2);

                Intent chatIntent = new Intent(productDetail.this, ChatActivity.class);
                chatIntent.putExtra("firstPartyUserId", SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1));
                chatIntent.putExtra("secondPartUserId", chatUserId);
                chatIntent.putExtra("targetUserName", chatUserName);
                startActivity(chatIntent);
            }
        });
    }

    private void buyProducts() {
        int userid = SharedPreferenceHelper.getSharedPreferenceInt(this, "id", -1);
        String url = "https://digi-barter.herokuapp.com/purchase?userId=" + userid + "&productId=" + productId;
       /* final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Fetching Products...");
        pDialog.show();*/

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //   pDialog.hide();
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    //JSONArray jsonArray = new JSONArray(response);
                    if (jsonObject.length() == 0) {

                    } else {
                        String status = jsonObject.getString("status");
                        if (status.equals("failed")) {
                            failedToPurchaseDialog();
                        } else if (status.equals("bought product")) {
                            buy.setEnabled(false);
                            showPurchaseDialog();
                        } else {
                            buy.setEnabled(false);
                            showPurchaseDialog();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //    pDialog.hide();

            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getProductDetails();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapView = googleMap;
        mapView.getUiSettings().setMyLocationButtonEnabled(false);
        mapready = true;
        if (product != null) {
            updateLocationView();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        //mapView.setMyLocationEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mapCard) {
            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        }
    }


    private void getProductDetails() {

        String url = "https://digi-barter.herokuapp.com/getProduct?productId=" + productId + "&userId=" + userId;
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Details");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                        // pDialog.hide();
                        try {
                            JSONArray imgObjArr = obj.getJSONArray("images");
                            String[] imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }
                            Category categoryObj = new Category(obj.getJSONObject("category").getInt("id"), obj.getJSONObject("category").getString("name"));
                            product = new Product(obj.getInt("id"),
                                    obj.getInt("rewardPoints"),
                                    obj.getInt("userId"),
                                    categoryObj.id,
                                    obj.getInt("stocks"),
                                    obj.getInt("userType"),
                                    obj.getString("title"),
                                    obj.getString("description"),
                                    obj.getString("createdDate"),
                                    imgArr,
                                    Double.parseDouble(obj.getString("lat")),
                                    Double.parseDouble(obj.getString("longt")));
                            if (obj.has("wishList")) {
                                favFlag = true;
                                wishListId = obj.getJSONObject("wishList").getInt("id");
                                ((ImageButton) findViewById(R.id.button)).setImageResource(R.drawable.ic_baseline_favorite_24);
                            }
                            if (mapready) {
                                updateLocationView();
                            }
                            if (obj.getInt("userType") == 0) {
                                chat.setVisibility(View.VISIBLE);
                                bart.setVisibility(View.VISIBLE);
                                buy.setVisibility(View.GONE);
                            } else {
                                chat.setVisibility(View.GONE);
                                bart.setVisibility(View.GONE);
                                buy.setVisibility(View.VISIBLE);
                            }
                            category.setText(categoryObj.name);
                            user.setText(obj.getJSONObject("user").getString("name"));
                            title.setText(product.title);
                            desc.setText(product.desc);
                            if (product.usertype == 1) {
                                storeValue.setVisibility(View.VISIBLE);
                                storeValue.setText("Points " + String.valueOf(product.rewards));
                            } else {
                                storeValue.setVisibility(View.INVISIBLE);
                            }


                            //store detail for chat
                            chatUserName = obj.getJSONObject("user").getString("name");
                            chatUserId = obj.getInt("userId");

                            RecyclerView recyclerView = findViewById(R.id.recyclerViewImgs);
                            LinearLayoutManager linearLayoutManager
                                    = new LinearLayoutManager(
                                    getApplicationContext(),
                                    LinearLayoutManager.HORIZONTAL,
                                    false);
                            recyclerView.setLayoutManager(linearLayoutManager);
                            ArrayList<ProductImages> imageUrlList = new ArrayList<>();
                            for (String ia : imgArr) {
                                imageUrlList.add(new ProductImages(productId, ia));
                            }
                            if (imageUrlList.size() == 0) {
                                imageUrlList.add(new ProductImages(productId, "https://enhancedperformanceinc.com/wp-content/uploads/2017/10/product-dummy.png"));
                            }

                            ViewProductImagesDataAdapters dataAdapter = new ViewProductImagesDataAdapters(productDetail.this, imageUrlList);
                            recyclerView.setAdapter(dataAdapter);

                            bartClickListener();
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


    void updateLocationView() {
        LatLng latLng = new LatLng(product.lat, product.longt);
        mapView.addMarker(new MarkerOptions().position(latLng).title("Posted here").snippet("Posted here"));
        mapView.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(product.lat, product.longt, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String cityName = addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() + ", " + addresses.get(0).getCountryCode();
        if (cityName != null) {
            location.setText(cityName);
        }
    }


    void bartClickListener() {
        ExtendedFloatingActionButton bartBtn = findViewById(R.id.bart);
        bartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //callBart(pid);
                showUserItems();
            }
        });
    }

    void callBart(int myPid) {
        String url = "https://digi-barter.herokuapp.com/requestBart?firstPartyProductId=" + myPid + "&secondPartyProductId=" + productId;
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Details");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                        // pDialog.hide();
                        try {
                            if (obj.getString("status").equals("success")) {
                                Toast.makeText(getApplicationContext(), "Requested Bart", Toast.LENGTH_LONG).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Can't request barter", Toast.LENGTH_LONG).show();
                            }
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
                //  pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    void showUserItems() {
        String url = "https://digi-barter.herokuapp.com/getProducts?userType=0&category=1&time=all";
        /*final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Requesting...");
        pDialog.show();*/
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                //  pDialog.hide();
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    productsOfMe.clear();
                    if (jsonArray.length() == 0) {
                        noProductToBart();
                        //Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //   public Product(int id, int rewards, int userid, int cat_id, int stocks, int usertype, String title, String desc, String createdDate, String[] images, double lat, double longt) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            JSONArray imgObjArr = obj.getJSONArray("images");
                            String[] imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }

                            Product p = new Product(obj.getInt("id"),
                                    obj.getInt("rewardPoints"),
                                    obj.getInt("userId"),
                                    obj.getInt("categoryId"),
                                    obj.getInt("stocks"),
                                    obj.getInt("userType"),
                                    obj.getString("title"),
                                    obj.getString("description"),
                                    obj.getString("createdDate"),
                                    imgArr,
                                    Double.parseDouble(obj.getString("lat")),
                                    Double.parseDouble(obj.getString("longt")));
                            if (p.userid == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                productsOfMe.add(p);
                            }
                        }
                        if (productsOfMe.size() == 0) {
                            Toast.makeText(getApplicationContext(), "Sorry you don't have any product", Toast.LENGTH_LONG).show();
                        } else {
                            showSelectionDialog();
                        }
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
        });

        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    void showSelectionDialog() {
        final String[] items = new String[productsOfMe.size()];
        for (int i = 0; i < productsOfMe.size(); i++) {
            items[i] = productsOfMe.get(i).title;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select product to swap");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                Toast.makeText(getApplicationContext(), "You selected barting for " + productsOfMe.get(item).title, Toast.LENGTH_SHORT).show();
                callBart(productsOfMe.get(item).id);
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void noProductToBart() {
        final MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme);
        materialAlertDialogBuilder.setTitle("Unable to Bart");
        materialAlertDialogBuilder.setMessage("Sorry! You don't have any products to bart,\nAdd products to continue");
        materialAlertDialogBuilder.setNegativeButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        materialAlertDialogBuilder.show();
    }

    void addToFav() {
        String url = "https://digi-barter.herokuapp.com/addToWishList?userId=" + SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1) + "&productId=" + productId;
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Adding Fav..");
        pDialog.show();

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                        pDialog.hide();
                        favFlag = true;
                        ((ImageButton) findViewById(R.id.button)).setImageResource(R.drawable.ic_baseline_favorite_24);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Sorry, Some error occured", Toast.LENGTH_LONG).show();
                // hide the progress dialog
                pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    void removeFav(int id) {

        String url = "https://digi-barter.herokuapp.com/deleteWishList?wishListId=" + id;

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                        ((ImageButton) findViewById(R.id.button)).setImageResource(R.drawable.ic_baseline_favorite_border_24);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjReq);
    }


    private void showPurchaseDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme);
        materialAlertDialogBuilder.setTitle("Successfully Purchased");
        materialAlertDialogBuilder.setMessage("Thank you for Shopping.\nProduct will be delivered to your address");
        materialAlertDialogBuilder.setNegativeButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        materialAlertDialogBuilder.show();
    }

    private void failedToPurchaseDialog() {
        MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(this, R.style.RoundShapeTheme);
        materialAlertDialogBuilder.setTitle("Purchase Failed");
        materialAlertDialogBuilder.setMessage("Sorry! you do not have enough reward points to do this transaction");
        materialAlertDialogBuilder.setNegativeButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        materialAlertDialogBuilder.show();
    }
}

