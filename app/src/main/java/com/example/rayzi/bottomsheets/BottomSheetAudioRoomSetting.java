package com.example.rayzi.bottomsheets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rayzi.R;
import com.example.rayzi.activity.GotoLiveActivityNew;
import com.example.rayzi.audioLive.LiveStreamRoot;
import com.example.rayzi.databinding.BottomSheetAudioroomSettingsBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class BottomSheetAudioRoomSetting {

    private final BottomSheetDialog bottomSheetDialog;

    @SuppressLint("SetTextI18n")
    public BottomSheetAudioRoomSetting(Context context, LiveStreamRoot.LiveUser liveUser, RoomSettingListener roomSettingListener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        bottomSheetDialog.setOnShowListener(dialog -> {
            BottomSheetDialog d = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = d.findViewById(R.id.design_bottom_sheet);
            BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        BottomSheetAudioroomSettingsBinding audioroomSettingsBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottom_sheet_audioroom_settings, null, false);
        bottomSheetDialog.setContentView(audioroomSettingsBinding.getRoot());
        bottomSheetDialog.show();

        if (liveUser.getPrivateCode() == 0) {
            audioroomSettingsBinding.tvTitleRoomPasscode.setVisibility(View.GONE);
            audioroomSettingsBinding.layPasscode.setVisibility(View.GONE);
        }

        Glide.with(context)
                .load(liveUser.getRoomImage())
                .into(audioroomSettingsBinding.imgRoom);

        audioroomSettingsBinding.tvSeatCount.setText(liveUser.getSeat().size() + " People");
        audioroomSettingsBinding.tvName.setText(liveUser.getRoomName());
        audioroomSettingsBinding.tvWelcomemsg.setText(liveUser.getRoomWelcome());
        audioroomSettingsBinding.tvPassCode.setText((liveUser.getPrivateCode() != 0) ? String.valueOf(liveUser.getPrivateCode()) : "");

        audioroomSettingsBinding.tvName.setOnClickListener(view -> {
            roomSettingListener.onRoomNameChanged(audioroomSettingsBinding);
        });

        audioroomSettingsBinding.tvWelcomemsg.setOnClickListener(view -> {
            roomSettingListener.onRoomWelcomeMessageChanged(audioroomSettingsBinding);
        });

        audioroomSettingsBinding.imgEdit.setOnClickListener(view -> {
            roomSettingListener.onRoomImageChanged(audioroomSettingsBinding);
            bottomSheetDialog.dismiss();

        });

        audioroomSettingsBinding.tvPassCode.setOnClickListener(view -> {
//            roomSettingListener.onRoomPasscodeChanged(audioroomSettingsBinding);

        });

        audioroomSettingsBinding.ivCopy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("", audioroomSettingsBinding.tvPassCode.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getResources().getString(R.string.Copied_successfully), Toast.LENGTH_SHORT).show();
        });

        audioroomSettingsBinding.layWheat.setOnClickListener(v -> {
            roomSettingListener.onSeatSizeChanged(audioroomSettingsBinding);
            bottomSheetDialog.dismiss();
        });

        audioroomSettingsBinding.layChangeBg.setOnClickListener(view -> {
            roomSettingListener.onRoomBackgroundChanged();
            bottomSheetDialog.dismiss();
        });

    }

    public interface RoomSettingListener {

        void onRoomNameChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding);

        void onRoomImageChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding);

        void onSeatSizeChanged(BottomSheetAudioroomSettingsBinding audioroomSettingsBinding);

        void onRoomWelcomeMessageChanged(BottomSheetAudioroomSettingsBinding audioroomSettings);

        void onRoomPasscodeChanged(BottomSheetAudioroomSettingsBinding audioroomSettings);

        void onRoomBackgroundChanged();

    }
}
