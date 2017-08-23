package com.terralogic.alexle.lighttransfer.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.model.Picture;

import java.util.List;

/**
 * Created by alex.le on 23-Aug-17.
 */

public class PicturesAdapter extends RecyclerView.Adapter<PicturesAdapter.StoredPictureViewHolder> {
    private List<Picture> pictures;
    private Context context;

    public PicturesAdapter(Context context, List<Picture> pictures) {
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
        //TODO hien thi thong tin hinh anh
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
}
