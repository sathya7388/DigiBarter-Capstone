package com.example.digibarter.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.digibarter.BuildConfig;
import com.example.digibarter.MainActivity;
import com.example.digibarter.helpers.SharedPreferenceHelper;
import com.example.digibarter.helpers.VolleySingleton;
import com.example.digibarter.model.Product;
import com.example.digibarter.productDetail;
import com.example.digibarter.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class notificationRecycleAdapter extends RecyclerView.Adapter<notificationRecycleAdapter.homeHolder> {

    ArrayList<Product> myProducts, reqProducts;
    ArrayList<String> reqUserNames;
    ArrayList<Integer> reqUSerIDs;
    ArrayList<Integer> bartIds;
    ArrayList<Integer> reqUserRewards;
    Context context;

    public notificationRecycleAdapter(Context ct, ArrayList<Product> myProduct,ArrayList<Product> reqProduct,ArrayList<String> unames,ArrayList<Integer> reqIds, ArrayList<Integer> bids,ArrayList<Integer> reqUserRewards){
        context = ct;
        this.myProducts = myProduct;
        this.reqProducts = reqProduct;
        this.reqUserNames = unames;
        this.reqUSerIDs = reqIds;
        this.bartIds = bids;
        this.reqUserRewards = reqUserRewards;
    }
    @NonNull
    @Override
    public homeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.notification_list_item,parent,false);
        return new homeHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull homeHolder holder, final int position) {
        holder.textViewMsg.setText(reqUserNames.get(position) + " wants to barter for '" + myProducts.get(position).title + "'" + " with '"+ reqProducts.get(position).title+"'");
        holder.barterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBart(bartIds.get(position),true,position);
            }
        });
        holder.declineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateBart(bartIds.get(position),false,position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myProducts.size();
    }

    public class homeHolder extends RecyclerView.ViewHolder {
        TextView textViewMsg;
        Button barterBtn,chatBtn,declineBtn;
        public homeHolder(@NonNull View itemView) {
            super(itemView);
            textViewMsg = itemView.findViewById(R.id.textViewMsg);
            barterBtn = itemView.findViewById(R.id.accept);

            declineBtn = itemView.findViewById(R.id.decline);
        }
    }

    void updateBart(int bartId, final boolean accept, final int pos)
    {
        String url;
        if(accept)
        {
             url = "https://digi-barter.herokuapp.com/acceptBart?bartId="+bartId;
        }
        else
        {
             url = "https://digi-barter.herokuapp.com/declineBart?bartId="+bartId;
        }
        /*final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Updating...");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                      //  pDialog.hide();
                        try {
                            if(obj.getString("status").equals("success"))
                            {
                                Toast.makeText(context,"Updated...",Toast.LENGTH_LONG).show();
                                ((MainActivity)context).getNotification();
                                if(accept)
                                {
                                    updateRewards(SharedPreferenceHelper.getSharedPreferenceInt(context,"id",-1),SharedPreferenceHelper.getSharedPreferenceInt(context,"rewardPoints",0)+100);
                                    updateRewards(reqUSerIDs.get(pos),reqUserRewards.get(pos)+100);
                                }
                            }
                            else {
                                Toast.makeText(context,"Can't update...",Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                // hide the progress dialog
              // pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    void updateRewards(int uid,int points)
    {
        String url = "https://digi-barter.herokuapp.com/updateRewardPoints?userId="+uid+"&rewardPoints="+points;

        /*final ProgressDialog pDialog = new ProgressDialog(context);
        pDialog.setMessage("Updating Rewards...");
        pDialog.show();*/

        JsonObjectRequest jsonObjReq;
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject obj) {
                     //   pDialog.hide();
                        try {
                                Toast.makeText(context,"Updated...",Toast.LENGTH_LONG).show();
                                ((MainActivity)context).getNotification();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context,"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context,"Sorry, Some error occured",Toast.LENGTH_LONG).show();
                // hide the progress dialog
               // pDialog.hide();
            }
        });

        // Adding request to request queue
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjReq);
    }
}
