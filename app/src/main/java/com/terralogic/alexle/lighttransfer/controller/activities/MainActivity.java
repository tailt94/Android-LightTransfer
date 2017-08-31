package com.terralogic.alexle.lighttransfer.controller.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.controller.adapters.PictureAdapter;
import com.terralogic.alexle.lighttransfer.controller.dialogs.DatePickerDialogFragment;
import com.terralogic.alexle.lighttransfer.model.Picture;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PictureAdapter.OnImageCountChangeListener,
        DatePickerDialog.OnDateSetListener{
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final String BUNDLE_BACK_BUTTON_STATE = "BUNDLE_BACK_BUTTON_STATE";
    private static final String BUNDLE_TOOLBAR_TITLE = "BUNDLE_TOOLBAR_TITLE";
    private static final String BUNDLE_PICTURE_LIST = "BUNDLE_PICTURE_LIST";
    private static final String BUNDLE_FILTERED_PICTURE_LIST = "BUNDLE_FILTERED_PICTURE_LIST";
    private static final String BUNDLE_IS_FILTER_MODE = "BUNDLE_IS_FILTER_MODE";

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private RecyclerView rvStoredPictures;
    private PictureAdapter rvAdapter;

    private ArrayList<Picture> pictures = new ArrayList<>();
    private ArrayList<Picture> filteredPictures = new ArrayList<>();
    private boolean isBackButtonEnabled = false;
    private boolean isFilterMode = false;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupListeners();

        if (savedInstanceState != null) {
            showBackButton(savedInstanceState.getBoolean(BUNDLE_BACK_BUTTON_STATE));
            getSupportActionBar().setTitle(savedInstanceState.getString(BUNDLE_TOOLBAR_TITLE));
            pictures = (ArrayList<Picture>) savedInstanceState.getSerializable(BUNDLE_PICTURE_LIST);
            filteredPictures = (ArrayList<Picture>) savedInstanceState.getSerializable(BUNDLE_FILTERED_PICTURE_LIST);
            isFilterMode = savedInstanceState.getBoolean(BUNDLE_IS_FILTER_MODE);
            setupRecyclerView();
            if (isFilterMode) {
                rvAdapter.setPictureList(filteredPictures);
            }
        } else {
            loadImages();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_BACK_BUTTON_STATE, isBackButtonEnabled);
        outState.putString(BUNDLE_TOOLBAR_TITLE, getSupportActionBar().getTitle().toString());
        outState.putSerializable(BUNDLE_PICTURE_LIST, pictures);
        outState.putSerializable(BUNDLE_FILTERED_PICTURE_LIST, filteredPictures);
        outState.putBoolean(BUNDLE_IS_FILTER_MODE, isFilterMode);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView)) {
            drawer.closeDrawer(navigationView);
        } else {
            if (isFilterMode) {
                isFilterMode = false;
            }
            rvAdapter.unselectAllImages();
            showBackButton(false);
            getSupportActionBar().setTitle(R.string.main_activity_title);
            rvAdapter.setPictureList(pictures);
            filteredPictures.clear();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                DatePickerDialogFragment datePickerDialog = new DatePickerDialogFragment();
                datePickerDialog.show(getSupportFragmentManager(), "DatePickerDialogFragment");
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onImageCountChange(int selectedImageCount) {
        if (selectedImageCount > 0) {
            showBackButton(true);
            getSupportActionBar().setTitle(Integer.toString(selectedImageCount));
        } else if (selectedImageCount == 0) {
            showBackButton(false);
            getSupportActionBar().setTitle(R.string.main_activity_title);
        }
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        isFilterMode = true;

        String filteredDate = new StringBuilder().append(day).append("/")
                .append(month + 1).append("/")
                .append(year).toString();
        //Show filtered date as toolbar title
        getSupportActionBar().setTitle(filteredDate);

        //Show back button
        showBackButton(true);

        //Reset all image state to unselected
        rvAdapter.unselectAllImages();

        //Reset filtered pictures
        filteredPictures.clear();

        //Show filtered images
        for (Picture picture : pictures) {
            if (picture.toLocalDate().equals(filteredDate)) {
                filteredPictures.add(picture);
            }
        }
        rvAdapter.setPictureList(filteredPictures);
    }

    private void showBackButton(boolean enable) {
        if (enable) {
            // Remove hamburger
            drawerToggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            drawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

        } else {
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            drawerToggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            drawerToggle.setToolbarNavigationClickListener(null);
        }
        isBackButtonEnabled = enable;
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
        rvAdapter = new PictureAdapter(this, pictures, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);

        rvStoredPictures.setAdapter(rvAdapter);
        rvStoredPictures.setLayoutManager(layoutManager);
        rvStoredPictures.addItemDecoration(new PictureAdapter.GridSpacingItemDecoration(2, 15,true));
    }

    /**
     * Load all stored images from device
     */
    private void loadImages() {
        //If target SDK is 23 or higher, this app will request permission at runtime
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handleReadExternalStoragePermission();
        } else {
            new LoadImagesTask().execute();
        }
    }

    @TargetApi(23)
    private void handleReadExternalStoragePermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else { //The permission is already granted
            new LoadImagesTask().execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new LoadImagesTask().execute();
                }
                break;
            default:
                break;
        }
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
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DATA,
            };

            Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Picture picture = new Picture();

                    int columnNameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    int columnTakenDateIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
                    int columnLocationIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

                    String name = cursor.getString(columnNameIndex);
                    long takenDate = cursor.getLong(columnTakenDateIndex);
                    String location = cursor.getString(columnLocationIndex);

                    picture.setName(name);
                    picture.setTakenDate(new Date(takenDate));
                    picture.setLocation(location);

                    pictures.add(picture);
                }
                cursor.close();
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