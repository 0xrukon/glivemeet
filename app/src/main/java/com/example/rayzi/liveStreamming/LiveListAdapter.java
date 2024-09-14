package com.example.rayzi.liveStreamming;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.graphics.drawable.PictureDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.activity.FakeWatchAudioLiveActivity;
import com.example.rayzi.activity.FakeWatchLiveActivity;
import com.example.rayzi.audioLive.WatchAudioLiveActivity;
import com.example.rayzi.databinding.ItemPartyBinding;
import com.example.rayzi.databinding.ItemVideoGrid1Binding;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.retrofit.Const;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class LiveListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "LiveListAdapter";
    private Context context;
    private List<LiveUserRoot.UsersItem> userDummies = new ArrayList<>();
    private int live_layout = 1;
    private int[] colors = {
            R.color.lavender,
            R.color.light_yellow,
            R.color.light_pink,
            R.color.light_sky
    };

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == 1) {
            return new VideoListViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_party, parent, false));
        } else {
            return new AdViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoListViewHolder) {
            ((VideoListViewHolder) holder).setData(position);
        }

    }

    @Override
    public int getItemCount() {
        return userDummies.size();
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        return this.live_layout;
    }

    public void addData(List<LiveUserRoot.UsersItem> userDummies) {
        this.userDummies.addAll(userDummies);
        notifyItemRangeInserted(this.userDummies.size(), userDummies.size());
    }

    public void clear() {
        userDummies.clear();
        notifyDataSetChanged();
    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder {
        ItemPartyBinding binding;

        public VideoListViewHolder(View itemView) {
            super(itemView);
            binding = ItemPartyBinding.bind(itemView);
        }

        public void setData(int position) {
            LiveUserRoot.UsersItem userDummy = userDummies.get(position);
            binding.tvPartyTitle.setText((userDummy.getRoomName() == null) ? userDummy.getName() : userDummy.getRoomName());

            int colorIndex = position % colors.length;
            binding.layMain.setCardBackgroundColor(ContextCompat.getColor(context, colors[colorIndex]));

            if(userDummy.getPrivateCode() == 0){
                binding.ivLock.setImageResource(R.drawable.ic_public);
                binding.tvPublic.setText("Public");
            }else{
                binding.ivLock.setImageResource(R.drawable.ic_private);
                binding.tvPublic.setText("Private");
            }

            Glide.with(context).load((userDummy.getRoomImage() == null) ? userDummy.getImage() : userDummy.getRoomImage())
                    .apply(MainApplication.requestOptionsLive)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop().into(binding.ivPartyImage);

            binding.tvViewCount.setText(String.valueOf(userDummy.getView()));
            binding.tvPartyDescription.setText((userDummy.getRoomWelcome()==null) ? "Welcome to the party" : userDummy.getRoomWelcome());
            binding.getRoot().setOnClickListener(v -> {
//                if (userDummy.isIsFake()) {
//                    context.startActivity(new Intent(context, FakeWatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
//                } else {
                context.startActivity(new Intent(context, WatchAudioLiveActivity.class).putExtra(Const.DATA, new Gson().toJson(userDummy)));
//                }

            });
        }
    }

    private class AdViewHolder extends RecyclerView.ViewHolder {
        public AdViewHolder(View inflate) {
            super(inflate);
        }
    }
}
