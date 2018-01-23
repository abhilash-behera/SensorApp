package com.sensorapp;


import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class FavouritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FavouritesAdapter favouritesAdapter;
    private DatabaseHelper databaseHelper;
    private ArrayList<FavouriteData> favouriteDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        initializeViews();
        checkInternetConnection();
    }

    private void initializeViews() {
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(FavouritesActivity.this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        getSupportActionBar().setTitle("Favourite Routes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void checkInternetConnection() {
        if(Utils.isNetworkAvailable(FavouritesActivity.this)){
            continueExecution();
        }else{
            Snackbar.make(recyclerView,"No internet connection available",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkInternetConnection();
                        }
                    })
                    .setActionTextColor(Color.RED)
                    .show();
        }
    }

    private void continueExecution() {
        databaseHelper=new DatabaseHelper(FavouritesActivity.this);
        favouriteDataList=databaseHelper.getFavouriteData();
        favouritesAdapter=new FavouritesAdapter(favouriteDataList,FavouritesActivity.this);
        recyclerView.setAdapter(favouritesAdapter);
        recyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
