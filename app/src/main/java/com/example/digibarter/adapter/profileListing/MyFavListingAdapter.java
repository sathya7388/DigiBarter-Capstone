package com.example.digibarter.adapter.profileListing;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.digibarter.R;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;
import com.example.digibarter.productDetail;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyFavListingAdapter extends RecyclerView.Adapter<MyFavListingAdapter.MyFavListingHolder> {

    Context context;
    ArrayList<Product> products;
    ArrayList<Integer> favIds;
    public MyFavListingAdapter(Context context, ArrayList<Product> arrayListFav, ArrayList<Integer> favIds) {
        this.context = context;
        this.products = arrayListFav;
        this.favIds = favIds;
    }

    @NonNull
    @Override
    public MyFavListingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.listing_adapter, parent, false);
        return new MyFavListingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyFavListingHolder holder, final int position) {
        if(products.get(position).images.length == 0)
        {
            Glide.with(context).load("https://enhancedperformanceinc.com/wp-content/uploads/2017/10/product-dummy.png").into(holder.image);
        }
        else {
            Glide.with(context).load(products.get(position).images[0]).into(holder.image);
        }
        holder.listingName.setText(products.get(position).title);
        holder.listingCategory.setText(products.get(position).desc);
        holder.listingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, productDetail.class);
                i.putExtra("pid",products.get(position).id);
                i.putExtra("userId", SharedPreferenceHelper.getSharedPreferenceInt(context, "id", -1));
                i.putExtra("userType",products.get(position).usertype);
                context.startActivity(i);
            }
        });
        holder.listingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeFav(favIds.get(position));
                products.remove(position);
                favIds.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public class MyFavListingHolder extends RecyclerView.ViewHolder {
        LinearLayout listingCard;
        Button listingDelete;
        ImageView image;
        TextView listingName;
        TextView listingCategory;

        public MyFavListingHolder(@NonNull View itemView) {
            super(itemView);
            //Data
            image = itemView.findViewById(R.id.listingImage);
            listingName = itemView.findViewById(R.id.listingName);
            listingCategory = itemView.findViewById(R.id.listingCategory);

            //Action
            listingCard = itemView.findViewById(R.id.listingCard);
            listingDelete = itemView.findViewById(R.id.listingDelete);

        }
    }


    void removeFav(int id)
    {

        String url="https://digi-barter.herokuapp.com/deleteWishList?wishListId="+id;

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjReq);
    }
}
