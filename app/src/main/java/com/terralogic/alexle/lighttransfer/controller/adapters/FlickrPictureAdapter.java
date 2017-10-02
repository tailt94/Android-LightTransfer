package com.terralogic.alexle.lighttransfer.controller.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.model.FlickrPicture;
import com.terralogic.alexle.lighttransfer.util.GlideApp;

import java.util.ArrayList;

/**
 * Created by alex.le on 26-Sep-17.
 */

public class FlickrPictureAdapter extends RecyclerView.Adapter<FlickrPictureAdapter.PictureViewHolder> {
    private Context context;
    private ArrayList<FlickrPicture> pictures;
    private PictureClickListener listener;

    public FlickrPictureAdapter(Context context, ArrayList<FlickrPicture> pictures, PictureClickListener listener) {
        this.context = context;
        this.pictures = pictures;
        this.listener = listener;
    }

    @Override
    public PictureViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_flickr_picture, parent, false);
        return new PictureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PictureViewHolder holder, int position) {
        FlickrPicture photo = pictures.get(position);
        GlideApp.with(context)
                .load(photo.getUrl())
                .placeholder(R.drawable.image_placeholder)
                .centerCrop()
                .into(holder.image);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    public class PictureViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        public PictureViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.picture);

            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onPictureClick(getAdapterPosition());
        }
    }

    public interface PictureClickListener {
        void onPictureClick(int position);
    }
}
