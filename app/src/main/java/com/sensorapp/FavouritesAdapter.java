package com.sensorapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;


public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.MyViewHolder> {

    private ArrayList<FavouriteData> favouritesList;
    private Context mContext;
    public static final String FAVOURITE_ROUTE_ID="favourite_route_id";

    public FavouritesAdapter(ArrayList<FavouriteData> favouritesList, Context mContext) {
        this.favouritesList = favouritesList;
        this.mContext = mContext;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView txtSource;
        public TextView txtDestination;
        public TextView txtAlgorithm;
        public TextView txtOptions;
        public RelativeLayout relativeLayout;
        public TextView txtType;

        //find out the views..
        public MyViewHolder(View itemView) {
            super(itemView);
            txtSource=(TextView)itemView.findViewById(R.id.txtSource);
            txtDestination=(TextView)itemView.findViewById(R.id.txtDestination);
            txtAlgorithm=(TextView)itemView.findViewById(R.id.txtAlgorithm);
            txtOptions=(TextView)itemView.findViewById(R.id.textViewOptions);
            relativeLayout=(RelativeLayout)itemView.findViewById(R.id.relativeLayout);
            txtType=(TextView)itemView.findViewById(R.id.txtType);
        }

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflating the favourites item on layout..
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourites_item, parent, false);
        return new MyViewHolder(v);
    }

    //setting data to holder
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final FavouriteData favouriteData= favouritesList.get(position); //getting position

        if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_NOISE)){
            holder.txtType.setText("Noise Measurements");
            holder.txtType.setBackgroundColor(0x99ff0000);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_WIFI)){
            holder.txtType.setText("Wifi Measurements");
            holder.txtType.setBackgroundColor(0x9900ff00);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_MOBILE_DATA)){
            holder.txtType.setText("Mobile Data Measurements");
            holder.txtType.setBackgroundColor(0x990000ff);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_GPS)){
            holder.txtType.setText("GPS Data measurements");
            holder.txtType.setBackgroundColor(0x99ffff00);
        }


        holder.txtSource.setText(favouriteData.getSource());
        holder.txtDestination.setText(favouriteData.getDestination());
        holder.txtAlgorithm.setText(favouriteData.getAlgorithm());
        holder.txtOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //creating a popup menu
                PopupMenu popup = new PopupMenu(mContext, holder.txtOptions);
                //inflating menu from xml resource
                popup.inflate(R.menu.favourites_item_menu);
                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                DatabaseHelper databaseHelper=new DatabaseHelper(mContext);
                                if(databaseHelper.deleteFavValue(favouriteData.getId())){
                                    favouritesList.remove(position);
                                    notifyDataSetChanged();
                                    Toasty.success(mContext,"Successfully deleted favourite route", Toast.LENGTH_LONG).show();
                                }else{
                                    Toasty.error(mContext,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                                }
                                break;
                        }
                        return false;
                    }
                });
                //displaying the popup
                popup.show();
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,FavouriteRouteViewActivity.class);
                intent.putExtra(FAVOURITE_ROUTE_ID,position+1);
                mContext.startActivity(intent);
            }
        });
    }


    //getting the list size for iteration..
    @Override
    public int getItemCount() {
        return favouritesList.size();
    }
}
