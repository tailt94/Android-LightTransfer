package com.terralogic.alexle.lighttransfer.controller.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.terralogic.alexle.lighttransfer.R;
import com.terralogic.alexle.lighttransfer.controller.adapters.FlickrPictureAdapter;
import com.terralogic.alexle.lighttransfer.model.FlickrPicture;
import com.terralogic.alexle.lighttransfer.service.HttpHandler;
import com.terralogic.alexle.lighttransfer.service.Service;
import com.terralogic.alexle.lighttransfer.util.SpannedGridLayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchResultActivity extends AppCompatActivity implements FlickrPictureAdapter.PictureClickListener {
    public static final String EXTRA_PICTURES = "EXTRA_PICTURES";
    public static final String EXTRA_POS = "EXTRA_POS";

    private RecyclerView recyclerView;

    private String searchText;
    private ArrayList<FlickrPicture> pictures = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        bindViews();
        handleIntent(getIntent());
        new LoadImagesTask().execute();
    }

    private void bindViews() {
        recyclerView = (RecyclerView) findViewById(R.id.rv_flickrPicture);
    }

    private void handleIntent(Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
            searchText = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private void setupRecyclerView() {
        FlickrPictureAdapter adapter = new FlickrPictureAdapter(this, pictures, this);
        SpannedGridLayoutManager layoutManager = new SpannedGridLayoutManager(
                new SpannedGridLayoutManager.GridSpanLookup() {
                    @Override
                    public SpannedGridLayoutManager.SpanInfo getSpanInfo(int position) {
                        if (position % 13 == 0) {
                            return new SpannedGridLayoutManager.SpanInfo(3, 2);
                        } else if (position % 13 == 7 || position % 13 == 11) {
                            return new SpannedGridLayoutManager.SpanInfo(2, 2);
                        }
                        return new SpannedGridLayoutManager.SpanInfo(1, 1);
                    }
                },
                3,
                1f
        );

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onPictureClick(int position) {
        Intent intent = new Intent(this, DetailedFlickrPictureActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_PICTURES, pictures);
        bundle.putInt(EXTRA_POS, position);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    /**
     * Background task to load searched images
     */
    private class LoadImagesTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog = new ProgressDialog(SearchResultActivity.this);

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
            HttpHandler httpHandler = new HttpHandler(Service.URL_FLICKR);
            httpHandler.addParam("method", "flickr.photos.search");
            httpHandler.addParam("api_key", Service.API_KEY_FLICKR);
            httpHandler.addParam("text", searchText);
            httpHandler.addParam("format", "json");
            httpHandler.addParam("nojsoncallback", "1");

            String result = httpHandler.get();
            try {
                JSONObject json = new JSONObject(result);
                JSONArray jsonArray = json.optJSONObject("photos").optJSONArray("photo");
                for (int i = 0; i < jsonArray.length(); i++) {
                    FlickrPicture picture = new FlickrPicture(jsonArray.optJSONObject(i));
                    pictures.add(picture);
                }
            } catch (JSONException e) {
                Log.e("SearchResultActivity", "Json mapping error");
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
