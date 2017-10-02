package com.terralogic.alexle.lighttransfer.controller.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.controller.adapters.LocalPictureAdapter;
import com.terralogic.alexle.lighttransfer.controller.dialogs.SocialNetworkChooserDialogFragment;
import com.terralogic.alexle.lighttransfer.model.LocalPicture;
import com.terralogic.alexle.lighttransfer.util.SpannedGridLayoutManager;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements LocalPictureAdapter.RecyclerViewClickListener,
        DialogInterface.OnClickListener {
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 100;
    private static final String BUNDLE_SELECTED_IMAGE_COUNT= "BUNDLE_SELECTED_IMAGE_COUNT";
    private static final String BUNDLE_RECYCLER_VIEW_DATA = "BUNDLE_RECYCLER_VIEW_DATA";

    private Toolbar toolbar;
    private Menu menu;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private RecyclerView rvStoredPictures;
    private LocalPictureAdapter rvAdapter;

    private LinkedHashMap<String, ArrayList<LocalPicture>> rvData = new LinkedHashMap<>();

    private int selectedImageCount = 0;
    private boolean isBackButtonEnabled = false;

    //Facebook
    private CallbackManager facebookCallbackManager;
    private ShareDialog facebookShareDialog;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        setupListeners();

        //Facebook preparation
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookShareDialog = new ShareDialog(this);

        if (savedInstanceState != null) {
            selectedImageCount = savedInstanceState.getInt(BUNDLE_SELECTED_IMAGE_COUNT);
            rvData = (LinkedHashMap<String, ArrayList<LocalPicture>>) savedInstanceState.getSerializable(BUNDLE_RECYCLER_VIEW_DATA);
            setupRecyclerView();
        } else {
            loadImages();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_SELECTED_IMAGE_COUNT, selectedImageCount);
        outState.putSerializable(BUNDLE_RECYCLER_VIEW_DATA, rvData);
        super.onSaveInstanceState(outState);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.menu = menu;
        notifyToolbarLayoutChanged();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(this, SearchResultActivity.class)));
        searchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int position) {
        switch (position) {
            case 0:
                List<Uri> shareUris = getSelectedPicturesUri();
                List<SharePhoto> sharePhotos = new ArrayList<>();
                for (Uri uri : shareUris) {
                    SharePhoto photo = new SharePhoto.Builder()
                            .setImageUrl(uri)
                            .build();
                    sharePhotos.add(photo);
                }

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhotos(sharePhotos)
                        .build();
                facebookShareDialog.show(content);
                break;
            case 1:
                TweetComposer.Builder builder = new TweetComposer.Builder(this)
                        .image(getSelectedPicturesUri().get(0));
                builder.show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;
            case R.id.action_share:
                SocialNetworkChooserDialogFragment dialog = new SocialNetworkChooserDialogFragment();
                dialog.show(getSupportFragmentManager(), "SocialNetworkChooserDialogFragment");
                return true;
            case R.id.action_take_picture:
                dispatchTakePictureIntent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //Reload all images after taking a picture
            rvData.clear();
            loadImages();
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

    @Override
    public void onHeaderCheckBoxClick(int position) {
        String takenDate = (String) rvAdapter.getData(position);
        changeMultiplePictureState(takenDate, !isAllPictureSelected(takenDate));
        vibrateDevice(50);
    }

    @Override
    public void onItemClick(int position) {
        LocalPicture picture = (LocalPicture) rvAdapter.getData(position);
        boolean isSelected = !picture.isSelected();
        if (isSelected) {
            selectedImageCount++;
        } else {
            selectedImageCount--;
        }
        picture.setSelected(isSelected);
        notifyToolbarLayoutChanged();
        rvAdapter.notifyDataSetChanged();

        vibrateDevice(50);
    }

    @Override
    public void onItemLongClick(int position) {
        LocalPicture picture = (LocalPicture) rvAdapter.getData(position);
        File imageFile = new File(picture.getLocation());
        Uri imageUri = FileProvider.getUriForFile(this,
                getApplicationContext().getPackageName() + ".provider",
                imageFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private List<Uri> getSelectedPicturesUri() {
        List<Uri> uris = new ArrayList<>();
        for (Map.Entry<String, ArrayList<LocalPicture>> entry : rvData.entrySet()) {
            ArrayList<LocalPicture> pictures = entry.getValue();
            for (LocalPicture picture : pictures) {
                if (picture.isSelected()) {
                    uris.add(Uri.fromFile(new File(picture.getLocation())));
                }
            }
        }
        return uris;
    }

    /**
     * Check if all picture taken at the same date is selected or not
     */
    private boolean isAllPictureSelected(String takenDate) {
        ArrayList<LocalPicture> pictures = rvData.get(takenDate);
        if (pictures != null) {
            for (LocalPicture picture : pictures) {
                if (!picture.isSelected()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    private void changeMultiplePictureState(String takenDate, boolean selected) {
        ArrayList<LocalPicture> pictures = rvData.get(takenDate);
        if (pictures != null) {
            for (LocalPicture picture : pictures) {
                boolean isSelected = picture.isSelected();
                if (selected != isSelected) {
                    picture.setSelected(selected);
                    if (selected) {
                        selectedImageCount++;
                    } else {
                        selectedImageCount--;
                    }
                    notifyToolbarLayoutChanged();
                    rvAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    /**
     * Change toolbar layout when select or deselect an image
     */
    private void notifyToolbarLayoutChanged() {
        if (selectedImageCount > 0) {
            showBackButton(true);
            setMenuItemVisible(R.id.action_share, true);
            getSupportActionBar().setTitle(Integer.toString(selectedImageCount));
        } else if (selectedImageCount == 0) {
            showBackButton(false);
            setMenuItemVisible(R.id.action_share, false);
            getSupportActionBar().setTitle(R.string.main_activity_title);
        }
    }

    /**
     * Reset all images selected state to false
     */
    private void resetAllImagesState() {
        selectedImageCount = 0;
        for (Map.Entry<String, ArrayList<LocalPicture>> entry : rvData.entrySet()) {
            ArrayList<LocalPicture> pictures = entry.getValue();
            for (LocalPicture picture : pictures) {
                picture.setSelected(false);
            }
        }
    }

    /**
     * Open a camera app to take a picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setMenuItemVisible(int menuItemId, boolean visible) {
        MenuItem item = menu.findItem(menuItemId);
        item.setVisible(visible);
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
                    vibrateDevice(50);
                    resetAllImagesState();
                    notifyToolbarLayoutChanged();
                    rvAdapter.notifyDataSetChanged();
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
        rvStoredPictures = (RecyclerView) findViewById(R.id.rv_local_picture);
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
        rvAdapter = new LocalPictureAdapter(this, rvData, this);
        rvStoredPictures.setAdapter(rvAdapter);

        /*GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (rvAdapter.isHeader(position)) {
                    return 3;
                }
                if (rvAdapter.isHeader(position - 1)) {
                    return 2;
                }
                return 1;
            }
        });*/

        SpannedGridLayoutManager layoutManager = new SpannedGridLayoutManager(
                new SpannedGridLayoutManager.GridSpanLookup() {
                    @Override
                    public SpannedGridLayoutManager.SpanInfo getSpanInfo(int position) {
                        if (rvAdapter.isHeader(position)) {
                            return new SpannedGridLayoutManager.SpanInfo(3, 1);
                        } else {
                            int lastHeaderPosition = position;
                            while (!rvAdapter.isHeader(lastHeaderPosition)) {
                                lastHeaderPosition--;
                            }
                            int relativePosition = position - lastHeaderPosition - 1;
                            if (relativePosition % 13 == 0) {
                                return new SpannedGridLayoutManager.SpanInfo(3, 2);
                            } else if (relativePosition % 13 == 7 || relativePosition % 13 == 11) {
                                return new SpannedGridLayoutManager.SpanInfo(2, 2);
                            }
                        }
                        return new SpannedGridLayoutManager.SpanInfo(1, 1);
                    }
                },
                3,
                1f
        );

        /*FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setAlignItems(AlignItems.STRETCH);*/

        rvStoredPictures.setLayoutManager(layoutManager);
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

    private void vibrateDevice(long milliseconds) {
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(milliseconds);
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
            List<LocalPicture> pictures = new ArrayList<>();

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
                    LocalPicture picture = new LocalPicture();

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
                Collections.reverse(pictures);

                for (LocalPicture picture : pictures) {
                    String takenDate = picture.toLocalDate();

                    if (!rvData.containsKey(takenDate)) {
                        rvData.put(takenDate, new ArrayList<LocalPicture>());
                    }
                    rvData.get(takenDate).add(picture);
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
