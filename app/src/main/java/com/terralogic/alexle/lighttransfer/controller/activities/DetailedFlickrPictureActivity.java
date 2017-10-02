package com.terralogic.alexle.lighttransfer.controller.activities;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.controller.adapters.DetailedFlickrPictureAdapter;
import com.terralogic.alexle.lighttransfer.model.FlickrPicture;

import java.util.ArrayList;

public class DetailedFlickrPictureActivity extends AppCompatActivity {
    private ViewPager viewPager;

    private ArrayList<FlickrPicture> pictures;
    private int chosenPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_flickr_picture);
        handleIntent();
        bindViews();
        setupViewPager();
    }

    @SuppressWarnings("unchecked")
    private void handleIntent() {
        Bundle bundle = getIntent().getExtras();
        pictures = (ArrayList<FlickrPicture>) bundle.getSerializable(SearchResultActivity.EXTRA_PICTURES);
        chosenPos = bundle.getInt(SearchResultActivity.EXTRA_POS);
    }

    private void bindViews() {
        viewPager = (ViewPager) findViewById(R.id.view_pager);
    }

    private void setupViewPager() {
        DetailedFlickrPictureAdapter adapter = new DetailedFlickrPictureAdapter(this, pictures);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(chosenPos);
    }
}
