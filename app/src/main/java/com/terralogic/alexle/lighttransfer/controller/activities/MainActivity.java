package com.terralogic.alexle.lighttransfer.controller.activities;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.controller.adapters.PictureAdapter;
import com.terralogic.alexle.lighttransfer.model.Picture;
import com.terralogic.alexle.lighttransfer.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private RecyclerView rvStoredPictures;
    private PictureAdapter rvAdapter;

    List<Picture> pictures = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupListeners();
        new LoadImagesTask().execute();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawer(navigationView);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void bindViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        rvStoredPictures = (RecyclerView) findViewById(R.id.rv_stored_pictures);
    }

    private void setupListeners() {
        drawerToggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                drawer.closeDrawers();
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        rvAdapter = new PictureAdapter(this, pictures);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        rvStoredPictures.setAdapter(rvAdapter);
        rvStoredPictures.setHasFixedSize(true);
        rvStoredPictures.setLayoutManager(layoutManager);
        rvStoredPictures.addItemDecoration(new PictureAdapter.GridSpacingItemDecoration(2, 15,true));
    }

    /**
     * Background task to load all stored images
     */
    private class LoadImagesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Loading...");
            progressDialog.setMessage("Please wait for a moment");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File dir = new File(Utils.Url.PICTURES);
            if (dir.isDirectory()) {
                String[] fileNames = dir.list();
                if (fileNames != null) {
                    for (String fileName : fileNames) {
                        pictures.add(new Picture(fileName, dir.getPath() + "/" + fileName));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            setupRecyclerView();
        }
    }
}
