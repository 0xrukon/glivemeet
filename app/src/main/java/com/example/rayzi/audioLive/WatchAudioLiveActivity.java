package com.example.rayzi.audioLive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.SessionManager;
import com.example.rayzi.adapter.GiftReceiveAdapter;
import com.example.rayzi.agora.AgoraBaseActivity;
import com.example.rayzi.agora.stats.RemoteStatsData;
import com.example.rayzi.agora.stats.StatsData;
import com.example.rayzi.audioLive.reactions.BottomSheetReactions;
import com.example.rayzi.audioLive.reactions.ReactionsViewModel;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomPasscode;
import com.example.rayzi.bottomsheets.BottomSheetReport_g;
import com.example.rayzi.bottomsheets.UserProfileBottomSheet;
import com.example.rayzi.databinding.ActivityWatchAudioLiveBinding;
import com.example.rayzi.databinding.ItemSeatBinding;
import com.example.rayzi.emoji.EmojiBottomSheetFragment;
import com.example.rayzi.emoji.UserSelectableClass;
import com.example.rayzi.modelclass.GiftRoot;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.modelclass.LiveUserRoot;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.socket.AudioRoomHandler;
import com.example.rayzi.socket.MySocketManager;
import com.example.rayzi.viewModel.EmojiSheetViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.example.rayzi.viewModel.WatchLiveViewModel;
import com.example.rayzi.z_demo.Demo_contents;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import eightbitlab.com.blurview.RenderScriptBlur;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchAudioLiveActivity extends AgoraBaseActivity {

    ActivityWatchAudioLiveBinding binding;

    private static final String TAG = "watchliveact";
    private static int MY_UID = 0;

    int uuid;
    String token = "";
    int selfPosition = -1;
    boolean isHost = false;
    SeatAdapter seatAdapter;
    boolean isMuteByHost = false;
    LiveUserRoot.UsersItem host;
    SessionManager sessionManager;
    Handler handler = new Handler();
    WatchLiveViewModel viewModel;
    EmojiSheetViewModel giftViewModel;
    GridLayoutManager gridLayoutManager;
    private boolean isUserJoined = false;
    List<String> uidlist = new ArrayList<>();
    UserProfileBottomSheet userProfileBottomSheet;
    private BottomSheetReactions bottomSheetReactions;
    List<SeatItem> bookedSeatItemList = new ArrayList<>();
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    ArrayList<SeatItem> coHostList = new ArrayList<>();
    List<GiftRoot.GiftItem> giftList = new ArrayList<>();
    GiftReceiveAdapter giftReceiveAdapter = new GiftReceiveAdapter();

    private void scrollAdapterLogic() {
        binding.rvComments.scrollToPosition(0);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewModel.liveStramCommentAdapter.addSingleComment(Demo_contents.getLiveStreamComment().get(0));
            binding.rvComments.scrollToPosition(0);
            Random random = new Random();
            int randomNumber = random.nextInt(50) + 1;
            binding.tvViewUserCount.setText(String.valueOf(randomNumber));
            handler.postDelayed(this, 5000);
        }
    };

    AudioRoomHandler audioRoomHandler = new AudioRoomHandler() {
        @Override
        public void onLiveEndByEnd(Object[] args) {

        }

        @Override
        public void onComment(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    Log.d(TAG, "commentlister : " + args[0]);
                    String data = args[0].toString();
                    if (!data.isEmpty()) {
                        LiveStramComment liveStramComment = new Gson().fromJson(data, LiveStramComment.class);
                        if (liveStramComment != null) {
                            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                            scrollAdapterLogic();
                        }
                    }
                });
            }
        }

        @Override
        public void onReactionReceived(Object[] args1) {
            Log.d(TAG, "onReactionRecived: ");
            runOnUiThread(() -> {
                if (args1[0] != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(args1[0].toString());
                        if (jsonObject.getInt("position") == -1) {
                            UserRoot.User user = new Gson().fromJson(jsonObject.getString("user"), UserRoot.User.class);
                            LiveStramComment liveStramComment = new LiveStramComment("", user, false, host.getLiveStreamingId(), jsonObject.getString("image"), "reaction", "");

                            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                            scrollAdapterLogic();

                        } else if (jsonObject.getInt("position") == -2) {
                            setUpReaction(jsonObject.getString("image"), binding.imgHostReaction);
                        } else {
                            RecyclerView.LayoutManager layoutManager = binding.rvSeat.getLayoutManager();
                            if (layoutManager instanceof LinearLayoutManager) {
                                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                                int position = jsonObject.getInt("position"); // Replace 0 with the position of the item you want
                                // Get the View at the specified position from the LayoutManager
                                View itemView = linearLayoutManager.findViewByPosition(position);
                                // Now you can use itemView to access the ViewBinding object if you have one
                                if (itemView != null) {
                                    @NonNull ItemSeatBinding seatBinding = DataBindingUtil.bind(itemView);
                                    setUpReaction(jsonObject.getString("image"), seatBinding.imgHostReaction);
                                }
                            }

                        }

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        public void onRoomNameChange(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        binding.tvName.setText(jsonObject.getString("roomName"));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        @Override
        public void onWelcomeMessage(Object[] args) {

        }

        @Override
        public void onRoomImageChange(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    host.setRoomImage(args[0].toString());
                    Glide.with(WatchAudioLiveActivity.this).load(args[0].toString()).into(binding.imgProfile);
                }
            });
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onGift(Object[] args) {
            runOnUiThread(() -> {

                Log.d(TAG, "giftloister : " + args[0].toString());
                if (args[0] != null) {
                    String data = args[0].toString();
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.get("gift") != null) {
                            GiftRoot.GiftItem giftData = new Gson().fromJson(jsonObject.get("gift").toString(), GiftRoot.GiftItem.class);
                            if (giftData != null) {
                                String finalGiftLink = null;
                                List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftData.getCategory());
                                for (int i = 0; i < giftItemList.size(); i++) {
                                    if (giftData.getId().equals(giftItemList.get(i).getId())) {
                                        finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                                    }
                                }
                                Log.d(TAG, "onGift: finalGiftLink ====== " + finalGiftLink);
                                String name = jsonObject.getString("userName").toString();
                                giftData.setName(name);
                                giftList.clear();
                                giftList.add(giftData);

                                if (giftData.getType() == 2) {
                                    binding.svgaImage.setVisibility(View.VISIBLE);
                                    SVGAImageView imageView = binding.svgaImage;
                                    SVGAParser parser = new SVGAParser(WatchAudioLiveActivity.this);
                                    try {
                                        parser.decodeFromURL(new URL(finalGiftLink), new SVGAParser.ParseCompletion() {
                                            @Override
                                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                                imageView.setImageDrawable(drawable);
                                                imageView.startAnimation();
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        }, list -> {

                                        });
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    new Handler(Looper.myLooper()).postDelayed(() -> {
                                        binding.svgaImage.setVisibility(View.GONE);
                                        binding.svgaImage.clear();
                                    }, 6000);

                                } else {
                                    Log.d(TAG, "onGift: els ma aave cehe ");
//                                    binding.lytGift.setVisibility(View.VISIBLE);
//                                    binding.imgGift.setVisibility(View.VISIBLE);
//                                    binding.imgGiftCount.setVisibility(View.VISIBLE);
//                                    binding.tvGiftUserName.setVisibility(View.VISIBLE);
//                                    Glide.with(WatchAudioLiveActivity.this).load(finalGiftLink).thumbnail(Glide.with(WatchAudioLiveActivity.this).load(R.drawable.loadergif)).into(binding.imgGift);
//                                    Glide.with(binding.imgGiftCount).load(RayziUtils.getImageFromNumber(giftData.getCount())).into(binding.imgGiftCount);
//                                    String name = jsonObject.getString("userName").toString();
//                                    binding.tvGiftUserName.setText(name + " Sent a gift");
//
//                                    runOnUiThread(() -> {
//                                        new Handler().postDelayed(() -> {
//                                            Log.d(TAG, "onGift: handler ma a aav ech e");
//                                            binding.lytGift.setVisibility(View.GONE);
//                                            binding.tvGiftUserName.setVisibility(View.GONE);
//                                            binding.tvGiftUserName.setText("");
//                                            binding.imgGift.setImageDrawable(null);
//                                            binding.imgGiftCount.setImageDrawable(null);
//                                        }, 3000);
//                                    });

                                    giftReceiveAdapter.addData(giftList);
                                    GiftRoot.GiftItem finalGiftData = giftData;
                                    new Handler().postDelayed(() -> {
                                        giftReceiveAdapter.remove(finalGiftData);
                                        giftList.remove(finalGiftData);
                                    }, 3000);

//                                    makeSound();
                                }


                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (args[1] != null) {  // gift sender user
                    Log.d(TAG, "user string   : " + args[1].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[1].toString());
                        UserRoot.User user = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (user != null) {
                            Log.d(TAG, ":getted user    " + user.toString());
                            if (user.getId().equals(sessionManager.getUser().getId())) {
                                sessionManager.saveUser(user);
                                giftViewModel.localUserCoin.setValue(user.getDiamond());
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                if (args[2] != null) {   // host
                    Log.d(TAG, "host string   : " + args[2].toString());
                    try {
                        JSONObject jsonObject = new JSONObject(args[2].toString());
                        UserRoot.User host = new Gson().fromJson(jsonObject.toString(), UserRoot.User.class);
                        if (host != null) {
                            Log.d(TAG, ":getted host    " + host.toString());

                            binding.tvRcoins.setText(String.valueOf(host.getRCoin()));

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }


            });

        }

        @Override
        public void onView(Object[] args) {
            runOnUiThread(() -> {
                Object args1 = args[0];
                Log.d(TAG, "viewListner : " + args1.toString());

                try {
                    JSONArray jsonArray = new JSONArray(args1.toString());
                    JSONArray finalArray = new JSONArray();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getBoolean("isAdd")) {
                            finalArray.put(jsonObject);
                        }
                    }

                    viewModel.liveViewAdapter.addData(finalArray);
                    viewModel.liveViewUserAdapter.addData(jsonArray);
                    binding.tvViewUserCount.setText(String.valueOf(finalArray.length()));
                    Log.d(TAG, "views2 : " + jsonArray);
                } catch (JSONException e) {
                    Log.d(TAG, "207: ");
                    e.printStackTrace();
                }
            });

        }

        @Override
        public void onAddRequested(Object[] args) {

        }

        @Override
        public void onDeclineInvite(Object[] args) {

        }

        @Override
        public void onAddParticipants(Object[] args) {

        }

        @Override
        public void onLessParticipants(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {
                    String data = args[0].toString();
                    Log.d(TAG, "onLessParticipants: data ==== " + data);
                    rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                });

            }
        }

        @Override
        public void onMuteSeat(Object[] args) {
            Log.d(TAG, "onMuteSeat: " + args[0].toString());
            if (args[0] != null) {
                runOnUiThread(() -> {
                    String data = args[0].toString();
                    try {
                        JSONObject json = new JSONObject(data);
                        if (json.has("agoraId")) {
                            isMuteByHost = json.getBoolean("mute");
                            Log.d(TAG, "onMuteSeat: isMute ===== " + isMuteByHost);
                            if (isMuteByHost) {
                                binding.btnMute.setEnabled(false);
                                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute_blocked));
                            } else {
                                binding.btnMute.setEnabled(true);
                                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.unmute));
                            }

                            rtcEngine().muteLocalAudioStream(isMuteByHost);
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        @Override
        public void onLockSeat(Object[] args) {

        }

        @Override
        public void onAllSeatLock(Object[] args) {

        }

        @Override
        public void onChangeTheme(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Log.d(TAG, "call: changetheme" + args[0]);
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        String image = jsonObject.getString("background");
                        Glide.with(WatchAudioLiveActivity.this).load(image).into(binding.mainImg);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public void onSeat(Object[] args) {
            if (args[0] != null) {
                Log.d(TAG, "onSeat: listener ma listen thya che =================" + args[0].toString());
                Log.d("onAudioVolumeIndication", ": seat listner" + args[0]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = args[0].toString();
                        Log.d("onAudioVolumeIndication", "initLister: usr sty1 " + data);
                        JsonParser parser = new JsonParser();
                        JsonElement mJson = parser.parse(data);
                        Log.d("onAudioVolumeIndication", "initLister: usr sty2 " + mJson);
                        Gson gson = new Gson();
                        LiveStreamRoot.LiveUser userData = gson.fromJson(mJson, LiveStreamRoot.LiveUser.class);
                        host.setSeat(userData.getSeat());
                        bookedSeatItemList = userData.getSeat();
                        if (userData.getSeat().size() >= 15) {
                            gridLayoutManager.setSpanCount(5); // Change to 5 columns when item count is 16 or more
                        } else {
                            gridLayoutManager.setSpanCount(4); // Default back to 4 columns for less than 16 items
                        }

                        seatAdapter.updateData(userData.getSeat());
                        coHostList.clear();
                        for (int i = 0; i < userData.getSeat().size(); i++) {
                            if (userData.getSeat().get(i).getUserId() != null) {
                                coHostList.add(userData.getSeat().get(i));
                            }
                        }

                    }
                });
            }
            if (args[1] != null) {
                String data = args[1].toString();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    if (jsonObject.has("agoraId") && jsonObject.has("mute")) {
                        int agoraId = Integer.parseInt(jsonObject.getString("agoraId"));
                        boolean isMute = jsonObject.getBoolean("mute");
                        Log.d(TAG, "onSeat: args[1] == agoraId " + agoraId);
                        Log.d(TAG, "onSeat: args[1] == isMute " + isMute);
                        rtcEngine().muteRemoteAudioStream(agoraId, isMute);
                        binding.btnMute.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.color_grey)));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        @Override
        public void onBlock(Object[] args) {
            Log.d(TAG, "onBlock: " + args[0].toString());
            runOnUiThread(() -> {
                if (args[0] != null) {
                    Object data = args[0];
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
                        JSONArray blockedList = jsonObject.getJSONArray("blocked");
                        for (int i = 0; i < blockedList.length(); i++) {
                            Log.d(TAG, "block user : " + blockedList.get(i).toString());
                            if (blockedList.get(i).toString().equals(sessionManager.getUser().getId())) {
                                Toast.makeText(WatchAudioLiveActivity.this, "You are blocked by host", Toast.LENGTH_SHORT).show();
                                new Handler(Looper.myLooper()).postDelayed(() -> endLive(), 500);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public void onGetUser(Object[] args) {
            runOnUiThread(() -> {
                if (args[0] != null) {
                    String data = args[0].toString();
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);

                    if (userData != null) {
                        if (userData.getUserId().equals(host.getLiveUserId())) {
                            userProfileBottomSheet.show(false, userData, host.getLiveStreamingId());
                        } else {
                            userProfileBottomSheet.show(false, userData, "");
                        }
                    }
                }
            });

        }

        @Override
        public void onInvite(Object[] args) {
            runOnUiThread(() -> {

                if (args[0] != null) {
                    try {
                        JSONObject jsonObject1 = new JSONObject(args[0].toString());
                        Log.d(TAG, "call:inviteListner " + args[0]);
                        String name = jsonObject1.getString("name");
                        String image = jsonObject1.getString("image");
                        String id = jsonObject1.getString("userId");

                        if (id.equalsIgnoreCase(sessionManager.getUser().getId())) {
                            new PopupBuilder(WatchAudioLiveActivity.this).showPkRequestPopup("Audio request received from" + host.getName(), host.getImage(), "Accept", "Decline", new PopupBuilder.OnMultButtonPopupLister() {
                                @Override
                                public void onClickCountinue() {
                                    JsonObject jsonObject = new JsonObject();
                                    try {
                                        jsonObject.addProperty("position", jsonObject1.getInt("position"));
                                        jsonObject.addProperty("liveUserMongoId", host.getId());
                                        jsonObject.addProperty("userId", sessionManager.getUser().getId());
                                        jsonObject.addProperty("name", sessionManager.getUser().getName());
                                        jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                                        jsonObject.addProperty("agoraUid", MY_UID);
                                        jsonObject.addProperty("liveStreamingId", host.getLiveStreamingId());
                                        jsonObject.addProperty("image", sessionManager.getUser().getImage());

                                        MySocketManager.getInstance().getSocet().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                @Override
                                public void onClickCancel() {

                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            });

        }

        @Override
        public void onLiveEnd(Object[] argr) {
            if (argr[0] != null) {
                if (!isFinishing()) {
                    endLive();
                }
            }
        }
    };

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_watch_audio_live);

        MySocketManager.getInstance().addAudioRoomHandler(audioRoomHandler);

        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        ReactionsViewModel reactionsViewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReactionsViewModel()).createFor()).get(ReactionsViewModel.class);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new WatchLiveViewModel()).createFor()).get(WatchLiveViewModel.class);

        sessionManager = new SessionManager(this);
        binding.setViewModel(viewModel);
        viewModel.initLister();
        giftViewModel.getGiftCategory();

        bottomSheetReactions = new BottomSheetReactions(this);
        reactionsViewModel.loadReactions(bottomSheetReactions::loadData);

        Intent intent = getIntent();
        String userStr = intent.getStringExtra(Const.DATA);
        boolean isNotification = intent.getBooleanExtra(Const.isNotification, false);

        if (isNotification) {
            ((MainApplication) getApplication()).initAgora(WatchAudioLiveActivity.this);
            if (!MySocketManager.getInstance().globalConnecting || !MySocketManager.getInstance().globalConnected) {
                getApp().initGlobalSocket();
            }
        }

        if (userStr != null && !userStr.isEmpty()) {
            host = new Gson().fromJson(userStr, LiveUserRoot.UsersItem.class);
            token = host.getToken();

            Glide.with(this).load(host.getImage()).apply(MainApplication.requestOptions).circleCrop().into(binding.mainHostProfileImage);
            binding.mainHostnameCount.setText(host.getName());
            binding.tvRcoins.setText(String.valueOf(host.getRCoin()));

            Glide.with(this).load((host.getRoomImage() == null) ? host.getImage() : host.getRoomImage()).apply(MainApplication.requestOptions).circleCrop().into(binding.imgProfile);
            binding.tvName.setText((host.getRoomName() == null) ? host.getName() : host.getRoomName());
            binding.tvUniqueId.setText("ID: " + host.getRoomOwnerUniqueId());

            gridLayoutManager = new GridLayoutManager(this, 4);
            binding.rvSeat.setLayoutManager(gridLayoutManager);
            seatAdapter = new SeatAdapter();
            binding.rvSeat.setAdapter(seatAdapter);
            Glide.with(this).load(host.getImage()).circleCrop().into(binding.mainHostProfileImage);
            binding.mainHostnameCount.setText(host.getName());

            if (host.getBackground() == null || host.getBackground().isEmpty()) {
                Glide.with(WatchAudioLiveActivity.this).load(R.drawable.default_bg_audioroom).into(binding.mainImg);
            } else {
                Glide.with(WatchAudioLiveActivity.this).load(host.getBackground()).into(binding.mainImg);
            }

            MY_UID = new Random().nextInt(500) + 2;

            initView();

            if (host.getPrivateCode() != 0) {

                float radius = 1f;
                View decorView = getWindow().getDecorView();
                ViewGroup rootView = decorView.findViewById(android.R.id.content);
                Drawable windowBackground = decorView.getBackground();

                binding.blurView.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                        .setFrameClearDrawable(windowBackground) // Optional
                        .setBlurRadius(radius);

                new BottomSheetAudioRoomPasscode(this, host, new BottomSheetAudioRoomPasscode.OnWelcomeMessageSubmittedListener() {
                    @Override
                    public void OnWelcomeMessageSubmitted(int privateCode) {
                        if (privateCode != 0) {
                            addLessView(true);
                            joinChannel();
                            binding.blurView.setVisibility(View.GONE);
                        } else {
                            rtcEngine().leaveChannel();
                            finish();
                        }
                    }
                });
            } else {
                addLessView(true);
                joinChannel();
            }

            binding.rvGift.setAdapter(giftReceiveAdapter);

            initLister();

//            bookedSeatItemList=host.getSeat();
//            updateSeat(bookedSeatItemList);

            seatAdapter.addData(host.getSeat());
            binding.rvComments.scrollToPosition(0);
        }

        if (host.isIsFake()) {

            Random random = new Random();
            int listSize = host.getSeat().size();
            int reservedCount = 0;
            int minReserved = 3;
            int maxReserved = 6;

            while (reservedCount < minReserved || (reservedCount < maxReserved && random.nextBoolean())) {
                int randomIndex = random.nextInt(listSize); // Get a random index within the list size
                SeatItem seatItem = host.getSeat().get(randomIndex);
                if (!seatItem.isReserved()) {
                    seatItem.setReserved(true);
                    seatItem.setImage(Demo_contents.getUsers(true).get(0).getImage());
                    seatItem.setName(Demo_contents.getUsers(true).get(0).getName());
                    host.getSeat().set(randomIndex, seatItem);
                    reservedCount++; // Increase the count of reserved seats
                    Log.d(TAG, "Reserved seat at index: " + randomIndex);
                }
            }
            seatAdapter.updateData(host.getSeat());

            viewModel.liveStramCommentAdapter.addSingleComment(new LiveStramComment("", Demo_contents.getUsers(true).get(0), true, "", "", "comment", ""));
            handler.postDelayed(runnable, 5000);

            binding.tvViewUserCount.setText(String.valueOf(host.getView()));

        }

        viewModel.liveStramCommentAdapter.addSingleComment(null);
        LiveStramComment liveStramComment1 = new LiveStramComment("Announcement: Welcome to room", sessionManager.getUser(), true, host.getLiveStreamingId(), "", "comment", "");
        viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment1);
        LiveStramComment liveStramComment = new LiveStramComment("", sessionManager.getUser(), true, host.getLiveStreamingId(), "", "comment", "");
        MySocketManager.getInstance().getSocet().emit(Const.EVENT_COMMENT, new Gson().toJson(liveStramComment));

    }

    private void becomeHost(SeatItem seatItem, boolean fromOnSeatClicked) {
        isHost = true;
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
        rtcEngine().enableAudio();
        rtcEngine().disableVideo();
        Log.d(TAG, "becomeHost: isMute ======n " + seatItem.isMute());
        Log.d(TAG, "becomeHost: isMuteByHost === " + isMuteByHost);
        if (!isMuteByHost) {
            if (fromOnSeatClicked) {
                new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
                    if (seatItem.isMute()) {
                        Log.d(TAG, "becomeHost: if ma jay che ==");
                        binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute));
                        rtcEngine().muteLocalAudioStream(true);
                    } else {
                        binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.unmute));
                        rtcEngine().muteLocalAudioStream(false);
                    }
                }, 500);
            }
        }
    }

    private void addLessView(boolean isAdd) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", host.getLiveStreamingId());
            jsonObject.put("liveUserMongoId", host.getId());
            jsonObject.put("userId", sessionManager.getUser().getId());
            jsonObject.put("isVIP", sessionManager.getUser().isIsVIP());
            jsonObject.put("image", sessionManager.getUser().getImage());
            jsonObject.put("name", sessionManager.getUser().getName());
            jsonObject.put("gender", sessionManager.getUser().getGender());
            jsonObject.put("country", sessionManager.getUser().getCountry());

            if (isAdd) {
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_ADDVIEW, jsonObject);
            } else {
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESSVIEW, jsonObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void joinChannel() {
        try {
            if (TextUtils.isEmpty(token) || TextUtils.equals(token, "#YOUR ACCESS TOKEN#")) {
                token = null; // default, no token
            }
            rtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            rtcEngine().disableVideo();
            rtcEngine().enableAudioVolumeIndication(1000, 3, true); // atyare kon bole chhe ae detect karva mate
            // configVideo();
            Log.d(TAG, "joinChannel: " + config().getChannelName());
            rtcEngine().joinChannel(token, host.getChannel(), "", MY_UID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        emojiBottomsheetFragment = new EmojiBottomSheetFragment(true);
        userProfileBottomSheet = new UserProfileBottomSheet(this);

        if (rtcEngine() == null) {
            Log.d(TAG, "initView: rtc engine null");
            return;
        }
        rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
        isHost = false;

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        endLive();
    }

    private void endLive() {

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("position", selfPosition);
        jsonObject.addProperty("liveUserMongoId", host.getId());
        jsonObject.addProperty("liveStreamingId", host.getLiveStreamingId());

        MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
        rtcEngine().leaveChannel();
        addLessView(false);
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (host.isIsFake()) {
            handler.removeCallbacks(runnable);
        }
        MySocketManager.getInstance().removeAudioRoomHandler(audioRoomHandler);
        statsManager().clearAllData();
    }

    private void initLister() {


        binding.btnMute.setOnClickListener(v -> {
            viewModel.isMuted = !viewModel.isMuted;
            int kk = rtcEngine().muteLocalAudioStream(viewModel.isMuted);
            Log.e(TAG, "initLister: kkk  " + kk);
            if (viewModel.isMuted) {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.mute));
//                Toast.makeText(WatchAudioLiveActivity.this, "Muted", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(WatchAudioLiveActivity.this, "Unmuted", Toast.LENGTH_SHORT).show();
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.unmute));
            }
        });

        viewModel.clickedComment.observe(this, user -> {
            getUser(user.getId());
        });
        viewModel.clickedUser.observe(this, user -> {
            try {
                getUser(user.get("userId").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        binding.btnReaction.setOnClickListener(view -> bottomSheetReactions.show());
        bottomSheetReactions.setOnReactionClickListner(reaction -> {
            Log.d(TAG, "initLister: " + reaction.getImage());

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("liveStreamingId", host.getLiveStreamingId());
                jsonObject.put("position", selfPosition);// seatpos
                jsonObject.put("image", reaction.getImage());
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                MySocketManager.getInstance().getSocket().emit(Const.EVENTSENDREACTION, jsonObject);
            } catch (Exception o) {
                o.printStackTrace();
            }
        });

        binding.lytHost.setOnClickListener(v -> getUser(host.getLiveUserId()));

        giftViewModel.finelGift.observe(this, giftItem -> {
            Log.d(TAG, "initLister: hello 123 main");
            if (giftItem != null) {
                int totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(WatchAudioLiveActivity.this, "You not have enough diamonds to send gift", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!host.isIsFake()) {

                    if (!giftViewModel.userListAdapter.getUsers().isEmpty()) {
                        List<String> selectedUsers = giftViewModel.userListAdapter.getUsers().stream()
                                .filter(UserSelectableClass::isSelected)
                                .map(user -> user.getSeatItem().getUserId())
                                .collect(Collectors.toList());

                        List<String> selectedUserName = giftViewModel.userListAdapter.getUsers().stream()
                                .filter(UserSelectableClass::isSelected)
                                .map(user -> user.getSeatItem().getName())
                                .collect(Collectors.toList());

                        if (selectedUsers.isEmpty()) {
                            Toast.makeText(this, "Select at least one user", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        try {

                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("senderUserId", sessionManager.getUser().getId());
                            jsonObject.put("receiverUserId", Arrays.toString(selectedUsers.toArray()));
                            jsonObject.put("receiverUserName", Arrays.toString(selectedUserName.toArray()));
                            jsonObject.put("hostId", host.getLiveUserId());
                            jsonObject.put("liveStreamingId", host.getLiveStreamingId());
                            jsonObject.put("userName", sessionManager.getUser().getName());
                            jsonObject.put("coin", totalCoin);
                            jsonObject.put("gift", new Gson().toJson(giftItem));
                            jsonObject.put("roomId", host.getId());
                            MySocketManager.getInstance().getSocket().emit(Const.EVENT_NORMALUSER_GIFT, jsonObject);
                            emojiBottomsheetFragment.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, "Don't have user to sent a gift wait for user", Toast.LENGTH_SHORT).show();
                    }

//                        JSONObject jsonObject = new JSONObject();
//                        jsonObject.put("senderUserId", sessionManager.getUser().getId());
//                        jsonObject.put("receiverUserId", host.getLiveUserId());
//                        jsonObject.put("hostId", host.getLiveUserId());
//                        jsonObject.put("liveStreamingId", host.getLiveStreamingId());
//                        jsonObject.put("userName", sessionManager.getUser().getName());
//                        jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
//                        jsonObject.put("gift", new Gson().toJson(giftItem));
//                        MySocketManager.getInstance().getSocet().emit(Const.EVENT_NORMALUSER_GIFT, jsonObject);
                } else {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("senderUserId", sessionManager.getUser().getId());
                    jsonObject.addProperty("coin", giftItem.getCoin() * giftItem.getCount());
                    jsonObject.addProperty("userName", sessionManager.getUser().getName());
                    jsonObject.addProperty("type", "live");

                    Call<UserRoot> call = RetrofitBuilder.create().sendGiftToFakeHost(jsonObject);
                    call.enqueue(new Callback<>() {
                        @Override
                        public void onResponse(Call<UserRoot> call, Response<UserRoot> response) {
                            if (response.code() == 200 && response.body().isStatus()) {

                                sessionManager.saveUser(response.body().getUser());

                                GiftRoot.GiftItem giftData = giftItem;
                                String finalGiftLink = null;
                                List<GiftRoot.GiftItem> giftItemList = sessionManager.getGiftsList(giftData.getCategory());
                                for (int i = 0; i < giftItemList.size(); i++) {
                                    if (giftData.getId().equals(giftItemList.get(i).getId())) {
                                        finalGiftLink = BuildConfig.BASE_URL + giftItemList.get(i).getImage();
                                    }
                                }
                                Log.d(TAG, "onGift: finalGiftLink ====== " + finalGiftLink);

                                giftList.clear();
                                giftList.add(giftData);

                                if (giftData.getType() == 2) {
                                    binding.svgaImage.setVisibility(View.VISIBLE);
                                    SVGAImageView imageView = binding.svgaImage;
                                    SVGAParser parser = new SVGAParser(WatchAudioLiveActivity.this);
                                    try {
                                        parser.decodeFromURL(new URL(finalGiftLink), new SVGAParser.ParseCompletion() {
                                            @Override
                                            public void onComplete(@NonNull SVGAVideoEntity svgaVideoEntity) {
                                                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                                                imageView.setImageDrawable(drawable);
                                                imageView.startAnimation();
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        }, list -> {

                                        });
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    new Handler(Looper.myLooper()).postDelayed(() -> {
                                        binding.svgaImage.setVisibility(View.GONE);
                                        binding.svgaImage.clear();
                                    }, 6000);

                                } else {
                                    Log.d(TAG, "onGift: els ma aave cehe ");
//                                    binding.lytGift.setVisibility(View.VISIBLE);
//                                    binding.imgGift.setVisibility(View.VISIBLE);
//                                    binding.imgGiftCount.setVisibility(View.VISIBLE);
//                                    binding.tvGiftUserName.setVisibility(View.VISIBLE);
//                                    Glide.with(WatchAudioLiveActivity.this).load(finalGiftLink).thumbnail(Glide.with(WatchAudioLiveActivity.this).load(R.drawable.loadergif)).into(binding.imgGift);
//                                    Glide.with(binding.imgGiftCount).load(RayziUtils.getImageFromNumber(giftData.getCount())).into(binding.imgGiftCount);

                                    giftReceiveAdapter.addData(giftList);
                                    GiftRoot.GiftItem finalGiftData = giftData;
                                    new Handler().postDelayed(() -> {
                                        giftReceiveAdapter.remove(finalGiftData);
                                        giftList.remove(finalGiftData);
                                    }, 3000);

//                                    runOnUiThread(() -> {
//                                        new Handler().postDelayed(() -> {
//                                            Log.d(TAG, "onGift: handler ma a aav ech e");
//                                            binding.lytGift.setVisibility(View.GONE);
//                                            binding.tvGiftUserName.setVisibility(View.GONE);
//                                            binding.tvGiftUserName.setText("");
//                                            binding.imgGift.setImageDrawable(null);
//                                            binding.imgGiftCount.setImageDrawable(null);
//                                        }, 3000);
//                                    });
//                                    makeSound();
                                }

                            }
                        }

                        @Override
                        public void onFailure(Call<UserRoot> call, Throwable t) {

                        }
                    });
                }

                emojiBottomsheetFragment.dismiss();
            }

        });

        binding.btnGift.setOnClickListener(v -> {
            if (!emojiBottomsheetFragment.isAdded()) {
                giftViewModel.users.clear();
                giftViewModel.users.add(new UserSelectableClass(new SeatItem(host.getImage(), host.getCountry(), true, "Host", false, 0, false, true, host.getId(), -1, false, host.getLiveUserId())));
                host.getSeat().stream().filter(SeatItem::isReserved).map(UserSelectableClass::new).forEach(giftViewModel.users::add);
                emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
            }
        });

        if (!host.isIsFake()) {
            seatAdapter.setOnSeatClick(new SeatAdapter.onSeatClick() {
                @Override
                public void OnClickSeat(SeatItem seatItem, int position) {
                    Log.d(TAG, "OnClickSeat: isMute ====  " + seatItem.isMute());
                    if (!isMuteByHost) {
                        rtcEngine().muteLocalAudioStream(seatItem.isMute());
                    }

                    doWork(seatItem, position);
                    selfPosition = position;
                }

                @Override
                public void onMuteClick(SeatItem seatItem, int positon) {
                    Log.d(TAG, "onMuteClick: ");
//                viewModel.isMuted = !viewModel.isMuted;
                    rtcEngine().muteLocalAudioStream(!viewModel.isMuted);
//                rtcEngine().enableLocalAudio(true);
                }

                @Override
                public void onUnmuteClick(SeatItem seatItem, int position) {
                    Log.d(TAG, "onUnmuteClick: ");
                    Log.d(TAG, "onUnmuteClick: isMuteByHost ===== " + isMuteByHost);
                    if (!isMuteByHost) {
                        viewModel.isMuted = !viewModel.isMuted;
                        rtcEngine().muteLocalAudioStream(false);
                        binding.btnMute.setImageDrawable(ContextCompat.getDrawable(WatchAudioLiveActivity.this, R.drawable.unmute));
                    }
//                rtcEngine().enableLocalAudio(true);
                }

                @Override
                public void onReserved(SeatItem seatItem, int position) {
                    Log.d(TAG, "onReserved: seatItem isMute == " + seatItem.isMute());
                    becomeHost(seatItem, false);
                    selfPosition = position;
                    //todo host user ne seat aape ae aya set karvanu baki
                }

                @Override
                public void onRedervedFalse(SeatItem seatItem, int position) {
                    Log.d(TAG, "onRedervedFalse: ");
                    rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                    rtcEngine().disableAudio();
                    rtcEngine().disableVideo();
                    isHost = false;
                }

                @Override
                public void onSpeakingTrue(SeatItem seatItem, int pos, ItemSeatBinding binding) {
                    Log.e(TAG, "onSpeakingTrue: watch " + seatItem.isSpeaking());
                    if (seatItem.isSpeaking() && seatItem.getUserId() != null && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
                        binding.animationView1.setVisibility(View.VISIBLE);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.animationView1.setVisibility(View.GONE);
                            }
                        }, 3000);

                    }
                }


            });
        } else {
            seatAdapter.setOnSeatClick(new SeatAdapter.onSeatClick() {
                @Override
                public void OnClickSeat(SeatItem seatItem, int position) {

                    if (!seatItem.isReserved()) {
                        boolean userRemoved = false;
                        for (int i = 0; i < host.getSeat().size(); i++) {
                            SeatItem currentItem = host.getSeat().get(i);
                            if (currentItem != null && currentItem.getName() != null) {
                                if (currentItem.getName().equals(sessionManager.getUser().getName())) {
                                    host.getSeat().set(i, new SeatItem(null, null, null, false));
                                    userRemoved = true;
                                    Log.d(TAG, "OnClickSeat: " + i);
                                    break;
                                }
                            }
                        }
                        if (!userRemoved) {
                            Log.d(TAG, "User not found in any seat: " + sessionManager.getUser().getName());
                        }
                        SeatItem seatItem1 = host.getSeat().get(position);
                        seatItem1.setReserved(true);
                        seatItem1.setImage(sessionManager.getUser().getImage());
                        seatItem1.setName(sessionManager.getUser().getName());
                        host.getSeat().set(position, seatItem1);
                        bookedSeatItemList = host.getSeat();
                        seatAdapter.updateData(host.getSeat());
                    }
                }

                @Override
                public void onMuteClick(SeatItem seatItem, int positon) {

                }

                @Override
                public void onUnmuteClick(SeatItem seatItem, int position) {

                }

                @Override
                public void onReserved(SeatItem seatItem, int pos) {

                }

                @Override
                public void onRedervedFalse(SeatItem seatItemm, int pos) {

                }

                @Override
                public void onSpeakingTrue(SeatItem seatItem, int pos, ItemSeatBinding binding) {

                }
            });
        }

        binding.ivShare.setOnClickListener(view -> {
            BranchUniversalObject buo = new BranchUniversalObject()
                    .setCanonicalIdentifier("content/12345")
                    .setTitle("Watch My Voice Room")
                    .setContentDescription("By : " + host.getName())
                    .setContentImageUrl(host.getImage())
                    .setContentMetadata(new ContentMetadata().addCustomMetadata("type", "Voice Room").addCustomMetadata(Const.DATA, new Gson().toJson(host)));

            LinkProperties lp = new LinkProperties()
                    .setChannel("facebook")
                    .setFeature("sharing")
                    .setCampaign("content 123 launch")
                    .setStage("new user")

                    .addControlParameter("", "")
                    .addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

            buo.generateShortUrl(this, lp, (url, error) -> {
                Log.d(TAG, "initListnear: branch url" + url);
                try {
                    Log.d(TAG, "initListnear: share");
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareMessage = url;
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                    startActivity(Intent.createChooser(shareIntent, "choose one"));
                } catch (Exception e) {
                    Log.d(TAG, "initListnear: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        });

    }

    private void doWork(SeatItem seatItem, int i) {
        Log.d(TAG, "doWork: isReseved " + seatItem.isReserved());

        if (seatItem.isReserved() && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
            new PopupBuilder(WatchAudioLiveActivity.this).showRemovePopup(() -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", host.getId());
                jsonObject.addProperty("liveStreamingId", host.getLiveStreamingId());
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);
                Log.d(TAG, "doWork: become audence");
                rtcEngine().setClientRole(Constants.CLIENT_ROLE_AUDIENCE);
                //rtcEngine().disabl`eAudio();
                rtcEngine().disableVideo();
                isHost = false;

            });
            return;
        }


        if (seatItem.isReserved()) {
            Toast.makeText(this, "Please Choose another Seat", Toast.LENGTH_SHORT).show();
        } else if (seatItem.isLock()) {
            Toast.makeText(this, "This seat is Locked by host", Toast.LENGTH_SHORT).show();
        } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("position", i);
            jsonObject.addProperty("liveUserMongoId", host.getId());
            jsonObject.addProperty("liveStreamingId", host.getLiveStreamingId());
            jsonObject.addProperty("userId", sessionManager.getUser().getId());
            jsonObject.addProperty("name", sessionManager.getUser().getName());
            jsonObject.addProperty("country", sessionManager.getUser().getCountry());
            jsonObject.addProperty("agoraUid", MY_UID);
            jsonObject.addProperty("image", sessionManager.getUser().getImage());

            MySocketManager.getInstance().getSocet().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
            Log.d(TAG, "doWork: add partiicpate emit " + jsonObject);

            becomeHost(seatItem, true);

        }
    }

    private void getUser(String userId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            MySocketManager.getInstance().getSocet().emit(Const.EVENT_GET_USER, jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onClickBack(View view) {
        onBackPressed();
    }

    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            binding.etComment.setText("");
            LiveStramComment liveStramComment = new LiveStramComment(comment, sessionManager.getUser(), false, host.getLiveStreamingId(), "", "comment", "");
            MySocketManager.getInstance().getSocet().emit(Const.EVENT_COMMENT, new Gson().toJson(liveStramComment));
        }
    }

    public void onclickShare(View view) {

        BranchUniversalObject buo = new BranchUniversalObject().setCanonicalIdentifier("content/12345").setTitle("Watch Live Video").setContentDescription("By : " + host.getName()).setContentImageUrl(host.getImage()).setContentMetadata(new ContentMetadata().addCustomMetadata("type", "LIVE").addCustomMetadata(Const.DATA, new Gson().toJson(host)));
        LinkProperties lp = new LinkProperties().setChannel("facebook").setFeature("sharing").setCampaign("content 123 launch").setStage("new user").addControlParameter("", "").addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

        buo.generateShortUrl(this, lp, (url, error) -> {
            Log.d(TAG, "initListnear: branch url" + url);
            try {
                Log.d(TAG, "initListnear: share");
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareMessage = url;
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch (Exception e) {
                Log.d(TAG, "initListnear: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onErr(int err) {
        Log.d(TAG, "onErr: " + err);
    }

    @Override
    public void onConnectionLost() {
        Log.d(TAG, "onConnectionLost: ");
    }

    @Override
    public void onVideoStopped() {
        Log.d(TAG, "onVideoStopped: ");
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
        Log.d(TAG, "onFirstRemoteVideoDecoded: elapsed ===== " + elapsed);
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.d(TAG, "onLeaveChannel: stts" + stats);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: ");
        this.uuid = uid;
        Log.d(TAG, "onJoinChannelSuccess: isUserJoined  " + isUserJoined);

        runOnUiThread((() -> {
            new Handler().postDelayed(() -> {
                Log.d(TAG, "onJoinChannelSuccess: isUserJoined ====== " + isUserJoined);
                if (isUserJoined) {
                    Log.d(TAG, "onJoinChannelSuccess: live joined ");
                } else {
                    Toast.makeText(WatchAudioLiveActivity.this, "Live has ended", Toast.LENGTH_SHORT).show();
                    endLive();
                }
            }, 4000);
        }));

    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d(TAG, "onUserOffline: " + uid + " reason" + reason);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // removeRemoteUser(uid);
//                if (uid == 1) {
//                    endLive();
//                }
            }
        });
    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.d(TAG, "onUserJoined: ma jay che =================================================================");
        Log.d(TAG, "onUserJoined: " + uid + "  elapsed" + elapsed);
        binding.rvSeat.setVisibility(View.VISIBLE);
        binding.mining.setVisibility(View.GONE);
        isUserJoined = true;
    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        if (!statsManager().isEnabled()) return;


    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        if (!statsManager().isEnabled()) return;

    }

    @Override
    public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
        if (!statsManager().isEnabled()) return;

        StatsData data = statsManager().getStatsData(uid);
        if (data == null) return;

        data.setSendQuality(statsManager().qualityToString(txQuality));
        data.setRecvQuality(statsManager().qualityToString(rxQuality));
    }

    @Override
    public void onRemoteVideoStats(IRtcEngineEventHandler.RemoteVideoStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
    }

    @Override
    public void onRemoteAudioStats(IRtcEngineEventHandler.RemoteAudioStats stats) {
        if (!statsManager().isEnabled()) return;

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setAudioNetDelay(stats.networkTransportDelay);
        data.setAudioNetJitter(stats.jitterBufferDelay);
        data.setAudioLoss(stats.audioLossRate);
        data.setAudioQuality(statsManager().qualityToString(stats.quality));
    }

    @Override
    public void onFirstLocalAudioFramePublished(int elapsed) {

    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        Log.d(TAG, "onFirstRemoteAudioFrame: user join thay che ========= " + elapsed);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {

    }

    @Override
    public void onAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume) {
        runOnUiThread(() -> {
            if (totalVolume <= 0) return;

            for (int i = 0; i < speakers.length; i++) {
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: uid " + speakers[i].uid);
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: channelid" + speakers[i].channelId);
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: volumne" + speakers[i].volume);
                Log.d("onAudioVolumeIndication", "onAudioVolumeIndication: vad" + speakers[i].vad);

//                if (speakers[i].uid == 0 && bookedSeatItemList.get(selfPosition).isReserved()) {
//                    //animateLay(selfPosition);
//                    //todo lottie fervvani baki
//                }

                if (speakers[i].volume > 10) {
                    Log.e(TAG, "onAudioVolumeIndication: >>>>>>>  " + speakers[i].uid + "   " + speakers[i]);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("liveUserMongoId", host.getId());
                    jsonObject.addProperty("agoraUID", speakers[i].uid);   //TODO  speakers[i].uid check karva 37 lidhi 6
                    jsonObject.addProperty("isSpeaking", true);

//                    MySocketManager.getInstance().getSocet().emit(Const.Speaking, jsonObject);

                    Log.e(TAG, "onAudioVolumeIndication:  true  ?" + jsonObject.toString());

                    String uid = String.valueOf(speakers[i].uid);

                    uidlist.add(uid);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "run: falseemit ");
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("liveUserMongoId", host.getId());
                            jsonObject.addProperty("agoraUid", uid);
                            jsonObject.addProperty("isspeaking", false);

//                            MySocketManager.getInstance().getSocet().emit(Const.Speaking, jsonObject);
                            Log.e(TAG, "onAudioVolumeIndication:  false ? " + jsonObject.toString());

                            if (uidlist != null && !uidlist.isEmpty() && uidlist.size() > 0) {
                                uidlist.remove(uid);
                            }
                        }
                    }, 3000);
                }
            }
        });
    }

    @Override
    public void onActiveSpeaker(int uid) {
        Log.d(TAG, "onActiveSpeaker: " + uid);
    }

    @Override
    public void onAudioMixingStateChanged(int state, int reason) {

    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();
    }

    public void onClickReport(View view) {
        new BottomSheetReport_g(this, host.getLiveUserId(), () -> {
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_layout, findViewById(R.id.customtoastlyt));
            Toast toast = new Toast(getApplicationContext());
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();

        });
    }
}