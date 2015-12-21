package com.example.keenan.kuky.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.adapters.CommentCardAdapter;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.fragments.ProfileFragment;
import com.example.keenan.kuky.helpers.AuthHelper;
import com.example.keenan.kuky.models.Comment;
import com.example.keenan.kuky.models.CommentComposeRequest;
import com.example.keenan.kuky.models.CommentComposeResponse;
import com.example.keenan.kuky.models.Ku;
import com.example.keenan.kuky.models.KuActionRequest;
import com.example.keenan.kuky.models.KuActionResponse;
import com.example.keenan.kuky.models.KuDetailResponse;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * The Detail Activity is shown when a Ku is pressed for more information, or detail
 * It shows the list of comments through a recycler view, as well as a floating action button
 * to submit a new comment, which is displayed through an AlertDialog. Also makes all of the API calls
 * when upvote/downvote/favorite buttons are clicked.
 */
public class DetailActivity extends AppCompatActivity {

    public static final String TAG = DetailActivity.class.getSimpleName();

    public static final String KU_ID = "ku_id";
    public static final String KU_UPVOTED = "ku_upvoted";
    public static final String KU_DOWNVOTED = "ku_downvoted";
    public static final String KU_FAVORITED = "ku_favorited";

    private String ku_id;
    private boolean upvoted, downvoted, favorited;
    private Ku mKu;

    private String mComment = "";
    private ArrayList<Comment> mCommentList = new ArrayList<Comment>();
    private RecyclerView.LayoutManager mLayoutManager;
    private CommentCardAdapter mCommentCardAdapter;

    @Bind(R.id.ku_content1_tv) TextView mKuContent1Tv;
    @Bind(R.id.ku_content2_tv) TextView mKuContent2Tv;
    @Bind(R.id.ku_content3_tv) TextView mKuContent3Tv;
    @Bind(R.id.ku_karma_tv) TextView mKuKarmaTv;
    @Bind(R.id.ku_card_detail_view) CardView mKuCard;
    @Bind(R.id.comment_feed_rv) RecyclerView mCommentRecyclerView;
    @Bind(R.id.lack_of_comments) TextView mNoCommentsText;
    @Bind(R.id.downvoteButtonDetail) ImageButton mDownVoteButton;
    @Bind(R.id.upvoteButtonDetail) ImageButton mUpVoteButton;
    @Bind(R.id.favoriteButtonDetail) ImageButton mFavoriteButton;

    /**
     * OnCreate sets the content of the screen, including the floating action button for commenting
     * Commenting button makes an API call and displays an AlertDialog to type the comment.
     * @param savedInstanceState if non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(R.drawable.ic_logo_clear);

        Intent intent = getIntent();
        ku_id = intent.getStringExtra(DetailActivity.KU_ID);
        upvoted = Boolean.valueOf(intent.getStringExtra(DetailActivity.KU_UPVOTED));
        downvoted = Boolean.valueOf(intent.getStringExtra(DetailActivity.KU_DOWNVOTED));
        favorited = Boolean.valueOf(intent.getStringExtra(DetailActivity.KU_FAVORITED));

        Log.d(TAG, "DetailActivity received Ku_ID of : " + ku_id);

        fetchKuInfo(ku_id);

        mCommentRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mCommentRecyclerView.setLayoutManager(mLayoutManager);

        Log.d(TAG, Integer.toString(mCommentList.size()));

        mCommentCardAdapter = new CommentCardAdapter(mCommentList, this, Integer.parseInt(ku_id));
        mCommentRecyclerView.setAdapter(mCommentCardAdapter);

        // Set FAB to add new post new comments through alertdialog
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                builder.setTitle("Enter Your Comment:");

                final EditText input = new EditText(DetailActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("Comment", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mComment = input.getText().toString();
                        int userId = getUserId();

                        CommentComposeRequest mCommentRequest = new CommentComposeRequest(mComment, Integer.parseInt(ku_id), userId);

                        ApiClient.getKukyApiClient(AuthHelper.getCreds(DetailActivity.this))
                                .postComment(mCommentRequest)
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Subscriber<CommentComposeResponse>() {
                                    @Override
                                    public void onCompleted() {

                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        Log.e(TAG + ": Retrofit Error - ", e.toString());
                                    }

                                    @Override
                                    public void onNext(CommentComposeResponse commentComposeResponse) {
                                        Log.wtf(TAG, commentComposeResponse.getComment().getContent());
                                        mCommentList.add(commentComposeResponse.getComment());
                                        mCommentCardAdapter.setList(mCommentList);
                                        mCommentCardAdapter.notifyDataSetChanged();
                                        ProfileFragment.updateScore(ProfileFragment.COMMENTING_SCORE);
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    /**
     * Updating the Comments that are displayed in the Detail View. Makes an API call to grab the
     * information. Also checks for empty data sets
     * @param ku_id the ID for the ku that needs to be grabbed
     */
    public void fetchKuInfo(String ku_id){
        ApiClient.getKukyApiClient().getKuDetail(ku_id, String.valueOf(getUserId()))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<KuDetailResponse>() {
                               @Override
                               public void onCompleted() {

                               }

                               @Override
                               public void onError(Throwable e) {
                                   Log.e(TAG + ": Retrofit Error - ", e.toString());
                               }

                               @Override
                               public void onNext(KuDetailResponse kuDetailResponse) {
                                   mKu = kuDetailResponse.getKu();
                                   // is upvoted, downvoted, etc. not sent by the server?
                                   mKu.setUpvoted(upvoted);
                                   mKu.setDownvoted(downvoted);
                                   mKu.setFavorited(favorited);
                                   Log.d(TAG, mKu.toString());
                                   setKuCardViewContent(mKu);
                                   updateButtons(mKu);

                                   mCommentList = kuDetailResponse.getComments();
                                   mCommentCardAdapter.setList(mCommentList);
                                   mCommentCardAdapter.notifyDataSetChanged();
                                   checkForComments(mCommentList);
                                   Log.d(TAG, mCommentList.get(0).toString());
                               }
                           }
                );
    }

    /**
     * Sets the card views content from the API call
     * @param ku the Ku object
     */
    public void setKuCardViewContent(Ku ku){
        String[] ku_content = ku.getContent();
        mKuContent1Tv.setText(ku_content[0]);
        mKuContent2Tv.setText(ku_content[1]);
        mKuContent3Tv.setText(ku_content[2]);
        mKuKarmaTv.setText(String.valueOf(ku.getKarma()));

    }

    /**
     * Checks for an empty set and handles it by showing a 'sorry' message if empty
     * @param mCommentList ArrayList of the comments
     */
    public void checkForComments(ArrayList mCommentList) {
        /* instead of passing the comment list in here, you can get the list from the adapter and check that.
          That way, it eliminates the needed input for cleaner code & makes it so that you can call this function anytime without
          needing to have a comment list handy */
        if (mCommentList.isEmpty())
        {
            mCommentRecyclerView.setVisibility(View.GONE);
            mNoCommentsText.setVisibility(View.VISIBLE);
        }
        else {
            mCommentRecyclerView.setVisibility(View.VISIBLE);
            mNoCommentsText.setVisibility(View.GONE);
        }


    }

    /**
     * Updates the view of the ImageButtons if they are pressed
     * @param ku the Ku Object
     */
    public void updateButtons(Ku ku)
    {
        if (ku.getUpvoted()) {
            mUpVoteButton.setBackgroundResource(R.drawable.ic_arrow_drop_up_blue_800_24dp);
        } else {
            mUpVoteButton.setBackgroundResource(R.drawable.ic_arrow_drop_up_black_24dp);
        }

        if (ku.getDownvoted()) {
            mDownVoteButton.setBackgroundResource(R.drawable.ic_arrow_drop_down_blue_800_24dp);
        } else {
            mDownVoteButton.setBackgroundResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }

        if (ku.getFavorited()) {
            mFavoriteButton.setImageResource(R.drawable.ic_star_yellow_900_24dp);
        } else {
            mFavoriteButton.setImageResource(R.drawable.ic_star_outline_black_24dp);
        }

        mKuKarmaTv.setText(String.valueOf(ku.getKarma()));

    }

    /**
     * Returns the ID of the user in shared preferences
     * @return the UserId
     */
    public int getUserId() {
        SharedPreferences settings = this.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getInt("userId", -1);
    }

    /**
     * Makes an API call if the Upvote button is clicked
     * @param view the current screen view
     */
    @OnClick(R.id.upvoteButtonDetail)
    public void onUpvoteClick(View view)
    {
        int userId = getUserId();
        int kuId = mKu.getId();
        mKu.setUpvoted(!mKu.getUpvoted());

        if (userId > 0) {
            sendUpvoteRequest(new KuActionRequest(userId, kuId), mKu);
            if (mKu.getDownvoted()) {
                mKu.setDownvoted(false);
                sendDownvoteRequest(new KuActionRequest(userId, kuId), mKu);
            }
        }
        updateButtons(mKu);

    }
    /**
     * Makes an API call if the Downvote button is clicked
     * @param view the current screen view
     */
    @OnClick(R.id.downvoteButtonDetail)
    public void onDownvoteClick(View view)
    {
        int userId = getUserId();
        int kuId = mKu.getId();

        mKu.setDownvoted(!mKu.getDownvoted());

        if (userId > 0) {
            sendDownvoteRequest(new KuActionRequest(userId, kuId), mKu);
            if (mKu.getUpvoted()) {
                mKu.setUpvoted(false);
                sendUpvoteRequest(new KuActionRequest(userId, kuId), mKu);
            }
        }
        updateButtons(mKu);
    }

    /**
     * Makes an API call if the Favorite button is clicked
     * @param view the current screen view
     */
    @OnClick(R.id.favoriteButtonDetail)
    public void onFavoriteClick(View view)
    {
        int userId = getUserId();
        int kuId = mKu.getId();

        mKu.setFavorited(!mKu.getFavorited());
        if (userId > 0) {
            // Could you make a wrapper around this? It seems long and unwieldy
            ApiClient.getKukyApiClient(AuthHelper.getCreds(getApplicationContext()))
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
//                            notifyItemChanged(position);
                            ProfileFragment.updateFavorite(mKu, mKu.getFavorited());
                            notify();
                        }
                    });
        }
        updateButtons(mKu);

    }

    /**
     * The API call for when the Upvote Button is pressed.
     * @param request the Action Request object from the API call
     * @param ku the Ku Object
     */
    public void sendUpvoteRequest(KuActionRequest request, final Ku ku) {
        ApiClient.getKukyApiClient(AuthHelper.getCreds(getApplicationContext()))
                .upvoteKu(request)
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
                        Log.wtf(TAG, kuActionResponse.getStatus());
                        ku.setKarma(Integer.parseInt(kuActionResponse.getStatus()));
                        notify();
                        if (ku.getUpvoted()) {
                            ProfileFragment.updateScore(ProfileFragment.VOTING_SCORE);
                        } else {
                            ProfileFragment.updateScore(-ProfileFragment.VOTING_SCORE);
                        }
                    }
                });
    }

    /**
     * The API call for when the Downvote Button is pressed.
     * @param request the Action Request object from the API call
     * @param ku the Ku Object
     */
    public void sendDownvoteRequest(KuActionRequest request, final Ku ku) {
        ApiClient.getKukyApiClient(AuthHelper.getCreds(getApplicationContext()))
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
                        notify();
                        if (ku.getDownvoted()) {
                            ProfileFragment.updateScore(ProfileFragment.VOTING_SCORE);
                        } else {
                            ProfileFragment.updateScore(-ProfileFragment.VOTING_SCORE);
                        }
                    }
                });
    }
}
