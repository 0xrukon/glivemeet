package com.example.rayzi.audioLive;

import static android.provider.MediaStore.MediaColumns.DATA;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.rayzi.BuildConfig;
import com.example.rayzi.MainApplication;
import com.example.rayzi.R;
import com.example.rayzi.RayziUtils;
import com.example.rayzi.SessionManager;
import com.example.rayzi.adapter.GiftReceiveAdapter;
import com.example.rayzi.agora.AgoraBaseActivity;
import com.example.rayzi.agora.RtcStatsView;
import com.example.rayzi.agora.stats.RemoteStatsData;
import com.example.rayzi.agora.stats.StatsData;
import com.example.rayzi.audioLive.reactions.BottomSheetReactions;
import com.example.rayzi.audioLive.reactions.ReactionsViewModel;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomName;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomPasscode;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomSetting;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomWelcomeMsg;
import com.example.rayzi.bottomsheets.BottomSheetAudioRoomWheatMode;
import com.example.rayzi.databinding.ActivityHostLiveAudioBinding;
import com.example.rayzi.databinding.BottomSheetAudioroomSettingsBinding;
import com.example.rayzi.databinding.BottomSheetOnlineProfileBinding;
import com.example.rayzi.databinding.ItemSeatBinding;
import com.example.rayzi.emoji.EmojiBottomSheetFragment;
import com.example.rayzi.emoji.UserSelectableClass;
import com.example.rayzi.liveStreamming.LiveSummaryActivity;
import com.example.rayzi.modelclass.GiftRoot;
import com.example.rayzi.modelclass.GuestProfileRoot;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.modelclass.RestResponse;
import com.example.rayzi.modelclass.UserRoot;
import com.example.rayzi.popups.PopupBuilder;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.retrofit.RetrofitBuilder;
import com.example.rayzi.socket.AudioRoomHandler;
import com.example.rayzi.socket.MySocketManager;
import com.example.rayzi.viewModel.EmojiSheetViewModel;
import com.example.rayzi.viewModel.HostLiveViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HostLiveAudioActivity extends AgoraBaseActivity {

    ActivityHostLiveAudioBinding binding;
    public static final String TAG = HostLiveAudioActivity.class.getSimpleName();

    private SeatItem viewerListItem = null;
    SessionManager sessionManager;
    JSONArray jsonArray;
    JSONArray finalArray;
    JSONArray blockedUsersList = new JSONArray();
    SeatAdapter seatAdapter;
    private LiveStreamRoot.LiveUser liveUser;
    private EmojiSheetViewModel giftViewModel;
    EmojiBottomSheetFragment emojiBottomsheetFragment;
    private BottomSheetReactions bottomSheetReactions;
    private HostLiveViewModel viewModel;
    private RtcStatsView rtcStatsView;

    boolean isSpeak = false;
    private int uuid;
    private int selfPosition = 0;
    private int userListPosition = 0;
    private int userPosition, hostPosition;
    private String picturePath;
    GridLayoutManager gridLayoutManager;

    List<GiftRoot.GiftItem> giftList = new ArrayList<>();
    GiftReceiveAdapter giftReceiveAdapter = new GiftReceiveAdapter();


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void scrollAdapterLogic() {
        if (!binding.rvComments.canScrollVertically(1)) {
            binding.rvComments.scrollToPosition(0);
        }
    }

    AudioRoomHandler audioRoomHandler = new AudioRoomHandler() {
        @Override
        public void onLiveEndByEnd(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {

                    removeRtcVideo(0, true);

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("liveRoom", liveUser.getLiveStreamingId());
                        jsonObject.put("liveHostRoom", sessionManager.getUser().getId());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                    MySocketManager.getInstance().getSocet().emit("liveHostEnd", jsonObject);
                    PopupBuilder popupBuilder = new PopupBuilder(HostLiveAudioActivity.this);
                    popupBuilder.showLiveEndPopup(() -> {

                        startActivity(new Intent(HostLiveAudioActivity.this, LiveSummaryActivity.class).putExtra(Const.DATA, liveUser.getLiveStreamingId()));
                        finish();
                        Toast.makeText(HostLiveAudioActivity.this, "End Live Video", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "liveEndByEnd: liveEndByEnd" + args[0].toString());

                    });

                });
            }

        }

        @Override
        public void onComment(Object[] args) {
            if (args[0] != null) {
                runOnUiThread(() -> {

                    Log.d(TAG, "commentlister : " + args[0]);
                    String data = args[0].toString();
                    if (!data.isEmpty()) {
                        LiveStramComment liveStramComment = new Gson().fromJson(data.toString(), LiveStramComment.class);
                        if (liveStramComment != null) {
                            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
                            scrollAdapterLogic();
                        }
                    }
                });
            }
        }

        @Override
        public void onGift(Object[] args) {
            runOnUiThread(() -> {

                Log.d(TAG, "giftloister : " + args);
                if (args[0] != null) {
                    String data = args[0].toString();
                    try {
                        JSONObject jsonObject = new JSONObject(data.toString());
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
                                    SVGAParser parser = new SVGAParser(HostLiveAudioActivity.this);
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

                                    giftReceiveAdapter.addData(giftList);
                                    GiftRoot.GiftItem finalGiftData = giftData;
                                    new Handler().postDelayed(() -> {
                                        giftReceiveAdapter.remove(finalGiftData);
                                        giftList.remove(finalGiftData);
                                    }, 3000);

//                                    Glide.with(HostLiveAudioActivity.this).load(finalGiftLink).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.imgGift);
//                                    Glide.with(binding.imgGiftCount).load(RayziUtils.getImageFromNumber(giftData.getCount())).into(binding.imgGiftCount);
//                                    String name = jsonObject.getString("userName").toString();
//                                    binding.tvGiftUserName.setText(name + " Sent a gift");
//
//                                    binding.lytGift.setVisibility(View.VISIBLE);
//                                    binding.tvGiftUserName.setVisibility(View.VISIBLE);
//                                    new Handler(Looper.myLooper()).postDelayed(() -> {
//                                        binding.lytGift.setVisibility(View.GONE);
//                                        binding.tvGiftUserName.setVisibility(View.GONE);
//                                        binding.tvGiftUserName.setText("");
//                                        binding.imgGift.setImageDrawable(null);
//                                        binding.imgGiftCount.setImageDrawable(null);
//                                    }, 3000);
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
            HostLiveAudioActivity.this.runOnUiThread(() -> {
                Log.d(TAG, "onView: viewListner " + args.toString());

                try {
                    jsonArray = new JSONArray(args[0].toString());
                    finalArray = new JSONArray();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        if (jsonObject.getBoolean("isAdd")) {
                            finalArray.put(jsonObject);
                        }
                    }
                    viewModel.liveViewAdapter.addData(finalArray);
                    binding.tvViewUserCount.setText(String.valueOf(finalArray.length()));
                    Log.d(TAG, "views2 : " + jsonArray);
//                    binding.tvNoOneJoined.setVisibility(jsonArray.length() > 0 ? View.GONE : View.VISIBLE);

                } catch (JSONException e) {
                    Log.d(TAG, "207: ");
                    e.printStackTrace();
                }

                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("blocked", blockedUsersList);
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    MySocketManager.getInstance().getSocet().emit(Const.EVENT_BLOCK, jsonObject);
                } catch (JSONException e) {
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

        }

        @Override
        public void onMuteSeat(Object[] args) {

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
                        Glide.with(HostLiveAudioActivity.this).load(image).into(binding.mainImg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public void onSeat(Object[] args) {
            Log.d(TAG, "onSeat: args[0] =================== " + Arrays.toString(args));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (args[0] != null) {
                        String data = args[0].toString();
                        JsonParser parser = new JsonParser();
                        JsonElement mJson = parser.parse(data);
                        Gson gson = new Gson();
                        liveUser = gson.fromJson(mJson, LiveStreamRoot.LiveUser.class);

                        if (liveUser.getSeat().size() >= 15) {
                            gridLayoutManager.setSpanCount(5); // Change to 5 columns when item count is 16 or more
                        } else {
                            gridLayoutManager.setSpanCount(4); // Default back to 4 columns for less than 16 items
                        }

                        seatAdapter.updateData(liveUser.getSeat());
                        //todo first time seat set
                        // updateSeat(liveUser.getSeat());
                    }
                }
            });

        }

        @Override
        public void onBlock(Object[] args) {

        }

        @Override
        public void onGetUser(Object[] args) {
            runOnUiThread(() -> {
                Log.d(TAG, "onGetUser: " + args[0].toString());
                if (args[0] != null) {

                    String data = args[0].toString();
                    JsonParser parser = new JsonParser();
                    JsonElement mJson = parser.parse(data);
                    Gson gson = new Gson();
                    GuestProfileRoot.User userData = gson.fromJson(mJson, GuestProfileRoot.User.class);
                    if (userData != null) {
                        doUserTask(userData, userListPosition, viewerListItem);
                    }
                }
            });
        }

        @Override
        public void onInvite(Object[] args) {

        }

        @Override
        public void onLiveEnd(Object[] argr) {

        }

        @Override
        public void onReactionReceived(Object[] args1) {
            handleReactionReceived(args1);
        }

        @Override
        public void onRoomNameChange(Object[] args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(args[0].toString());
                        binding.tvName.setText(jsonObject.getString("roomName"));
                        liveUser.setRoomName(jsonObject.getString("roomName"));
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
                    liveUser.setRoomImage(args[0].toString());
                    Glide.with(HostLiveAudioActivity.this).load(args[0].toString()).into(binding.imgProfile);
                }
            });
        }
    };

    private void handleReactionReceived(Object[] args1) {
        Log.d(TAG, "onReactionRecived: ");

        runOnUiThread(() -> {
            if (args1[0] != null) {
                try {
                    JSONObject jsonObject = new JSONObject(args1[0].toString());
                    if (jsonObject.getInt("position") == -1) {
                        UserRoot.User user = new Gson().fromJson(jsonObject.getString("user"), UserRoot.User.class);
                        LiveStramComment liveStramComment = new LiveStramComment("", user, false, liveUser.getLiveStreamingId(), jsonObject.getString("image"), "reaction", "");
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_host_live_audio);

        MySocketManager.getInstance().addAudioRoomHandler(audioRoomHandler);

        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new HostLiveViewModel()).createFor()).get(HostLiveViewModel.class);
        ReactionsViewModel reactionsViewModel = ViewModelProviders.of(this, new ViewModelFactory(new ReactionsViewModel()).createFor()).get(ReactionsViewModel.class);
        giftViewModel = ViewModelProviders.of(this, new ViewModelFactory(new EmojiSheetViewModel()).createFor()).get(EmojiSheetViewModel.class);
        sessionManager = new SessionManager(this);
        giftViewModel.getGiftCategory();
        binding.setViewModel(viewModel);

        emojiBottomsheetFragment = new EmojiBottomSheetFragment(true);

        bottomSheetReactions = new BottomSheetReactions(this);
        reactionsViewModel.loadReactions(bottomSheetReactions::loadData);

        binding.rvComments.smoothScrollToPosition(0);
        viewModel.initLister();

        gridLayoutManager = new GridLayoutManager(this, 4);
        binding.rvSeat.setLayoutManager(gridLayoutManager);
        seatAdapter = new SeatAdapter();
        binding.rvSeat.setAdapter(seatAdapter);

        Intent intent = getIntent();

        if (intent != null) {
            String data = intent.getStringExtra(Const.DATA);
            String privacy = intent.getStringExtra(Const.PRIVACY);

            if (data != null && !data.isEmpty()) {
                liveUser = new Gson().fromJson(data, LiveStreamRoot.LiveUser.class);
                liveUser.setAgoraUID(1);

                Glide.with(this).load(liveUser.getImage()).apply(MainApplication.requestOptions).circleCrop().into(binding.mainHostProfileImage);
                binding.mainHostnameCount.setText(liveUser.getName());

                Glide.with(this).load(liveUser.getRoomImage()).apply(MainApplication.requestOptions).circleCrop().into(binding.imgProfile);
                binding.tvName.setText(liveUser.getRoomName());
                binding.tvRcoins.setText(String.valueOf(liveUser.getRCoin()));
                binding.tvUniqueId.setText("ID: " + liveUser.getRoomOwnerUniqueId());

                Log.d(TAG, "onCreate: live room id " + liveUser.getLiveStreamingId());
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("liveUserId", sessionManager.getUser().getId());
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    MySocketManager.getInstance().getSocet().emit(Const.LIVEROOMCONNECT, jsonObject);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                seatAdapter.addData(liveUser.getSeat());
            }
        }

        binding.btnSetting.setOnClickListener(v -> {
            new BottomSheetAudioRoomSetting(HostLiveAudioActivity.this, liveUser, new BottomSheetAudioRoomSetting.RoomSettingListener() {
                @Override
                public void onRoomNameChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding) {
                    new BottomSheetAudioRoomName(HostLiveAudioActivity.this, liveUser, audioroomSettingsBinding.tvName::setText);
                }

                @Override
                public void onRoomImageChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding) {
                    requestGalleryPermissions(HostLiveAudioActivity.this);
                }

                @Override
                public void onSeatSizeChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding) {
                    new BottomSheetAudioRoomWheatMode(HostLiveAudioActivity.this, liveUser.getSeat().size(), new BottomSheetAudioRoomWheatMode.OnSeatClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onSeatClick(int seatCount) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                                jsonObject.put("seatCount", seatCount);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                            MySocketManager.getInstance().getSocet().emit(Const.EVENT_UPDATE_SEAT_COUNT, jsonObject);
                            audioroomSettingsBinding.tvSeatCount.setText(seatCount + " People");
                        }
                    });
                }

                @Override
                public void onRoomWelcomeMessageChanged(BottomSheetAudioroomSettingsBinding audioroomSettings) {
                    new BottomSheetAudioRoomWelcomeMsg(HostLiveAudioActivity.this, liveUser, text -> {
                        audioroomSettings.tvWelcomemsg.setText(text);
                        liveUser.setRoomWelcome(text);
                    });
                }

                @Override
                public void onRoomPasscodeChanged(BottomSheetAudioroomSettingsBinding audioroomSettings) {
                }

                @Override
                public void onRoomBackgroundChanged() {
                    new BottomSheetOtions(HostLiveAudioActivity.this, image -> {

                        Glide.with(HostLiveAudioActivity.this).load(BuildConfig.BASE_URL + image).into(binding.mainImg);
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("liveUserMongoId", liveUser.getId());
                            jsonObject.put("background", image);
                            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        MySocketManager.getInstance().getSocet().emit(Const.EVENT_CHANGE_THEME, jsonObject);
                    });
                }
            });
        });

        rtcStatsView = findViewById(R.id.single_host_rtc_stats);
        binding.rvGift.setAdapter(giftReceiveAdapter);
        initLister();
        joinChannel();
        startBroadcast();

        if (liveUser.getBackground() == null || liveUser.getBackground().isEmpty()) {
            Glide.with(HostLiveAudioActivity.this).load(R.drawable.bg5).into(binding.mainImg);
        } else {
            Glide.with(HostLiveAudioActivity.this).load(liveUser.getBackground()).into(binding.mainImg);
        }

        viewModel.liveStramCommentAdapter.addSingleComment(null);
        LiveStramComment liveStramComment1 = new LiveStramComment("Announcement: Welcome to room", sessionManager.getUser(), true, liveUser.getLiveStreamingId(), "", "comment", "");
        viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment1);

    }

    private static final int REQUEST_CODE_PICK_IMAGE = 1001;
    private static final int REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE = 1002;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private void requestGalleryPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION_READ_EXTERNAL_STORAGE);
            } else {
                openGallery();
            }
        }
    }

    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString();
        if (!comment.isEmpty()) {
            binding.etComment.setText("");
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("comment", comment);
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                jsonObject.put("isJoined", false);
                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.put("userId", sessionManager.getUser().getId());

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            LiveStramComment liveStramComment = new LiveStramComment(comment, sessionManager.getUser(), false, liveUser.getLiveStreamingId(), "", "comment", "");
            MySocketManager.getInstance().getSocet().emit(Const.EVENT_COMMENT, new Gson().toJson(liveStramComment));
            hideKeyboard(HostLiveAudioActivity.this);
        }
    }

    @Override
    public void onBackPressed() {
        endLive();
    }

    private void endLive() {
        new PopupBuilder(this).liveEndPopup(new PopupBuilder.OnPopupClickListner() {
            @Override
            public void onClickCountinue() {
                confirmedEndLive();
            }
        });
    }

    private void confirmedEndLive() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
            jsonObject.put("liveUserId", liveUser.getLiveUserId());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        MySocketManager.getInstance().getSocet().emit("liveHostEnd", jsonObject);
        seatAdapter.clear();
        startActivity(new Intent(HostLiveAudioActivity.this, LiveSummaryActivity.class).putExtra(Const.DATA, liveUser.getLiveStreamingId()));
        finish();
    }

    private void joinChannel() {
        try {
            rtcEngine().setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            rtcEngine().joinChannel(liveUser.getToken(), liveUser.getChannel(), "", liveUser.getAgoraUID());
            rtcEngine().enableAudioVolumeIndication(1000, 3, true); // atyare kon bole chhe ae detect karva mate
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBroadcast() {

        Log.d(TAG, "startBroadcast: ");
        try {
            rtcEngine().setClientRole(Constants.CLIENT_ROLE_BROADCASTER);
            rtcEngine().enableAudio();
            rtcEngine().disableVideo();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initLister() {
        binding.btnMute.setOnClickListener(v -> {
            viewModel.isMuted = !viewModel.isMuted;
            rtcEngine().muteLocalAudioStream(viewModel.isMuted);
            Log.e(TAG, "initLister: >>>>>>>>>>>>>  " + viewModel.isMuted);
            if (viewModel.isMuted) {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.mute));
            } else {
                binding.btnMute.setImageDrawable(ContextCompat.getDrawable(HostLiveAudioActivity.this, R.drawable.unmute));
            }
        });

        binding.btnClose.setOnClickListener(v -> {
            endLive();
        });

        giftViewModel.finelGift.observe(this, giftItem -> {
            if (giftItem != null) {
                int totalCoin = giftItem.getCoin() * giftItem.getCount();
                if (sessionManager.getUser().getDiamond() < totalCoin) {
                    Toast.makeText(HostLiveAudioActivity.this, "You not have enough diamonds to send gift", Toast.LENGTH_SHORT).show();
                    return;
                }

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
                        jsonObject.put("hostId", liveUser.getLiveUserId());
                        jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                        jsonObject.put("userName", sessionManager.getUser().getName());
                        jsonObject.put("coin", totalCoin);
                        jsonObject.put("gift", new Gson().toJson(giftItem));
                        jsonObject.put("roomId", liveUser.getId());
                        MySocketManager.getInstance().getSocket().emit(Const.EVENT_NORMALUSER_GIFT, jsonObject);

                        emojiBottomsheetFragment.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Don't have user to sent a gift wait for user", Toast.LENGTH_SHORT).show();
                }

               /* try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("senderUserId", sessionManager.getUser().getId());
                    jsonObject.put("receiverUserId", liveUser.getLiveUserId());
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    jsonObject.put("userName", sessionManager.getUser().getName());
                    jsonObject.put("coin", giftItem.getCoin() * giftItem.getCount());
                    jsonObject.put("gift", new Gson().toJson(giftItem));
                    MySocketManager.getInstance().getSocet().emit(Const.EVENT_LIVEUSER_GIFT, jsonObject);
                    emojiBottomsheetFragment.dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
            }
        });

        seatAdapter.setOnSeatClick(new SeatAdapter.onSeatClick() {
            @Override
            public void OnClickSeat(SeatItem seatItem, int position) {
                doWork(seatItem, position);
                selfPosition = position;
            }

            @Override
            public void onMuteClick(SeatItem seatItem, int positon) {
                viewModel.isMuted = !viewModel.isMuted;
                rtcEngine().muteLocalAudioStream(!viewModel.isMuted);
            }

            @Override
            public void onUnmuteClick(SeatItem seatItem, int position) {
                viewModel.isMuted = !viewModel.isMuted;
                rtcEngine().muteLocalAudioStream(viewModel.isMuted);
            }

            @Override
            public void onReserved(SeatItem seatItem, int position) {
            }

            @Override
            public void onRedervedFalse(SeatItem seatItem, int position) {
            }

            @Override
            public void onSpeakingTrue(SeatItem seatItem, int pos, ItemSeatBinding binding) {
                Log.e(TAG, "onSpeakingTrue:  host >>>>>>>>>  " + seatItem.isSpeaking() + "  " + isSpeak);

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
                Log.e(TAG, "onSpeakingTrue: >>>>>   " + pos);
            }


        });


//        binding.options.setOnClickListener(v -> {
//            new BottomSheetOtions(HostLiveAudioActivity.this, image -> {
////                Dialog dialog = new Dialog(HostLiveAudioActivity.this);
////                dialog.setContentView(R.layout.switch_popup);
////                dialog.setCanceledOnTouchOutside(false);
////                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
////
////                Button sure = dialog.findViewById(R.id.sure);
////                Button cancel = dialog.findViewById(R.id.cancel);
//                Glide.with(HostLiveAudioActivity.this).load(BuildConfig.BASE_URL + image).into(binding.mainImg);
////                dialog.dismiss();
//
//                JSONObject jsonObject = new JSONObject();
//                try {
//                    jsonObject.put("liveUserMongoId", liveUser.getId());
//                    jsonObject.put("background", image);
//                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                MySocketManager.getInstance().getSocet().emit(Const.EVENT_CHANGE_THEME, jsonObject);
//
////                sure.setOnClickListener(v1 -> {
////
////
////                });
////                cancel.setOnClickListener(v1 -> dialog.dismiss());
////                dialog.show();
//
//            });
//        });


        binding.btnReaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetReactions.show();
                Log.d(TAG, "onClick: cliced");
            }
        });
        bottomSheetReactions.setOnReactionClickListner(reaction -> {
            Log.d(TAG, "initLister: " + reaction.getImage());

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.put("position", -2); // for host
                jsonObject.put("image", reaction.getImage());
                jsonObject.put("user", new Gson().toJson(sessionManager.getUser()));
                MySocketManager.getInstance().getSocket().emit(Const.EVENTSENDREACTION, jsonObject);
            } catch (Exception o) {
                o.printStackTrace();
            }
        });

        binding.ivShare.setOnClickListener(view -> {
            BranchUniversalObject buo = new BranchUniversalObject().setCanonicalIdentifier("content/12345").setTitle("Watch My Voice Room").setContentDescription("By : " + sessionManager.getUser().getName()).setContentImageUrl(sessionManager.getUser().getImage()).setContentMetadata(new ContentMetadata().addCustomMetadata("type", "Voice Room").addCustomMetadata(Const.DATA, new Gson().toJson(liveUser)));

            LinkProperties lp = new LinkProperties().setChannel("facebook").setFeature("sharing").setCampaign("content 123 launch").setStage("new user")
                    .addControlParameter("", "").addControlParameter("", Long.toString(Calendar.getInstance().getTimeInMillis()));

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
                }
            });
        });

    }

    private void doWork(SeatItem seatItem, int i) {
        Log.e(TAG, "doWork: >>>>>>>>>>  " + i);

        if (seatItem.isReserved() && seatItem.getUserId().equalsIgnoreCase(sessionManager.getUser().getId())) {
            Toast.makeText(this, "hello", Toast.LENGTH_SHORT).show();
            new PopupBuilder(this).showRemovePopup(() -> {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());

                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

                Log.d(TAG, "doWork: remove sit by it self" + jsonObject.toString());

            });
            return;
        }

        if (seatItem.isReserved()) {
            getUser(seatItem.getUserId(), i, seatItem);
            return;
        }

        new BottomSheetHostMic(HostLiveAudioActivity.this, seatItem, new BottomSheetHostMic.OnClickListner() {
            @Override
            public void onTakeMic() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                jsonObject.addProperty("name", sessionManager.getUser().getName());
                jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                jsonObject.addProperty("agoraUid", liveUser.getAgoraUID());
                jsonObject.addProperty("image", sessionManager.getUser().getImage());
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_ADD_PARTICIPATED, jsonObject);
                hostPosition = i;

            }

            @Override
            public void onGiveMic() {
                if (finalArray != null && finalArray.length() > 0) {
                    Log.e(TAG, "onGiveMic: Click>>>>>>>>>>>>>>>>>>>>>> if condition");

                    new BottomSheetViewersUsers(HostLiveAudioActivity.this, finalArray, userDummy -> {
                        try {
                            Log.e(TAG, "onGiveMic: >>>>>>>>>>>>>>>try catch");
                            getUser(userDummy.get("userId").toString(), i, seatItem);
                            userPosition = i;

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Toast.makeText(HostLiveAudioActivity.this, "You haven't ny user to give mic", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onLockMic() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("lock", !seatItem.isLock());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LOCK_SEAT, jsonObject);
            }

            @Override
            public void onMuteMic() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
                Log.d(TAG, "onMuteMic: liveUser.getLiveStreamingId() === " + liveUser.getLiveStreamingId());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.addProperty("agoraId", seatItem.getAgoraUid());
                jsonObject.addProperty("mute", !seatItem.isMute());
                jsonObject.addProperty("mutedUserId", seatItem.getUserId());
                rtcEngine().muteRemoteAudioStream(seatItem.getAgoraUid(), true);
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_MUTESEAT, jsonObject);
            }

            @Override
            public void onCancelClick() {

            }

            @Override
            public void onClickRemove() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", i);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());

                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

            }
        });
    }

    public void onclickGiftIcon(View view) {
        if (!emojiBottomsheetFragment.isAdded()) {
            giftViewModel.users.clear();
            giftViewModel.users.add(new UserSelectableClass(new SeatItem(liveUser.getImage(), liveUser.getCountry(), true, "Host", false, liveUser.getAgoraUID(), false, true, liveUser.getId(), -1, false, liveUser.getLiveUserId())));
            liveUser.getSeat().stream()
                    .filter(SeatItem::isReserved)
                    .map(UserSelectableClass::new)
                    .forEach(giftViewModel.users::add);
            emojiBottomsheetFragment.show(getSupportFragmentManager(), "emojifragfmetn");
        }
    }

    private void getUser(String userId, int postion, SeatItem seatItem) {
        userListPosition = postion;
        viewerListItem = seatItem;
        Log.d(TAG, "getUser: vali methos call thay che userListPosition ======" + userListPosition);
        Log.d(TAG, "getUser: vali methos call thay che viewerListItem ======" + viewerListItem);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("fromUserId", sessionManager.getUser().getId());
            jsonObject.put("toUserId", userId);
            Log.d(TAG, "getUser:request  " + jsonObject);
            MySocketManager.getInstance().getSocet().emit(Const.EVENT_GET_USER, jsonObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doUserTask(GuestProfileRoot.User userData, int postion, SeatItem seatItem) {

        new BottomSheetViewersUserProfile(this, seatItem, userData, new BottomSheetViewersUserProfile.OnClickListner() {
            @Override
            public void onUnMute(BottomSheetOnlineProfileBinding sheetDilogBinding) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", postion);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("liveUserId", liveUser.getLiveUserId());
                jsonObject.addProperty("mute", !seatItem.isMute());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.addProperty("agoraId", seatItem.getAgoraUid());
                jsonObject.addProperty("mutedUserId", seatItem.getUserId());
                Log.e(TAG, "onUnMute: >>>>>>>>>>>>>>>>  " + !seatItem.isMute());
                Log.e(TAG, "onUnMute: >>>>>>>>>>>>>>>> getAgoraUid  " + seatItem.getAgoraUid());
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_MUTESEAT, jsonObject);
                rtcEngine().muteRemoteAudioStream(seatItem.getAgoraUid(), !seatItem.isMute());

                if (seatItem.isMute()) {
                    sheetDilogBinding.txtMic.setText("Unmute mic");
                    Glide.with(HostLiveAudioActivity.this).load(R.drawable.speaker_off).into(sheetDilogBinding.mute);
                } else {
                    sheetDilogBinding.txtMic.setText("Mute mic");
                    Glide.with(HostLiveAudioActivity.this).load(R.drawable.speaker).into(sheetDilogBinding.mute);
                }
            }

            @Override
            public void onRemoveSeat() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", postion);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.addProperty("name", sessionManager.getUser().getName());
                jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                jsonObject.addProperty("agoraUid", liveUser.getAgoraUID());
                jsonObject.addProperty("removedUserID", seatItem.getUserId());
                jsonObject.addProperty("image", sessionManager.getUser().getImage());
                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

            }

            @Override
            public void onkickOut() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", postion);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("userId", sessionManager.getUser().getId());
                jsonObject.addProperty("name", sessionManager.getUser().getName());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.addProperty("country", sessionManager.getUser().getCountry());
                jsonObject.addProperty("agoraUid", liveUser.getAgoraUID());
                jsonObject.addProperty("image", sessionManager.getUser().getImage());

                MySocketManager.getInstance().getSocet().emit(Const.EVENT_LESS_PARTICIPATED, jsonObject);

                blockedUsersList.put(seatItem.getUserId());

                Log.d(TAG, "initLister: blocked " + blockedUsersList.toString());

                try {
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("blocked", blockedUsersList);
                    jsonObject1.put("liveStreamingId", liveUser.getLiveStreamingId());
                    MySocketManager.getInstance().getSocet().emit(Const.EVENT_BLOCK, jsonObject1);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void inviteUser() {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("position", postion);
                jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                jsonObject.addProperty("userId", userData.getUserId());
                jsonObject.addProperty("liveStreamingId", liveUser.getLiveStreamingId());
                jsonObject.addProperty("name", userData.getName());
                jsonObject.addProperty("country", userData.getCountry());
                jsonObject.addProperty("agoraUid", -1);
                jsonObject.addProperty("image", userData.getImage());

                MySocketManager.getInstance().getSocet().emit(Const.EVENT_ADD_REQUESTED, jsonObject);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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
    protected void onDestroy() {
        super.onDestroy();
        endLive();
        MySocketManager.getInstance().removeAudioRoomHandler(audioRoomHandler);
        statsManager().clearAllData();
    }

    @Override
    public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
    }

    @Override
    public void onLeaveChannel(IRtcEngineEventHandler.RtcStats stats) {
        Log.d(TAG, "onLeaveChannel: stts " + stats);
    }

    @Override
    public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
        Log.d(TAG, "onJoinChannelSuccess: chanel " + channel + " uid" + uid + "  elapsed " + elapsed);
        this.uuid = uid;
    }

    @Override
    public void onUserOffline(int uid, int reason) {
        Log.d(TAG, "onUserOffline: " + uid + " reason" + reason);

    }

    @Override
    public void onUserJoined(int uid, int elapsed) {
        Log.d(TAG, "onUserJoined: " + uid + "  elapsed" + elapsed);
    }

    @Override
    public void onLastmileQuality(int quality) {

    }

    @Override
    public void onLastmileProbeResult(IRtcEngineEventHandler.LastmileProbeResult result) {

    }

    @Override
    public void onLocalVideoStats(IRtcEngineEventHandler.LocalVideoStats stats) {
        Log.d(TAG, "onLocalVideoStats: ");
        if (!statsManager().isEnabled()) return;

    }

    @Override
    public void onRtcStats(IRtcEngineEventHandler.RtcStats stats) {
        runOnUiThread(() -> {
            if (rtcStatsView != null && rtcStatsView.getVisibility() == View.VISIBLE) {
                rtcStatsView.setLocalStats(stats.rxKBitRate, stats.rxPacketLossRate, stats.txKBitRate, stats.txPacketLossRate);
            }
        });

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

        RemoteStatsData data = (RemoteStatsData) statsManager().getStatsData(stats.uid);
        if (data == null) return;

        data.setWidth(stats.width);
        data.setHeight(stats.height);
        data.setFramerate(stats.rendererOutputFrameRate);
        data.setVideoDelay(stats.delay);
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

                    Log.e("onAudioVolumeIndication", "onAudioVolumeIndication: >>>>>>>  " + speakers[i].uid + "   " + speakers[i]);
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                    jsonObject.addProperty("agoraUID", speakers[i].uid);
                    jsonObject.addProperty("isSpeaking", true);

//                    MySocketManager.getInstance().getSocet().emit(Const.Speaking, jsonObject);
                    Log.e("onAudioVolumeIndication", "onAudioVolumeIndication: >>>>>>>>>>>>  emit " + jsonObject.toString());
                    String uid = String.valueOf(speakers[i].uid);
                    List<String> uidlist = new ArrayList<>();
                    uidlist.add(uid);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "run: falseemit ");
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("liveUserMongoId", liveUser.getId());
                            jsonObject.addProperty("agoraUid", uid);
                            jsonObject.addProperty("isspeaking", false);

//                            MySocketManager.getInstance().getSocet().emit(Const.Speaking, jsonObject);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK) {

            RequestOptions requestOptions = new RequestOptions();
            requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(16));

            Glide.with(this).load(data.getData()).apply(requestOptions).into(binding.imgProfile);
            String[] filePathColumn = {DATA};
            Cursor cursor = this.getContentResolver().query(data.getData(), filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();

            RequestBody liveUserIdBody = RequestBody.create(MediaType.parse("text/plain"), liveUser.getLiveUserId());

            HashMap<String, RequestBody> map = new HashMap<>();
            MultipartBody.Part body = null;

            if (picturePath != null) {
                File file = new File(picturePath);
                RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
                body = MultipartBody.Part.createFormData("roomImage", file.getName(), requestFile);
            }
            map.put("liveUserId", liveUserIdBody);

            Call<RestResponse> call = RetrofitBuilder.create().updateRoomImage(map, body);
            call.enqueue(new Callback<RestResponse>() {
                @Override
                public void onResponse(@NonNull Call<RestResponse> call, Response<RestResponse> response) {

                    if (response.body() != null) {
                        if (response.body().isStatus()) {
                            Toast.makeText(HostLiveAudioActivity.this, "Room Image Updated", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<RestResponse> call, Throwable t) {

                }
            });

        }

    }

    @Override
    public void onActiveSpeaker(int uid) {
        Log.d(TAG, "onActiveSpeaker: " + uid);
    }

    @Override
    public void onAudioMixingStateChanged(int state, int reason) {

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
        Log.d(TAG, "onFirstLocalAudioFramePublished: " + elapsed);
    }

    @Override
    public void onFirstRemoteAudioFrame(int uid, int elapsed) {
        Log.d(TAG, "onFirstRemoteAudioFrame: " + uid);
    }

    @Override
    public void onUserMuteAudio(int uid, boolean muted) {
        Log.d(TAG, "onUserMuteAudio: " + uid);
    }

    @Override
    public void finish() {
        super.finish();
        statsManager().clearAllData();

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: isFinishing ===== " + isFinishing());
//        if (!isFinishing()) {
//            confirmedEndLive();
//        }

    }
}