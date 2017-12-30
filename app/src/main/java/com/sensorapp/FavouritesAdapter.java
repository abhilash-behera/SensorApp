package com.sensorapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class FavouritesAdapter extends RecyclerView.Adapter<FavouritesAdapter.MyViewHolder> {

    private ArrayList<FavouritesList> favouritesLists;
    private Context mContext;
    FavouritesList myList;
    DatabaseHelper dbDatabaseHelper;

    public FavouritesAdapter(ArrayList<FavouritesList> fabFavouritesLists, Context mContext) {
        this.favouritesLists = fabFavouritesLists;
        this.mContext = mContext;
         Log.e("we are in constructer","we are in cunstrocter");

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infleating the favourites item on layout..
        Log.e("we are in constructer","inflate list");
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.favourites_item, parent, false);
        return new MyViewHolder(v);

    }

    //setting data to holder
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {

        myList = favouritesLists.get(position); //getting position
        Log.e("MyList Adapter", String.valueOf(myList));
        String str=myList.getSource();
        holder.tvSource.setText(str);
        String str1=myList.getDestination();
        holder.tvDestination.setText(str1);

        //redirecting to selected card view data..
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(mContext,FavouritesViewActivity.class);
                intent.putExtra("source",favouritesLists.get(position).getSource());
                intent.putExtra("destination",favouritesLists.get(position).getDestination());
                intent.putExtra("avgNoise",favouritesLists.get(position).getAverageNoise());
                intent.putExtra("algorithm",favouritesLists.get(position).getAlgorithm());
                mContext.startActivity(intent);
            }
        });

        //open dilog for deleting the list data..
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                final AlertDialog.Builder builder=new AlertDialog.Builder(mContext,R.style.CustomDialog);
                builder.setTitle("Delete");
                builder.setCancelable(false);
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    dbDatabaseHelper=new DatabaseHelper(mContext);
                        dbDatabaseHelper.removeSingleContact(favouritesLists.get(position).getId());
                        Log.e("id ",favouritesLists.get(position).getId());
                        favouritesLists.remove(position);
                        notifyDataSetChanged();

                    }
                });
                builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();

                    }
                });
                builder.show();
                return false;
            }
        });

    }
    //getting the list size for iteration..

    @Override
    public int getItemCount() {
        return favouritesLists.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tvSource;
        public TextView tvDestination;
        public TextView tvDistance;
        public TextView tvAvgNoise;


        //find out the views..
        public MyViewHolder(View itemView) {
            super(itemView);

            tvSource=(TextView)itemView.findViewById(R.id.tvSource);
            tvDestination=(TextView)itemView.findViewById(R.id.tvDestination);
            tvDistance=(TextView)itemView.findViewById(R.id.tvDistance);
            tvAvgNoise=(TextView)itemView.findViewById(R.id.tvAvgNoise);

        }


    }
}
