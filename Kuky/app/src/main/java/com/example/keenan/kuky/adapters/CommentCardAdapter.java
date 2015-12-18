package com.example.keenan.kuky.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.keenan.kuky.R;
import com.example.keenan.kuky.activities.LoginActivity;
import com.example.keenan.kuky.api.ApiClient;
import com.example.keenan.kuky.helpers.AuthHelper;
import com.example.keenan.kuky.models.Comment;
import com.example.keenan.kuky.models.CommentActionRequest;
import com.example.keenan.kuky.models.CommentActionResponse;

import java.util.ArrayList;

import butterknife.ButterKnife;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CommentCardAdapter extends RecyclerView.Adapter<CommentViewHolder> {
    private static final String TAG = CommentCardAdapter.class.getSimpleName();
    private ArrayList<Comment> mDataset;
    private Context mContext;
    private int mku_id;

    public CommentCardAdapter(ArrayList<Comment> Comments, Context context, int kuId) {
        mDataset = Comments;
        mContext = context;
        mku_id = kuId;
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_card_view, parent, false);
        ButterKnife.bind(this, v);
        return new CommentViewHolder(v);
    }

    public void onBindViewHolder(CommentViewHolder holder, final int position) {

        final Comment mComment = mDataset.get(position);

        String content = mComment.getContent();
        int kudos = mComment.getKudos();

        holder.vCommentContent.setText(content);
        holder.vCommentKarma.setText(String.valueOf(kudos));

        if (mComment.isUpvoted()) {
            holder.vUpvote.setBackgroundResource(R.drawable.ic_arrow_drop_up_blue_800_24dp);
        } else {
            holder.vUpvote.setBackgroundResource(R.drawable.ic_arrow_drop_up_black_24dp);
        }

        if (mComment.isDownvoted()) {
            holder.vDownvote.setBackgroundResource(R.drawable.ic_arrow_drop_down_blue_800_24dp);
        } else {
            holder.vDownvote.setBackgroundResource(R.drawable.ic_arrow_drop_down_black_24dp);
        }

        holder.vUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getUserId();
                int commentId = mComment.getId();
                CommentActionRequest req = new CommentActionRequest(userId, mku_id, commentId);
                Log.d(TAG, req.toString());

                mComment.setUpvoted(!mComment.isUpvoted());

                if (userId > 0) {
                    sendUpvoteRequest(req, mComment, position);
                    if (mComment.isDownvoted()) {
                        mComment.setDownvoted(false);
                        sendDownvoteRequest(req, mComment, position);
                    }
                }
            }
        });

        holder.vDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int userId = getUserId();
                int commentId = mComment.getId();
                CommentActionRequest req = new CommentActionRequest(userId, mku_id, commentId);
                Log.d(TAG, req.toString());

                mComment.setDownvoted(!mComment.isDownvoted());

                if (userId > 0) {
                    sendDownvoteRequest(req, mComment, position);
                    if (mComment.isUpvoted()) {
                        mComment.setUpvoted(false);
                        sendUpvoteRequest(req, mComment, position);
                    }
                }
            }
        });
    }

    public void sendUpvoteRequest(CommentActionRequest request, final Comment comment, final int position) {
        String[] creds = AuthHelper.getCreds(mContext);
        // if (creds != null) {
            ApiClient.getKukyApiClient(creds)
                    .upvoteComment(request)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<CommentActionResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(CommentActionResponse CommentActionResponse) {
                            updateItem(comment, Integer.parseInt(CommentActionResponse.getStatus()), position);
                        }
                    });
        // }
    }

    public void sendDownvoteRequest(CommentActionRequest request, final Comment comment, final int position) {
        String[] creds = AuthHelper.getCreds(mContext);
        // if (creds != null) {
            ApiClient.getKukyApiClient(creds)
                    .downvoteComment(request)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<CommentActionResponse>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(CommentActionResponse CommentActionResponse) {
                            updateItem(comment, Integer.parseInt(CommentActionResponse.getStatus()), position);
                        }
                    });
        // }
    }

    public void updateItem(Comment comment, int karma, int pos) {
        comment.setKudos(karma);
        notifyItemChanged(pos);
    }

    public int getUserId() {
        SharedPreferences settings = mContext.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
        return settings.getInt("userId", -1);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void setList(ArrayList<Comment> newList) {
        mDataset = newList;
    }
}
