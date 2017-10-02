package com.terralogic.alexle.lighttransfer.controller.adapters;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.model.FlickrPicture;
import com.terralogic.alexle.lighttransfer.util.GlideApp;

import java.util.ArrayList;

/**
 * Created by alex.le on 29-Sep-17.
 */

public class DetailedFlickrPictureAdapter extends PagerAdapter {
    private Context context;
    private ArrayList<FlickrPicture> pictures;

    public DetailedFlickrPictureAdapter(Context context, ArrayList<FlickrPicture> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @Override
    public int getCount() {
        return pictures.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_pager, container, false);
        ImageView imageView = itemView.findViewById(R.id.picture_detail);
        GlideApp.with(context)
                .load(pictures.get(position).getUrl())
                .thumbnail(0.5f)
                .into(imageView);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }
}
