package com.example.digibarter;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.digibarter.adapter.ChatListAdapter;
import com.example.digibarter.adapter.StorePageAdapter;
import com.example.digibarter.adapter.homeRecycleAdapter;
import com.example.digibarter.adapter.notificationRecycleAdapter;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.ChatUser;
import com.example.digibarter.model.Product;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView recycleView;
    Toolbar searchToolBar;
    TextView locationText;
    Boolean filterFlag = false;
    LocationManager locationManager;
    Location userLocation;
    SearchView searchView;
    ProgressBar progress_circular;

    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;

    //filter Page values
    String categoryValue = "0", userTypeValue = "2",  timeValue = "all", distanceFilter = "1000";

    ArrayList<Product> products = new ArrayList<>();
    ArrayList<Product> filterProduct;
    ArrayList<String> categoryList = new ArrayList<>();

    homeRecycleAdapter homeAdapter;
    notificationRecycleAdapter notificationRecycleAdapter;
    ChatListAdapter chatListAdapter;
    String defURL;

    // for store
    ViewPager2 viewPager2;

    //chat
    List<ChatUser> chatUsers;
    ListView chatUsersList;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        defURL = "https://digi-barter.herokuapp.com/getProducts?userType=-1&category=1&time=all";

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });
        progress_circular = findViewById(R.id.progress_circular);
        searchToolBar = findViewById(R.id.toolbar);
        recycleView = findViewById(R.id.recycleView);
        locationText = findViewById(R.id.locationText);
        // for store
        viewPager2 = findViewById(R.id.viewPager);
        viewPager2.setVisibility(View.GONE);

        //chat
        chatUsersList = findViewById(R.id.chatUserList);
        chatUsersList.setVisibility(View.GONE);
        chatUsersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent chatIntent = new Intent(MainActivity.this, ChatActivity.class);
                chatIntent.putExtra("firstPartyUserId", SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1));
                chatIntent.putExtra("secondPartUserId", chatUsers.get(position).getUserId());
                chatIntent.putExtra("targetUserName", chatUsers.get(position).getUserName());
                startActivity(chatIntent);
            }
        });

        progress_circular.setVisibility(View.GONE);
        getCategories();

        findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.VISIBLE);
                locationText.setVisibility(View.VISIBLE);
                viewPager2.setVisibility(View.GONE);
                chatUsersList.setVisibility(View.INVISIBLE);
                getProduct(defURL);
            }
        });
        findViewById(R.id.notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.GONE);
                locationText.setVisibility(View.INVISIBLE);
                viewPager2.setVisibility(View.GONE);
                chatUsersList.setVisibility(View.INVISIBLE);
                getNotification();
            }
        });
        findViewById(R.id.addProduct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.GONE);
                locationText.setVisibility(View.INVISIBLE);
                viewPager2.setVisibility(View.GONE);
                chatUsersList.setVisibility(View.INVISIBLE);
                startActivity(new Intent(getApplicationContext(), AddProduct.class));
            }
        });
        findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchToolBar.setVisibility(View.GONE);
                locationText.setVisibility(View.INVISIBLE);
                viewPager2.setVisibility(View.INVISIBLE);
                updateChatRecycler();
            }
        });
        findViewById(R.id.store).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchToolBar.setVisibility(View.GONE);
                locationText.setVisibility(View.INVISIBLE);
                viewPager2.setVisibility(View.VISIBLE);
                chatUsersList.setVisibility(View.INVISIBLE);
                getStoreProducts();
            }
        });

        findViewById(R.id.profilePage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
            }
        });

        findViewById(R.id.filterPage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, filterPage.class);
                intent.putExtra("categorySpinner", categoryList);
                intent.putExtra("userTypeValue", userTypeValue);
                intent.putExtra("categoryValue", categoryValue);
                intent.putExtra("timeValue", timeValue);
                intent.putExtra("distanceFilter", distanceFilter);
                startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
            }
        });
        getAddressFromLocation();
        getProduct(defURL);
    }

    private void getAddressFromLocation() {
        Location gps_loc = null,network_loc = null;
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

        if(gps_loc != null) {
            userLocation = gps_loc;
        }else if (network_loc != null) {
            userLocation = network_loc;
        }else {
            userLocation.setLongitude(43.777702);
            userLocation.setLongitude(-79.233238);
        }
        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);
        try {
            String subLocality = null;
            List<Address> addresses = geocoder.getFromLocation(userLocation.getLatitude(),userLocation.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                if(fetchedAddress.getSubLocality() == null){
                    subLocality = fetchedAddress.getSubAdminArea();
                }else{
                    subLocality = fetchedAddress.getSubLocality();
                }
                locationText.setText(subLocality+" "+fetchedAddress.getPostalCode());
            } else {
                locationText.setText("Toronto,ON");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void performSearch(String query) {
        ArrayList<Product> filteredList = new ArrayList<>();
        if(query == null || query.length() == 0){
            updateHomeRecycler(products);
        }else{
            filterProduct = new ArrayList(products);
            String filterPattern = query.toString().toLowerCase().trim();
            for (Product item : filterProduct) {
                if (item.title.toLowerCase().contains(filterPattern)) {
                    filteredList.add(item);
                }
            }
            updateHomeRecycler(filteredList);
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

    // Filter page selected filters
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String myStr = data.getStringExtra("filterQuery");
                categoryValue = data.getStringExtra("categoryValue");
                userTypeValue = data.getStringExtra("userTypeValue");
                timeValue = data.getStringExtra("timeValue");
                distanceFilter = data.getStringExtra("distanceFilter");
                filterFlag = true;
                System.out.println(myStr);
                recycleView.removeAllViews();
                getProduct(myStr);
            }
        }
    }

    private void updateHomeRecycler(ArrayList<Product> productsList) {
        recycleView.removeAllViews();
        viewPager2.setAdapter(new StorePageAdapter(this, new ArrayList<Product>()));
        chatUsersList.setAdapter(new ChatListAdapter(this,new ArrayList<ChatUser>()));
        homeAdapter = new homeRecycleAdapter(this, productsList);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.setAdapter(homeAdapter);
    }

    private void updateNotificationRecycler(ArrayList<Product> firstPartyProducts, ArrayList<Product> secondPartyProducts, ArrayList<String> fpnames, ArrayList<Integer> fpIDs, ArrayList<Integer> bartIds, ArrayList<Integer> reqUserRewards) {
        recycleView.removeAllViews();
        viewPager2.setAdapter(new StorePageAdapter(this, new ArrayList<Product>()));
        chatUsersList.setAdapter(new ChatListAdapter(this,new ArrayList<ChatUser>()));
        notificationRecycleAdapter = new notificationRecycleAdapter(this, firstPartyProducts, secondPartyProducts, fpnames, fpIDs, bartIds, reqUserRewards);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 1, GridLayoutManager.VERTICAL, false);
        recycleView.setLayoutManager(gridLayoutManager);
        recycleView.setAdapter(notificationRecycleAdapter);
    }

    private void updateChatRecycler() {
        recycleView.removeAllViews();
        recycleView.setAdapter(new homeRecycleAdapter(this,new ArrayList<Product>()));
        viewPager2.removeAllViews();
        viewPager2.setAdapter(new StorePageAdapter(this,new ArrayList<Product>()));


        chatUsersList.setVisibility(View.VISIBLE);
        chatUsers = new ArrayList<>();
        int currentUserId = SharedPreferenceHelper.getSharedPreferenceInt(this, "id", -1);
        DatabaseReference userReference =  FirebaseDatabase.getInstance().getReference(String.valueOf(currentUserId));

        ChatListAdapter chatListAdapter = new ChatListAdapter(this, chatUsers);
        chatUsersList.setAdapter(chatListAdapter);



        userReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatUser chatUser = snapshot.getValue(ChatUser.class);
                chatUsers.add(chatUser);
                chatUsersList.invalidateViews();
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void getProduct(String productURL) {

        progress_circular.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.GET, productURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progress_circular.setVisibility(View.INVISIBLE);
                try {
                    JSONArray jsonArray = new JSONArray(s);
                    products.clear();
                    if (jsonArray.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            Location loc2 = new Location("");
                            loc2.setLatitude(Double.parseDouble(obj.getString("lat")));
                            loc2.setLongitude(Double.parseDouble(obj.getString("longt")));

                            int productDistance = (int) (userLocation.distanceTo(loc2)/1000);


                            JSONArray imgObjArr = obj.getJSONArray("images");
                            String[] imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }

                            if(distanceFilter.equals("1000")){
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
                                if (p.userid != SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                    products.add(p);
                                }
                            }else{
                                if(productDistance < Integer.parseInt(distanceFilter)){
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
                                    if (p.userid != SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) {
                                        products.add(p);
                                    }
                                }
                            }


                        }
                    }
                    updateHomeRecycler(products);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progress_circular.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        });

        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    public void getNotification() {
        String url = "https://digi-barter.herokuapp.com//getBarts?userId=" + SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1);
        progress_circular.setVisibility(View.VISIBLE);
        final ArrayList<Product> firstPartyProducts = new ArrayList<>();
        final ArrayList<Product> secondPartyProducts = new ArrayList<>();
        final ArrayList<String> fpnames = new ArrayList<>();
        final ArrayList<Integer> fpIDs = new ArrayList<>();
        final ArrayList<Integer> bartIds = new ArrayList<>();
        final ArrayList<Integer> reqUserRewards = new ArrayList<>();
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                progress_circular.setVisibility(View.INVISIBLE);
                try {
                    JSONArray jsonArray = new JSONArray(s);

                    if (jsonArray.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No Notifications for you.", Toast.LENGTH_LONG).show();
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
                            ;
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

                            if ((spId == SharedPreferenceHelper.getSharedPreferenceInt(getApplicationContext(), "id", -1)) && (bartStatus.equals("REQUEST"))) {
                                firstPartyProducts.add(firstPartyProduct);
                                secondPartyProducts.add(secondPartyProduct);
                                fpnames.add(fpName);
                                fpIDs.add(fpId);
                                bartIds.add(bartId);
                                reqUserRewards.add(fpRewards);
                            }
                        }
                    }
                    if (firstPartyProducts.size() > 0) {
                        updateNotificationRecycler(firstPartyProducts, secondPartyProducts, fpnames, fpIDs, bartIds, reqUserRewards);
                    } else {
                        updateNotificationRecycler(firstPartyProducts, secondPartyProducts, fpnames, fpIDs, bartIds, reqUserRewards);
                        Toast.makeText(getApplicationContext(), "No Notifications for you.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progress_circular.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void getStoreProducts() {

        String url = "https://digi-barter.herokuapp.com/getProducts?userType=1&category=1&time=all";
        progress_circular.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progress_circular.setVisibility(View.INVISIBLE);
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    products.clear();
                    if (jsonArray.length() == 0) {
                        Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                    } else {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);

                            JSONArray imgObjArr = obj.getJSONArray("images");
                            String[] imgArr = new String[imgObjArr.length()];
                            for (int j = 0; j < imgObjArr.length(); j++) {
                                imgArr[j] = imgObjArr.getJSONObject(j).getString("imageLink");
                            }
                            if (obj.getInt("userType") == 1) {
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
                                products.add(p);
                            }
                        }
                        if (products.size() > 0) {
                            loadViewPager(products);
                        } else {
                            loadViewPager(products);
                            Toast.makeText(getApplicationContext(), "No products found.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                progress_circular.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Some error occurred -> " + volleyError, Toast.LENGTH_LONG).show();
            }
        });
        request.setShouldCache(false);
        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private void loadViewPager(ArrayList<Product> productsList) {
        recycleView.removeAllViews();

        recycleView.setAdapter(new homeRecycleAdapter(this, new ArrayList<Product>()));
        chatUsersList.setAdapter(new ChatListAdapter(this,new ArrayList<ChatUser>()));
        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
//        viewPager2.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                page.setScaleY(0.85F + (1 - Math.abs(position)) * 0.15f);
            }
        });
        viewPager2.setPageTransformer(compositePageTransformer);
        viewPager2.setAdapter(new StorePageAdapter(this, productsList));
    }

}