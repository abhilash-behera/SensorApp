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
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    DatabaseHelper databaseHelper;
    CoordinatorLayout coordinatorLayout;


    //model object for our list data
    private List<FavouritesList> favouritesLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        // checking internate connection..
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.coordinatorLayout);

        ConnectivityManager cm = (ConnectivityManager) getSystemService(FavouritesActivity.this.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo datac = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null & datac != null)
                && (wifi.isConnected() | datac.isConnected())) {
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Check Your Internet Connection!!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view)
                        {
                            finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);

            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }

        //setting Action Bar..
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Favourites List");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);



        //initializing views
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        favouritesLists = new ArrayList<>();

        databaseHelper=new DatabaseHelper(this);


       //Set Base Adapter
        adapter = new FavouritesAdapter(databaseHelper.getAllFabList(),FavouritesActivity.this);
        recyclerView.setAdapter(adapter);

        Log.e("list", String.valueOf(databaseHelper.getAllFabList()));
        Log.e("list", String.valueOf(databaseHelper.getAllFabList()));


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


}
