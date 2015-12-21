package com.example.keenan.kuky.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import com.example.keenan.kuky.helpers.AuthHelper;
import com.example.keenan.kuky.models.Ku;
import com.example.keenan.kuky.models.KuListResponse;
import com.example.keenan.kuky.models.KuRequest;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**\
 * Feed Fragment is one of the tabs of the KuView activity. It has two tabs within it as well,
 * the recent kus and the hot kus. This is the screen shown after the user logs in.
 * It holds a Recycler view of cards, which can be upvoted, downvoted, or favorited.
 */
public class FeedFragment extends Fragment {

    private static final String TAG = FeedFragment.class.getSimpleName();

    private KuCardAdapter mKuCardAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    ArrayList<Ku> mkuList = new ArrayList<>();

    @Bind(R.id.ku_feed_rv) RecyclerView mKuRecyclerView;
    @Bind(R.id.hotButton) ImageButton mHotButton;
    @Bind(R.id.recentButton) ImageButton mRecentButton;
    @Bind(R.id.lack_of_kus) TextView mNoKusText;

    /**
     * One of the tabs for KuView, reorganizes the ku list to show the 'hot' haikus
     * @param view the screen view
     */
    @OnClick(R.id.hotButton)
    public void onHotButtonClicked(View view) {
        UpdateKus(KuRequest.KU_SORT_HOT);
        mHotButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan_A700));
        mRecentButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_100));
        Snackbar.make(view, "Showing hottest Kus!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    /**
     * One of the tabs for KuView, reorganizes the ku list to show the 'recent' haikus
     * @param view the screen view
     */
    @OnClick(R.id.recentButton)
    public void onRecentButtonClicked(View view) {
        UpdateKus(KuRequest.KU_SORT_RECENT);
        mHotButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.grey_100));
        mRecentButton.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.cyan_A700));
        Snackbar.make(view, "Showing recent Kus!", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public FeedFragment() {
        // Required empty public constructor
    }

    /**
     * calls the super oncreate
     * @param savedInstanceState a previous saved state that can be reconstructed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Sets up the view of the fragment as well as the recycler view. Calls the
     * updateKus() method to load data into the view
     * @param inflater inflates the view to the fragment
     * @param container the parent view that the UI should be attached to
     * @param savedInstanceState a previous saved state that can be attached to
     * @return the rootView of the the fragments view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_feed, container, false);
        ButterKnife.bind(this, rootView);

        mKuRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mKuRecyclerView.setLayoutManager(mLayoutManager);

        mKuCardAdapter = new KuCardAdapter(mkuList, getActivity());

        mKuRecyclerView.setAdapter(mKuCardAdapter);

        UpdateKus(KuRequest.KU_SORT_RECENT);

        return rootView;
    }

    /**
     * Makes an API call to get the top or recent Kus. Checks that it isn't an empty dataset
     * and updates the view
     * Uses the KuCardAdapter class for button functionality and loading data
     */
    public void UpdateKus(String sort) {
        // use a lowercase name for this method
        ApiClient.getKukyApiClient(AuthHelper.getCreds(getContext()))
                .getKus(sort, String.valueOf(getUserId()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<KuListResponse>() {
                    @Override
                    public final void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public final void onError(Throwable e) {
                        Log.e("KukyAPI Error", e.getMessage());
                    }

                    @Override
                    public final void onNext(KuListResponse response) {
                        mkuList = response.getKus();
                        checkForKus(mkuList);
                        mKuCardAdapter.setList(mkuList);
                        mKuCardAdapter.notifyDataSetChanged();
                        Log.d(TAG, "Received data");
                    }
                });
    }
    /**
     * Checks for empty list of kus, if so, handles empty set by showing 'sorry' text
     * @param mkuList List of haikus
     */
    public void checkForKus(ArrayList mkuList) {
        /*
        Again, it would be better to simply get the list from the adapter rather than take it as an input
         */
        if (mkuList.isEmpty())
        {
            mKuRecyclerView.setVisibility(View.GONE);
            mNoKusText.setVisibility(View.VISIBLE);
        }
        else {
            mKuRecyclerView.setVisibility(View.VISIBLE);
            mNoKusText.setVisibility(View.GONE);
        }
    }

    /**
     * Get method for the UserID
     * @return the user's ID
     */
    public int getUserId() {
        SharedPreferences settings = getContext().getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getInt("userId", -1);
    }
}
