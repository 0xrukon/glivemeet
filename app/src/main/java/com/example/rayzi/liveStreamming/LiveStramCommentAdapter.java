package com.example.rayzi.liveStreamming;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.databinding.ItemLivestramCommentBinding;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.modelclass.UserRoot;

import java.util.ArrayList;
import java.util.List;

public class LiveStramCommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW1 = 1;
    private static final int VIEW2 = 2;
    List<LiveStramComment> comments = new ArrayList<>();
    OnCommentClickListner onCommentClickListner;
    private Context context;

    @Override
    public int getItemViewType(int position) {
        if (comments.get(position) == null) return VIEW1;
        return VIEW2;
    }

    public OnCommentClickListner getOnCommentClickListner() {
        return onCommentClickListner;
    }

    public void setOnCommentClickListener(OnCommentClickListner onCommentClickListner) {
        this.onCommentClickListner = onCommentClickListner;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == VIEW1) {
            return new NoticeViewHOlder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livestream_comment_1, parent, false));
        }
        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_livestram_comment, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).setCommentData(position);
        }

    }


    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addSingleComment(LiveStramComment liveStramCommentDummy) {
        this.comments.add(0, liveStramCommentDummy);
        notifyItemInserted(0);
    }

    public interface OnCommentClickListner {
        void onClickCommet(UserRoot.User userDummy);
    }

    public class NoticeViewHOlder extends RecyclerView.ViewHolder {

        public NoticeViewHOlder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemLivestramCommentBinding binding;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemLivestramCommentBinding.bind(itemView);

        }

        @SuppressLint("SetTextI18n")
        public void setCommentData(int position) {
            LiveStramComment comment = comments.get(position);

            if (comment.getType() != null && comment.getType().equals("reaction")) {
                binding.tvComment.setText("Reacted");
                binding.tvJoined.setVisibility(View.GONE);
                binding.imgReaction.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(comment.getReaction())
                        .into(binding.imgReaction);
            } else {
                if (comment.isJoined() && !comment.getComment().isEmpty()) {
                    binding.tvJoined.setText(comment.getComment());
                    binding.tvName.setVisibility(View.GONE);
                    binding.tvJoined.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.layUserImage.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.GONE);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.green_light));
                } else if (comment.isJoined()) {
                    binding.tvJoined.setText("Enter a room");
                    binding.tvName.setText(comment.getUser().getName() + " :");
                    binding.tvName.setVisibility(View.VISIBLE);
                    binding.tvJoined.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.GONE);
                    binding.layUserImage.setVisibility(View.VISIBLE);
                    binding.tvJoined.setTextColor(context.getResources().getColor(R.color.white));
                } else {
                    binding.tvJoined.setVisibility(View.GONE);
                    binding.tvName.setVisibility(View.VISIBLE);
                    binding.imgReaction.setVisibility(View.GONE);
                    binding.layTvComment.setVisibility(View.VISIBLE);
                    binding.layUserImage.setVisibility(View.VISIBLE);
                    binding.tvComment.setText(comment.getComment());
                    binding.tvName.setText(comment.getUser().getName());
                }
            }

            Log.e("TAG", "setCommentData: >>>>>>>>>>>>>> " + comment.getUser().getImage());
            binding.imgUser.setUserImage(comment.getUser().getImage(), comment.getUser().isIsVIP(), context, 10);

            binding.getRoot().setOnClickListener(v -> onCommentClickListner.onClickCommet(comment.getUser()));
        }

        private void setUserLevel(String image, ImageView buttomLevel) {
            Glide.with(context).load(BuildConfig.BASE_URL + image)
                    .apply(MainApplication.requestOptions)
                    .into(buttomLevel);
        }


    }


}
