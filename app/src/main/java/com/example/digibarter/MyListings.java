package com.example.digibarter;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.digibarter.adapter.profileListing.MyBartedListingAdapter;
import com.example.digibarter.adapter.profileListing.MyBartingListingAdapter;
import com.example.digibarter.adapter.profileListing.MyFavListingAdapter;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

enum ListType {
    FAVOURITES,
    BARTED,
    PURCHASE,
    BARTING
}

public class MyListings extends AppCompatActivity {
    RecyclerView recycleView;
    TabLayout listingTab;
    int images[] = {R.drawable.a, R.drawable.aa, R.drawable.ab, R.drawable.b, R.drawable.ac};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_listings);
        recycleView = findViewById(R.id.listingReycler);
        listingTab = findViewById(R.id.listingTab);
        updateListingRecycler(ListType.FAVOURITES);
        updateListingRecycler(ListType.FAVOURITES);
        listingTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 0) {
                    updateListingRecycler(ListType.FAVOURITES);
                } else if (pos == 1) {
                    updateListingRecycler(ListType.BARTED);
                } else if (pos == 2) {
                    updateListingRecycler(ListType.PURCHASE);
                } else {
                    updateListingRecycler(ListType.BARTING);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void getListingData(int position) {
    }

    private void updateListingRecycler(ListType type) {
        if (type == ListType.FAVOURITES) {
            getFavourites();
        }
        if (type == ListType.BARTED) {
            getBarted();
        }
        if (type == ListType.PURCHASE) {
            getMyPrurchases();
        }
        if (type == ListType.BARTING) {
            getMyBarting();
        }
    }

    void getFavourites() {
        String url = "https://digi-barter.herokuapp.com/getWishList?userId=" + SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1);
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching Details..");
        pDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String s) {
                        pDialog.hide();
                        ArrayList<Product> arrayListFav = new ArrayList<>();
                        ArrayList<Integer> favIds = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(s);
                            arrayListFav.clear();
                            if (jsonArray.length() == 0) {
                                updateFavouriteList(new ArrayList<Product>(),new ArrayList<Integer>());
                                Toast.makeText(getApplicationContext(), "No products found in Favourites list.", Toast.LENGTH_LONG).show();
                            }
                            for (int i = 0; i < jsonArray.length(); i++) {
                                //   public Product(int id, int rewards, int userid, int cat_id, int stocks, int usertype, String title, String desc, String createdDate, String[] images, double lat, double longt) {
                                JSONObject obj = jsonArray.getJSONObject(i).getJSONObject("product");
                                favIds.add(jsonArray.getJSONObject(i).getInt("id"));
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
                                arrayListFav.add(p);
                            }
                            updateFavouriteList(arrayListFav, favIds);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    void updateFavouriteList(ArrayList<Product> arrayListFav, ArrayList<Integer> favIds) {
        recycleView.removeAllViews();
        MyFavListingAdapter myListingAdapter = new MyFavListingAdapter(this, arrayListFav, favIds);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.setAdapter(myListingAdapter);
    }

    void getMyBarting() {
        String url = "https://digi-barter.herokuapp.com/getProducts?userType=0&category=1&time=all";
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching your products..");
        pDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String s) {
                        pDialog.hide();
                        ArrayList<Product> arrayList = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(s);

                            if (jsonArray.length() == 0) {
                                updateBartingList(new ArrayList<Product>());
                                Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
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
                                            "Selling: " + obj.getString("title"),
                                            obj.getString("description"),
                                            obj.getString("createdDate"),
                                            imgArr,
                                            Double.parseDouble(obj.getString("lat")),
                                            Double.parseDouble(obj.getString("longt")));
                                    if (p.userid == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                        arrayList.add(p);
                                    }
                                }
                                if (arrayList.size() == 0) {
                                    Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                                }
                            }
                            updateBartingList(arrayList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    void updateBartingList(ArrayList<Product> productArrayList) {

        recycleView.removeAllViews();
        MyBartingListingAdapter myListingAdapter = new MyBartingListingAdapter(this, productArrayList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.setAdapter(myListingAdapter);
    }

    void getBarted() {
        String url = "https://digi-barter.herokuapp.com/getBarts?userId=" + SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1);
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching details..");
        pDialog.show();

        final ArrayList<Product> firstPartyProducts = new ArrayList<>();
        final ArrayList<Product> secondPartyProducts = new ArrayList<>();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                pDialog.hide();
                try {
                    JSONArray jsonArray = new JSONArray(s);

                    if (jsonArray.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No Products for you.", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            //   public Product(int id, int rewards, int userid, int cat_id, int stocks, int usertype, String title, String desc, String createdDate, String[] images, double lat, double longt) {
                            JSONObject mainObj = jsonArray.getJSONObject(i);
                            int bartId = mainObj.getInt("id");
                            String bartStatus = mainObj.getString("bartStatus");
                            JSONObject obj = mainObj.getJSONObject("firstPartyProduct");

                            JSONArray imgObjArr = obj.getJSONArray("images");
                            String[] imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }

                            Product firstPartyProduct = new Product(obj.getInt("id"),
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
                            String fpName = obj.getJSONObject("user").getString("name");
                            int fpId = obj.getJSONObject("user").getInt("id");
                            int fpRewards = obj.getJSONObject("user").getInt("rewardPoints");

                            obj = mainObj.getJSONObject("secondPartyProduct");
                            imgObjArr = obj.getJSONArray("images");
                            imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }

                            Product secondPartyProduct = new Product(obj.getInt("id"),
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

                            int spId = obj.getJSONObject("user").getInt("id");

                            if ((bartStatus.equals("ACCEPT"))) {
                                if (firstPartyProduct.id == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                    firstPartyProduct.title = "Requested: " + firstPartyProduct.title;
                                } else {
                                    secondPartyProduct.title = "Received: " + secondPartyProduct.title;
                                }
                                firstPartyProducts.add(firstPartyProduct);
                                secondPartyProducts.add(secondPartyProduct);
                            }
                        }
                    }
                    if (firstPartyProducts.size() > 0) {
                        //firstPartyProducts.addAll(secondPartyProducts);
                        updateBartedList(firstPartyProducts);
                    } else {
                        updateBartedList(new ArrayList<Product>());
                        Toast.makeText(getApplicationContext(), "No Products for you.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);

    }


    void updateBartedList(ArrayList<Product> productArrayList) {
        recycleView.removeAllViews();
        MyBartedListingAdapter myListingAdapter = new MyBartedListingAdapter(this, productArrayList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.setAdapter(myListingAdapter);
    }

    void getMyPrurchases() {
        String url = "https://digi-barter.herokuapp.com/getPurchases?userId="+SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", 0);
        final ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Fetching your products..");
        pDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String s) {
                        pDialog.hide();
                        ArrayList<Product> arrayList = new ArrayList<>();
                        try {
                            JSONArray jsonArray = new JSONArray(s);

                            if (jsonArray.length() == 0) {
                                updateBartedList(new ArrayList<Product>());
                                Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    //   public Product(int id, int rewards, int userid, int cat_id, int stocks, int usertype, String title, String desc, String createdDate, String[] images, double lat, double longt) {
                                    JSONObject productObj = jsonArray.getJSONObject(i);
                                    JSONObject  obj = productObj.getJSONObject("product");
                                    JSONObject  userObj = productObj.getJSONObject("user");
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
                                            "Selling: " + obj.getString("title"),
                                            obj.getString("description"),
                                            obj.getString("createdDate"),
                                            imgArr,
                                            Double.parseDouble(obj.getString("lat")),
                                            Double.parseDouble(obj.getString("longt")));
                                   if (userObj.getInt("id") == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                        arrayList.add(p);
                                    }
                                }
                                if (arrayList.size() == 0) {
                                    updateBartedList(new ArrayList<Product>());
                                    Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                                }
                            }
                            updateBartedList(arrayList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


}