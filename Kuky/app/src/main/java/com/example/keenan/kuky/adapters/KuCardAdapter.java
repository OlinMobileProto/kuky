package com.example.keenan.kuky.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.activities.DetailActivity;
import com.example.keenan.kuky.activities.LoginActivity;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.fragments.ProfileFragment;
import com.example.keenan.kuky.helpers.AuthHelper;
import com.example.keenan.kuky.models.Ku;
import com.example.keenan.kuky.models.KuActionRequest;
import com.example.keenan.kuky.models.KuActionResponse;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class KuCardAdapter extends RecyclerView.Adapter<KuViewHolder>{
    private static final String TAG = KuCardAdapter.class.getSimpleName();
    private ArrayList<Ku> mDataset;
    private Context mContext;
    @Bind(R.id.downvoteButton) ImageButton mDownVoteButton;
    @Bind(R.id.upvoteButton) ImageButton mUpVoteButton;
    @Bind(R.id.favoriteButton) ImageButton mFavoriteButton;

    // Provide a suitable constructor (depends on the kind of dataset)
    public KuCardAdapter(ArrayList<Ku> Kus, Context context) {
        mDataset = Kus;
        mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public KuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ku_card_view, parent, false);
        // set the view's size, margins, paddings and layout parameters
        //...
        ButterKnife.bind(this, v);
        return new KuViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final KuViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.setClickListener(new KuViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos) {
                Ku ku = mDataset.get(pos);
                Intent dIntent = new Intent(mContext, DetailActivity.class);
                Log.d(TAG, ku.toString());
                // nice job working with intents
                dIntent.putExtra(DetailActivity.KU_ID, String.valueOf(ku.getId()));
                dIntent.putExtra(DetailActivity.KU_UPVOTED, String.valueOf(ku.getUpvoted()));
                dIntent.putExtra(DetailActivity.KU_DOWNVOTED, String.valueOf(ku.getDownvoted()));
                dIntent.putExtra(DetailActivity.KU_FAVORITED, String.valueOf(ku.getFavorited()));
                mContext.startActivity(dIntent);
            }
        });


        final Ku mKu = mDataset.get(position);

        Log.i(TAG, mKu.toString());

        String[] lines = mKu.getContent();
        Integer ku_karma = mKu.getKarma();

        holder.vKuContent1.setText(lines[0]);
        holder.vKuContent2.setText(lines[1]);
        holder.vKuContent3.setText(lines[2]);
        holder.vKuKarma.setText(String.valueOf(ku_karma));
        holder.vUpvotePressed = mKu.getUpvoted();
        holder.vDownvotePressed = mKu.getDownvoted();
        holder.vFavoritePressed = mKu.getFavorited();

        boolean isUpvoted = mKu.getUpvoted();
        boolean isDownvoted = mKu.getDownvoted();
        boolean isFavorited = mKu.getFavorited();

        if (isFavorited) {
            holder.vFavorite.setImageResource(R.drawable.ic_star_yellow_900_24dp);
        } else {
            holder.vFavorite.setImageResource(R.drawable.ic_star_outline_black_24dp);
        }

        if (isUpvoted) {
            holder.vUpvote.setBackgroundResource(R.drawable.ic_arrow_drop_up_blue_800_24dp);
        } else {
            holder.vUpvote.setBackgroundResource(R.drawable.ic_arrow_drop_up_black_24dp);
        }

        if (isDownvoted) {
            holder.vDownvote.setBackgroundResource(R.drawable.ic_arrow_drop_down_blue_800_24dp);
        } else {
            holder.vDownvote.setBackgroundResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }

        // again, you should write code that wraps around this functionality
        holder.vUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getUserId();
                int kuId = mKu.getId();

                mKu.setUpvoted(!mKu.getUpvoted());

                if (userId > 0) {
                    sendUpvoteRequest(new KuActionRequest(userId, kuId), mKu, position);
                    if (mKu.getDownvoted()) {
                        mKu.setDownvoted(false);
                        sendDownvoteRequest(new KuActionRequest(userId, kuId), mKu, position);
                    }
                }
            }
        });

        holder.vDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getUserId();
                int kuId = mKu.getId();

                mKu.setDownvoted(!mKu.getDownvoted());

                if (userId > 0) {
                    sendDownvoteRequest(new KuActionRequest(userId, kuId), mKu, position);
                    if (mKu.getUpvoted()) {
                        mKu.setUpvoted(false);
                        sendUpvoteRequest(new KuActionRequest(userId, kuId), mKu, position);
                    }
                }
            }
        });

        holder.vFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getUserId();
                int kuId = mKu.getId();

                mKu.setFavorited(!mKu.getFavorited());
                if (userId > 0) {
                    ApiClient.getKukyApiClient(AuthHelper.getCreds(mContext))
                            .favoriteKu(new KuActionRequest(userId, kuId))
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Subscriber<KuActionResponse>() {
                                @Override
                                public void onCompleted() {
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Log.e(TAG + ": Retrofit Error - ", e.toString());
                                }

                                @Override
                                public void onNext(KuActionResponse kuActionResponse) {
                                    notifyItemChanged(position);
                                    ProfileFragment.updateFavorite(mKu, mKu.getFavorited());
                                }
                            });
                }
            }
        });
    }

    /**
     * Sends an upvote request to the server for the given ku
     * Then updates the view for the ku feed
     * @param request KuActionRequest object representing the upvote request
     * @param ku The ku item being updated
     * @param position The position of the ku object being updated
     */
    public void sendUpvoteRequest(KuActionRequest request, final Ku ku, final int position) {
        ApiClient.getKukyApiClient(AuthHelper.getCreds(mContext))
            .upvoteKu(request)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<KuActionResponse>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG + ": Retrofit Error - ", e.toString());
                }

                @Override
                public void onNext(KuActionResponse kuActionResponse) {
                    Log.wtf(TAG, kuActionResponse.getStatus());
                    ku.setKarma(Integer.parseInt(kuActionResponse.getStatus()));
                    notifyItemChanged(position);
                    if (ku.getUpvoted()) {
                        ProfileFragment.updateScore(ProfileFragment.VOTING_SCORE);
                    } else {
                        ProfileFragment.updateScore(-ProfileFragment.VOTING_SCORE);
                    }
                }
            });
    }

    /**
     * Sends a downvote request to the server for the given ku
     * Then updates the view for the ku feed
     * @param request KuActionRequest object representing the downvote request
     * @param ku The ku item being updated
     * @param position The position of the ku object being updated
     */
    public void sendDownvoteRequest(KuActionRequest request, final Ku ku, final int position) {
        ApiClient.getKukyApiClient(AuthHelper.getCreds(mContext))
            .downvoteKu(request)
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Subscriber<KuActionResponse>() {
                @Override
                public void onCompleted() {}

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG + ": Retrofit Error - ", e.toString());
                }

                @Override
                public void onNext(KuActionResponse kuActionResponse) {
                    Log.wtf(TAG, kuActionResponse.getStatus());
                    ku.setKarma(Integer.parseInt(kuActionResponse.getStatus()));
                    notifyItemChanged(position);
                    if (ku.getDownvoted()) {
                        ProfileFragment.updateScore(ProfileFragment.VOTING_SCORE);
                    } else {
                        ProfileFragment.updateScore(-ProfileFragment.VOTING_SCORE);
                    }
                }
            });
    }

    /**
     * Get the UserId from SharedPreferences
     * @return int UserId
     */
    public int getUserId() {
        SharedPreferences settings = mContext.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getInt("userId", -1);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setList(ArrayList<Ku> newList) {
        mDataset = newList;
    }
}
