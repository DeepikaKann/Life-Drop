package com.amiecart.lifedrop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amiecart.adapter.SearchAdapter;
import com.amiecart.util.API;
import com.amiecart.util.Constant;
import com.amiecart.util.Events;
import com.amiecart.item.ItemPlaceList;
import com.example.placefinderapp.R;
import com.amiecart.util.GlobalBus;
import com.amiecart.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class NearSearchActivity extends AppCompatActivity {

    ArrayList<ItemPlaceList> mListItem;
    public RecyclerView recyclerView;
    SearchAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout lyt_not_found;
    String search_dis, search_cat;
    LinearLayout adLayout;
    TextView no_fav;
    int j = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        JsonUtils.setStatusBarGradiant(NearSearchActivity.this);
        toolbar.setTitle(R.string.search_place);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        GlobalBus.getBus().register(this);

        Intent intent = getIntent();
        search_cat = intent.getStringExtra("CatId");
        search_dis = intent.getStringExtra("DisId");

        mListItem = new ArrayList<>();

        lyt_not_found = findViewById(R.id.lyt_not_found);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.vertical_courses_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(NearSearchActivity.this, 1));
        recyclerView.setFocusable(false);
        adLayout = findViewById(R.id.adLayout);
        if (Constant.SAVE_BANNER_TYPE.equals("admob")) {
            JsonUtils.ShowBannerAds(NearSearchActivity.this, adLayout);
        } else {
            JsonUtils.ShowBannerAdsFb(adLayout, NearSearchActivity.this);
        }
        no_fav = findViewById(R.id.no_fav);

        if (Constant.USER_LATITUDE == null && Constant.USER_LONGITUDE == null) {
            Toast.makeText(NearSearchActivity.this, getString(R.string.location_permission), Toast.LENGTH_SHORT).show();
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "get_nearby_place");
            jsObj.addProperty("user_id", MyApplication.getAppInstance().getUserId());
            jsObj.addProperty("cat_id", search_cat);
            jsObj.addProperty("user_lat", "0");
            jsObj.addProperty("user_long", "0");
            jsObj.addProperty("distance_limit", search_dis);
            if (JsonUtils.isNetworkAvailable(NearSearchActivity.this)) {
                new getNearSearchData(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            }
        } else {
            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API());
            jsObj.addProperty("method_name", "get_nearby_place");
            jsObj.addProperty("cat_id", search_cat);
            jsObj.addProperty("user_lat", Constant.USER_LATITUDE);
            jsObj.addProperty("user_long", Constant.USER_LONGITUDE);
            jsObj.addProperty("distance_limit", search_dis);
            if (JsonUtils.isNetworkAvailable(NearSearchActivity.this)) {
                new getNearSearchData(API.toBase64(jsObj.toString())).execute(Constant.API_URL);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class getNearSearchData extends AsyncTask<String, Void, String> {

        String base64;

        private getNearSearchData(String base64) {
            this.base64 = base64;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress(true);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0], base64);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            showProgress(false);
            if (null == result || result.length() == 0) {
                lyt_not_found.setVisibility(View.VISIBLE);
            } else {
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.CATEGORY_ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        ItemPlaceList objItem = new ItemPlaceList();
                        objItem.setPlaceId(objJson.getString(Constant.LISTING_H_ID));
                        objItem.setPlaceCatId(objJson.getString(Constant.LISTING_H_CAT_ID));
                        objItem.setPlaceName(objJson.getString(Constant.LISTING_H_NAME));
                        objItem.setPlaceImage(objJson.getString(Constant.LISTING_H_IMAGE));
                        objItem.setPlaceVideo(objJson.getString(Constant.LISTING_H_VIDEO));
                        objItem.setPlaceDescription(objJson.getString(Constant.LISTING_H_DES));
                        objItem.setPlaceAddress(objJson.getString(Constant.LISTING_H_ADDRESS));
                        objItem.setPlaceEmail(objJson.getString(Constant.LISTING_H_EMAIL));
                        objItem.setPlacePhone(objJson.getString(Constant.LISTING_H_PHONE));
                        objItem.setPlaceWebsite(objJson.getString(Constant.LISTING_H_WEBSITE));
                        objItem.setPlaceLatitude(objJson.getString(Constant.LISTING_H_MAP_LATITUDE));
                        objItem.setPlaceLongitude(objJson.getString(Constant.LISTING_H_MAP_LONGITUDE));
                        objItem.setPlaceRateAvg(objJson.getString(Constant.LISTING_H_RATING_AVG));
                        objItem.setPlaceRateTotal(objJson.getString(Constant.LISTING_H_RATING_TOTAL));
                        if (Constant.SAVE_ADS_NATIVE_ON_OFF.equals("true")) {
                            if (j % Integer.parseInt(Constant.SAVE_NATIVE_CLICK_OTHER) == 0) {
                                mListItem.add(null);
                                j++;
                            }
                        }
                        mListItem.add(objItem);
                        j++;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                displayData();
            }
        }
    }

    private void displayData() {
        adapter = new SearchAdapter(NearSearchActivity.this, mListItem);
        recyclerView.setAdapter(adapter);

        if (adapter.getItemCount() == 0) {
            lyt_not_found.setVisibility(View.VISIBLE);
            no_fav.setText(getString(R.string.no_near));
        } else {
            lyt_not_found.setVisibility(View.GONE);
        }
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            lyt_not_found.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GlobalBus.getBus().unregister(this);
    }

    @Subscribe
    public void getFav(Events.Fav saveJob) {
        for (int i = 0; i < mListItem.size(); i++) {
            if (mListItem.get(i) != null) {
                if (mListItem.get(i).getPlaceId().equals(saveJob.getReId())) {
                    mListItem.get(i).setFavourite(saveJob.isFav());
                    adapter.notifyItemChanged(i);
                }
            }
        }
    }
}
