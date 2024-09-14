package com.example.rayzi.bottomsheets;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.example.rayzi.R;
import com.example.rayzi.SessionManager;
import com.example.rayzi.audioLive.LiveStreamRoot;
import com.example.rayzi.databinding.BottomSheetAudioroomSettingsBinding;
import com.example.rayzi.databinding.BottomsheetAudioroomNameBinding;
import com.example.rayzi.retrofit.Const;
import com.example.rayzi.socket.MySocketManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

public class BottomSheetAudioRoomName {

    private final BottomSheetDialog bottomSheetDialog;

    public BottomSheetAudioRoomName(Context context, LiveStreamRoot.LiveUser liveUser,OnRoomNameSubmittedListener listener) {
        bottomSheetDialog = new BottomSheetDialog(context, R.style.CustomBottomSheetDialogTheme);
        bottomSheetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        BottomsheetAudioroomNameBinding audioroomNameBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.bottomsheet_audioroom_name, null, false);
        bottomSheetDialog.setContentView(audioroomNameBinding.getRoot());
        bottomSheetDialog.show();

        audioroomNameBinding.tvSubmit.setOnClickListener(v -> {
           String roomname = audioroomNameBinding.etName.getText().toString();
            listener.onRoomNameSubmitted(roomname);

            if (roomname.isEmpty()){
                Toast.makeText(context, "Please Enter Room Name", Toast.LENGTH_SHORT).show();
            }else {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("liveStreamingId", liveUser.getLiveStreamingId());
                    jsonObject.put("roomName",roomname);
                    MySocketManager.getInstance().getSocet().emit(Const.EVENT_ROOMNAME, jsonObject);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            bottomSheetDialog.dismiss();
        });
    }

    public interface OnRoomNameSubmittedListener {
        void onRoomNameSubmitted(String roomName);
    }

}
