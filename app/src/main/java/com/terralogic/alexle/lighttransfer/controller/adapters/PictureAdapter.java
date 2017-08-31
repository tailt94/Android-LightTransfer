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
    private int selectedImageCount = 0;
    private OnImageCountChangeListener mListener;

    public PictureAdapter(Context context, List<Picture> pictures, OnImageCountChangeListener listener) {
        this.context = context;
        this.pictures = pictures;
        this.mListener = listener;
        updateSelectedImageCount();
    }

    public void setPictureList(List<Picture> pictures) {
        this.pictures = pictures;
        updateSelectedImageCount();
        notifyDataSetChanged();
    }

    @Override
    public StoredPictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_stored_picture, parent, false);
        return new StoredPictureViewHolder(view, new StoredPictureViewHolder.OnImageStateChangeListener() {
            @Override
            public void onImageStateChange(int position, boolean isSelected) {
                if (isSelected) {
                    selectedImageCount++;
                } else {
                    selectedImageCount--;
                }
                pictures.get(position).setSelected(isSelected);
                mListener.onImageCountChange(selectedImageCount);
            }
        });
    }

    @Override
    public void onBindViewHolder(StoredPictureViewHolder holder, int position) {
        Picture picture = pictures.get(position);
        holder.pictureName.setText(picture.getName());
        if (picture.isSelected()) {
            holder.pictureLayout.setBackgroundResource(R.color.stored_picture_background_color_selected);
        } else {
            holder.pictureLayout.setBackgroundResource(R.color.stored_picture_background_color_unselected);
        }
        holder.setSelected(picture.isSelected());
        File imageFile = new File(picture.getLocation());
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

    /**
     * Reset all images selected state to false
     */
    public void unselectAllImages() {
        selectedImageCount = 0;
        for (Picture picture : pictures) {
            picture.setSelected(false);
        }
        notifyDataSetChanged();
    }

    private void updateSelectedImageCount() {
        selectedImageCount = 0;
        for (Picture picture : pictures) {
            if (picture.isSelected()) {
                selectedImageCount++;
            }
        }
    }

    public interface OnImageCountChangeListener {
        void onImageCountChange(int selectedImageCount);
    }

    public static class StoredPictureViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener{
        private ViewGroup pictureLayout;
        private ImageView pictureImage;
        private TextView pictureName;

        private boolean isSelected = false;
        private OnImageStateChangeListener mListener;


        public StoredPictureViewHolder(View itemView, OnImageStateChangeListener listener) {
            super(itemView);
            pictureLayout = itemView.findViewById(R.id.picture_layout);
            pictureImage = itemView.findViewById(R.id.picture_image);
            pictureName = itemView.findViewById(R.id.picture_name);

            mListener = listener;

            pictureImage.setOnLongClickListener(this);
            pictureImage.setOnClickListener(this);
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        @Override
        public boolean onLongClick(View view) {
            changeImageState();
            return true;
        }

        @Override
        public void onClick(View view) {
            if (isSelected) {
                changeImageState();
            }
        }

        private void changeImageState() {
            isSelected = !isSelected;
            mListener.onImageStateChange(getAdapterPosition(), isSelected);
            if (isSelected) {
                pictureLayout.setBackgroundResource(R.color.stored_picture_background_color_selected);
            } else {
                pictureLayout.setBackgroundResource(R.color.stored_picture_background_color_unselected);
            }
        }

        interface OnImageStateChangeListener{
            void onImageStateChange(int position, boolean isSelected);
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
