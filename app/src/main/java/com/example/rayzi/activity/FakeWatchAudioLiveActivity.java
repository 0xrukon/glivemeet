package com.example.rayzi.activity;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.example.rayzi.R;
import com.example.rayzi.audioLive.BottomSheetOtions;
import com.example.rayzi.audioLive.SeatAdapter;
import com.example.rayzi.audioLive.SeatItem;
import com.example.rayzi.databinding.ActivityFakeWatchAudioLiveBinding;
import com.example.rayzi.databinding.ActivityFakeWatchLiveBinding;
import com.example.rayzi.databinding.ActivityWatchAudioLiveBinding;
import com.example.rayzi.databinding.ItemSeatBinding;
import com.example.rayzi.modelclass.LiveStramComment;
import com.example.rayzi.viewModel.HostLiveViewModel;
import com.example.rayzi.viewModel.ViewModelFactory;
import com.example.rayzi.z_demo.Demo_contents;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FakeWatchAudioLiveActivity extends AppCompatActivity {

    public static int takemicpos;
    List<SeatItem> seatList1 = new ArrayList<>();
    List<SeatItem> seatList2 = new ArrayList<>();
    List<SeatItem> seatList3 = new ArrayList<>();
    SeatItem seatModalClass;
    private boolean mMuted;
    private int randomRcoin;
    private CountDownTimer timer;
    ActivityFakeWatchAudioLiveBinding binding;
    SeatAdapter seatAdapter = new SeatAdapter();
    Handler timerHandler = new Handler(Looper.myLooper());
    Handler timerHandler1 = new Handler(Looper.myLooper());
    Handler handler = new Handler();
    Runnable timerRunnable2 = new Runnable() {
        @Override
        public void run() {
            seatAdapter.updateData(seatList3);
//            timerHandler1.removeCallbacksAndMessages(null);
//            timerHandler1.removeCallbacks(timerRunnable2);

            timerHandler.postDelayed(timerRunnable, 3000);

        }
    };
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            seatAdapter.updateData(seatList2);
//            timerHandler.removeCallbacksAndMessages(null);
//            timerHandler.removeCallbacks(timerRunnable);

            timerHandler1.postDelayed(timerRunnable2, 3000);
        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            viewModel.liveStramCommentAdapter.addSingleComment(Demo_contents.getLiveStreamComment().get(0));
            binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);
            handler.postDelayed(this, 2000);
        }
    };

    int p;
    Handler ha = new Handler();
    private HostLiveViewModel viewModel;


    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void addDataListone() {

        binding.rvSeat.setAdapter(seatAdapter);

//        seatModalClass = new SeatItem("1", R.drawable.audio_sit, "", false);
//        seatList1.add(0, seatModalClass);
//        seatModalClass = new SeatItem("2", R.drawable.img1, "Miya", true);
//        seatList1.add(1, seatModalClass);
//        seatModalClass = new SeatItem("3", R.drawable.img2, "Riya", true);
//        seatList1.add(2, seatModalClass);
//        seatModalClass = new SeatItem("4", R.drawable.audio_sit, "", false);
//        seatList1.add(3, seatModalClass);
//        seatModalClass = new SeatItem("5", R.drawable.img5, "Luliya", true);
//        seatList1.add(4, seatModalClass);
//        seatModalClass = new SeatItem("6", R.drawable.audio_sit, "", false);
//        seatList1.add(5, seatModalClass);
//        seatModalClass = new SeatItem("7", R.drawable.img7, "Rehan", true);
//        seatList1.add(6, seatModalClass);
//        seatModalClass = new SeatItem("8", R.drawable.audio_sit, "", false);
//        seatList1.add(7, seatModalClass);
//        seatModalClass = new SeatItem("9", R.drawable.audio_sit, "", false);
//        seatList1.add(8, seatModalClass);
//        seatModalClass = new SeatItem("10", R.drawable.img10, "Jeny", true);
//        seatList1.add(9, seatModalClass);
//
//
//        seatModalClass = new SeatItem("1", R.drawable.audio_sit, "", false);
//        seatList2.add(0, seatModalClass);
//        seatModalClass = new SeatItem("2", R.drawable.img1, "Susmita", true);
//        seatList2.add(1, seatModalClass);
//        seatModalClass = new SeatItem("3", R.drawable.audio_sit, "", false);
//        seatList2.add(2, seatModalClass);
//        seatModalClass = new SeatItem("4", R.drawable.img2, "Rahi", true);
//        seatList2.add(3, seatModalClass);
//        seatModalClass = new SeatItem("5", R.drawable.audio_sit, "", false);
//        seatList2.add(4, seatModalClass);
//        seatModalClass = new SeatItem("6", R.drawable.img5, "Nirma", true);
//        seatList2.add(5, seatModalClass);
//        seatModalClass = new SeatItem("7", R.drawable.audio_sit, "", false);
//        seatList2.add(6, seatModalClass);
//        seatModalClass = new SeatItem("8", R.drawable.img7, "Jashvi", true);
//        seatList2.add(7, seatModalClass);
//        seatModalClass = new SeatItem("9", R.drawable.audio_sit, "", false);
//        seatList2.add(8, seatModalClass);
//        seatModalClass = new SeatItem("10", R.drawable.img10, "Mariyam", true);
//        seatList2.add(9, seatModalClass);
//
//
//        seatModalClass = new SeatItem("1", R.drawable.audio_sit, "", false);
//        seatList3.add(0, seatModalClass);
//        seatModalClass = new SeatItem("2", R.drawable.audio_sit, "", false);
//        seatList3.add(1, seatModalClass);
//        seatModalClass = new SeatItem("3", R.drawable.img2, "Hely", true);
//        seatList3.add(2, seatModalClass);
//        seatModalClass = new SeatItem("4", R.drawable.img1, "Shruti", true);
//        seatList3.add(3, seatModalClass);
//        seatModalClass = new SeatItem("5", R.drawable.img5, "Dhruvi", true);
//        seatList3.add(4, seatModalClass);
//        seatModalClass = new SeatItem("6", R.drawable.img7, "Mamta", true);
//        seatList3.add(5, seatModalClass);
//        seatModalClass = new SeatItem("7", R.drawable.audio_sit, "", false);
//        seatList3.add(6, seatModalClass);
//        seatModalClass = new SeatItem("8", R.drawable.img10, "Zeel", true);
//        seatList3.add(seatModalClass);
//        seatModalClass = new SeatItem("9", R.drawable.audio_sit, "", false);
//        seatList3.add(8, seatModalClass);
//        seatModalClass = new SeatItem("10", R.drawable.audio_sit, "", false);
//        seatList3.add(9, seatModalClass);


        seatAdapter.addData(seatList1);

        timerHandler.postDelayed(timerRunnable, 1000);

        handler.postDelayed(runnable, 2000);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_fake_watch_audio_live);
        viewModel = ViewModelProviders.of(this, new ViewModelFactory(new HostLiveViewModel()).createFor()).get(HostLiveViewModel.class);
        viewModel.initLister();

        addDataListone();
        initlistener();
//        viewModel.liveStramCommentAdapter.addSingleComment(new LiveStramComment(Demo_contents.getUsers(true).get(0).getUsername().toString(),"",Demo_contents.getBoysGirlsRandomImage().toString()));
        handler.postDelayed(runnable, 2000);

//        LiveStramComment liveStramComment = new LiveStramComment(Demo_contents.getUsers(true).get(0).getUsername().toString(),"", Demo_contents.getBoysGirlsRandomImage().toString());
//        viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment);
//        binding.rvComments.scrollToPosition(viewModel.liveStramCommentAdapter.getItemCount() - 1);

        seatAdapter.setOnSeatClick(new SeatAdapter.onSeatClick() {
            @Override
            public void OnClickSeat(SeatItem seatItem, int position) {

//                seatModalClass = new SeatItem(String.valueOf(position), R.drawable.audio_sit, "", false);
//                seatList1.add(position, seatModalClass);
//                seatAdapter.addData(seatList1);

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

    public void onClickSendComment(View view) {
        String comment = binding.etComment.getText().toString().trim();
        if (!comment.isEmpty()) {
//            LiveStramComment liveStramComment1 = new LiveStramComment(Demo_contents.getUsers(true).get(0).getUsername().toString(),comment,Demo_contents.getBoysGirlsRandomImage().toString());
//            viewModel.liveStramCommentAdapter.addSingleComment(liveStramComment1);
//            hideKeyboard(FakeWatchAudioLiveActivity.this);
            binding.etComment.setText("");
        }

    }


    private void initlistener() {
//        binding.options.setOnClickListener((View.OnClickListener) view -> new BottomSheetOtions(FakeWatchAudioLiveActivity.this, image -> {
//            Dialog dialog = new Dialog(FakeWatchAudioLiveActivity.this);
//            dialog.setContentView(R.layout.switch_popup);
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//            Button sure = dialog.findViewById(R.id.sure);
//            Button cancel = dialog.findViewById(R.id.cancel);
//
//            sure.setOnClickListener(v1 -> {
//                Glide.with(FakeWatchAudioLiveActivity.this).load(image).into(binding.mainImg);
//                dialog.dismiss();
//
//            });
//
//
//            cancel.setOnClickListener(v1 -> dialog.dismiss());
//
//            dialog.show();
//
//        }));

        binding.btnMute.setOnClickListener(v -> {
            mMuted = !mMuted;
            int res = mMuted ? R.drawable.mute : R.drawable.unmute;
            binding.btnMute.setImageResource(res);
        });

        RandomRcoinGenerater();

        binding.btnClose.setOnClickListener(view -> {
            finish();
        });

        ActivityClosingTimer();
    }

    private void ActivityClosingTimer() {
        long totalMilliseconds = 60000; // 1 minute in milliseconds
        timer = new CountDownTimer(totalMilliseconds, 1000) {
            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                finish();
                cancelTimer();
            }
        };

        timer.start();
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel(); // Cancel the timer if it's running
            timer = null; // Reset the timer object
        }
    }

    private void RandomRcoinGenerater() {
        Random random = new Random();
        randomRcoin = random.nextInt(951) + 50;

        binding.tvRcoins.setText(String.valueOf(randomRcoin));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacksAndMessages(null);
        timerHandler1.removeCallbacksAndMessages(null);
        handler.removeCallbacksAndMessages(null);
        cancelTimer();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancelTimer();
    }
}