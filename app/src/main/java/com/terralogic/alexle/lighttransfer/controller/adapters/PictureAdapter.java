package com.terralogic.alexle.lighttransfer.controller.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.model.Picture;
import com.terralogic.alexle.lighttransfer.util.GlideApp;

import java.io.File;
import java.util.List;

/**
 * Created by alex.le on 23-Aug-17.
 */

public class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.StoredPictureViewHolder> {
    private Context context;
    private List<Picture> pictures;

    public PictureAdapter(Context context, List<Picture> pictures) {
        this.context = context;
        this.pictures = pictures;
    }

    @Override

    public StoredPictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_stored_picture, parent, false);
        return new StoredPictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StoredPictureViewHolder holder, int position) {
        Picture picture = pictures.get(position);
        holder.pictureName.setText(picture.getName());
        File imageFile = new File(picture.getUrl());
        GlideApp.with(context)
                .load(imageFile)
                .placeholder(R.drawable.image_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(holder.pictureImage);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    public class StoredPictureViewHolder extends RecyclerView.ViewHolder {
        private ImageView pictureImage;
        private TextView pictureName;
        public StoredPictureViewHolder(View itemView) {
            super(itemView);
            pictureImage = itemView.findViewById(R.id.picture_image);
            pictureName = itemView.findViewById(R.id.picture_name);
        }
    }

    public static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }
}
