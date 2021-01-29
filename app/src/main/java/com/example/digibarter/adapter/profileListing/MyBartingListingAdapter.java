package com.example.digibarter.adapter.profileListing;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.example.digibarter.R;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;
import com.example.digibarter.productDetail;

import org.json.JSONObject;

import java.util.ArrayList;

public class MyBartingListingAdapter extends RecyclerView.Adapter<MyBartingListingAdapter.MyBartingListingHolder> {
    Context context;
    ArrayList<Product> productArrayList;
    public MyBartingListingAdapter(Context context, ArrayList<Product> list) {
        this.productArrayList = list;
        this.context = context;
    }

    @NonNull
    @Override
    public MyBartingListingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.listing_adapter, parent, false);
        return new MyBartingListingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyBartingListingHolder holder, final int position) {
        if(productArrayList.get(position).images.length == 0)
        {
            Glide.with(context).load("https://enhancedperformanceinc.com/wp-content/uploads/2017/10/product-dummy.png").into(holder.image);
        }
        else {
            Glide.with(context).load(productArrayList.get(position).images[0]).into(holder.image);
        }
        holder.listingName.setText(productArrayList.get(position).title);
        holder.listingCategory.setText(productArrayList.get(position).desc);
        holder.listingDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteMyProduct(productArrayList.get(position).id);
                productArrayList.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }

    public class MyBartingListingHolder extends RecyclerView.ViewHolder {
        LinearLayout listingCard;
        ImageView image;
        TextView listingName;
        TextView listingCategory;
        Button listingDelete;

        public MyBartingListingHolder(@NonNull View itemView) {
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

    void deleteMyProduct(int id)
    {

        String url="https://digi-barter.herokuapp.com/deleteProduct?productId="+id;

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
