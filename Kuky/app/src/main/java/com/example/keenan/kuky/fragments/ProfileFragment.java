package com.example.keenan.kuky.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.activities.LoginActivity;
import com.example.keenan.kuky.adapters.KuCardAdapter;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.models.Ku;
import com.example.keenan.kuky.models.User;
import com.example.keenan.kuky.models.UserProfileResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();
    private ArrayList<Ku> composedKus = new ArrayList<>();
    private ArrayList<Ku> favoritedKus = new ArrayList<>();
    private User user;
    private KuCardAdapter mKuCardAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Bind(R.id.ku_profile_feed) RecyclerView mKuRecyclerView;
    @Bind(R.id.kudos_display) TextView kudosDisplay;
    @Bind(R.id.favorite_kus_profile) ImageButton favoritesButton;
    @Bind(R.id.composed_kus_profile) ImageButton composedButton;

    @OnClick(R.id.favorite_kus_profile)
    public void onFavoritesSelected(View view) {
        mKuCardAdapter.setList(favoritedKus);
        mKuCardAdapter.notifyDataSetChanged();
        favoritesButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red_100));
        composedButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_100));
    }

    @OnClick(R.id.composed_kus_profile)
    public void onComposedSelected(View view) {
        mKuCardAdapter.setList(composedKus);
        mKuCardAdapter.notifyDataSetChanged();
        composedButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.red_100));
        favoritesButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_100));
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, rootView);

        updateProfile();

        mKuRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mKuRecyclerView.setLayoutManager(mLayoutManager);

        mKuCardAdapter = new KuCardAdapter(favoritedKus, getActivity());
        mKuRecyclerView.setAdapter(mKuCardAdapter);

        return rootView;
    }

    public void updateProfile() {
        SharedPreferences settings = getContext().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        String uname = settings.getString("username", null);
        String apiKey = settings.getString("apiKey", null);
        ApiClient.getKukyApiClient(
                uname,
                apiKey
        ).getUser(uname)
            .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserProfileResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(UserProfileResponse userProfileResponse) {
                        Log.d(TAG, userProfileResponse.toString());
                        processKus(userProfileResponse.getComposedKus(), "composed");
                        processKus(userProfileResponse.getFavoritedKus(), "favorited");
                        processUserInfo(userProfileResponse.getBasicInfo());
                        mKuCardAdapter.notifyDataSetChanged();
                    }
                });
    }

    public void processKus(JsonObject composed, String type) {
        for (Map.Entry<String, JsonElement> entry: composed.entrySet()) {
            JsonObject ku = entry.getValue().getAsJsonObject();
            int id = ku.get("id").getAsInt();
            String content = ku.get("content").getAsString();
            int karma = ku.get("karma").getAsInt();
            double lat = ku.get("lat").getAsDouble();
            double lon = ku.get("lon").getAsDouble();
            boolean upvoted = ku.get("upvoted").getAsBoolean();
            boolean downvoted = ku.get("downvoted").getAsBoolean();
            Ku thisKu = new Ku(id, content, karma, lat, lon, upvoted, downvoted);
            if (type.equals("composed")) {
                composedKus.add(thisKu);
                Log.d(TAG, "COMPOSED " + composedKus.toString());
            } else {
                favoritedKus.add(thisKu);
                Log.d(TAG, "FAVORITED " + favoritedKus.toString());
            }
        }
    }

    public void processUserInfo(JsonObject userInfo) {
        user = new User(userInfo.get("id").getAsInt(),
                userInfo.get("username").getAsString(),
                userInfo.get("score").getAsInt(),
                userInfo.get("radiusLimit").getAsDouble());
        String kudos = getResources().getString(R.string.kudos) + ' ' + String.valueOf(user.getScore());
        kudosDisplay.setText(kudos);
    }
}
