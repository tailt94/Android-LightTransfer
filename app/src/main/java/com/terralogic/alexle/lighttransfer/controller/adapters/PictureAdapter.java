package com.terralogic.alexle.lighttransfer.controller.adapters;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.model.Picture;
import com.terralogic.alexle.lighttransfer.util.GlideApp;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by alex.le on 23-Aug-17.
 */

public class PictureAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private Context context;
    private LinkedHashMap<String, ArrayList<Picture>> data;
    private RecyclerViewClickListener listener;

    public PictureAdapter(Context context, LinkedHashMap<String, ArrayList<Picture>> data, RecyclerViewClickListener listener) {
        this.context = context;
        this.data = data;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.header_stored_picture, parent, false);
            return new HeaderViewHolder(view);
        } else if (viewType == TYPE_ITEM) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_stored_picture, parent, false);
            return new ItemViewHolder(view);
        }
        throw new RuntimeException("No type matches " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String takenDate = (String) getData(position);

            boolean checked = true;
            ArrayList<Picture> pictures = data.get(takenDate);
            if (pictures != null) {
                for (Picture picture : pictures) {
                    if (!picture.isSelected()) {
                        checked = false;
                        break;
                    }
                }
            }

            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.title.setText(takenDate);
            headerHolder.checkBox.setChecked(checked);
        } else if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            Picture picture = (Picture) getData(position);
            if (picture.isSelected()) {
                itemHolder.iconCheck.setVisibility(View.VISIBLE);
            } else {
                itemHolder.iconCheck.setVisibility(View.INVISIBLE);
            }
            File imageFile = new File(picture.getLocation());
            GlideApp.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.image_placeholder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(itemHolder.pictureImage);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeader(position)) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        int size = 0;
        for (Map.Entry<String, ArrayList<Picture>> entry : data.entrySet()) {
            size += entry.getValue().size() + 1;
        }
        return size;
    }

    /**
     * Check if the item at specific position is a Header or not
     * @param position The position of the item in adapter data set
     */
    public boolean isHeader(int position) {
        int size = 0;
        for (Map.Entry<String, ArrayList<Picture>> entry : data.entrySet()) {
            if (size < position) {
                size += entry.getValue().size() + 1;
            } else {
                break;
            }
        }
        if (size == position) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param position The position of the item in adapter data set
     * @return String if the data at position is Header or Picture if the data is Item
     */
    public Object getData(int position) {
        int startIndex = 0;
        int size = 0;

        String key = null;
        ArrayList<Picture> value = null;

        for (Map.Entry<String, ArrayList<Picture>> entry : data.entrySet()) {
            startIndex = size;
            size += entry.getValue().size() + 1;
            if (size > position) {
                key = entry.getKey();
                value = entry.getValue();
                break;
            }
        }
        if (startIndex == position) {
            return key;
        }
        return value.get(position - (startIndex + 1));
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CheckBox checkBox;
        private TextView title;
        public HeaderViewHolder(View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.header_checkbox);
            title = itemView.findViewById(R.id.header_title);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (checkBox.isChecked()) {
                checkBox.setChecked(false);
            } else {
                checkBox.setChecked(true);
            }
            listener.onHeaderCheckBoxClick(getAdapterPosition());
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener,
            View.OnClickListener{
        private ImageView pictureImage;
        private ImageView iconCheck;

        public ItemViewHolder(View itemView) {
            super(itemView);
            pictureImage = itemView.findViewById(R.id.picture_image);
            iconCheck = itemView.findViewById(R.id.icon_check);

            pictureImage.setOnLongClickListener(this);
            pictureImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            listener.onItemLongClick(getAdapterPosition());
            return true;
        }
    }

    public interface RecyclerViewClickListener {
        void onHeaderCheckBoxClick(int position);
        void onItemClick(int position);
        void onItemLongClick(int position);
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
