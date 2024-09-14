package com.example.rayzi.audioLive;

import com.google.gson.annotations.SerializedName;

public class SeatItem {

    public SeatItem() {
    }

    public SeatItem(String seat_id, String image, String name, boolean isReserve) {
        this.id = seat_id;
        this.image = String.valueOf(image);
        this.name = name;
        this.reserved = isReserve;
    }

    public SeatItem(String image, String country, boolean reserved,
                    String name, boolean lock, int agoraUid, boolean mute,
                    boolean isSpeaking, String id, int position, boolean invite, String userId) {
        this.image = image;
        this.country = country;
        this.reserved = reserved;
        this.name = name;
        this.lock = lock;
        this.agoraUid = agoraUid;
        this.mute = mute;
        this.isSpeaking = isSpeaking;
        this.id = id;
        this.position = position;
        this.invite = invite;
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "SeatItem{" +
                "image='" + image + '\'' +
                ", country='" + country + '\'' +
                ", reserved=" + reserved +
                ", name='" + name + '\'' +
                ", lock=" + lock +
                ", agoraUid=" + agoraUid +
                ", mute=" + mute +
                ", id='" + id + '\'' +
                ", position=" + position +
                ", invite=" + invite +
                ", userId='" + userId + '\'' +
                '}';
    }

    @SerializedName("image")
    private String image;

    @SerializedName("country")
    private String country;

    @SerializedName("reserved")
    private boolean reserved;

    @SerializedName("name")
    private String name;

    @SerializedName("lock")
    private boolean lock;

    @SerializedName("agoraUid")
    private int agoraUid;

    @SerializedName("mute")
    private boolean mute;

    @SerializedName("isSpeaking")
    private boolean isSpeaking;

    @SerializedName("_id")
    private String id;

    @SerializedName("position")
    private int position;

    @SerializedName("invite")
    private boolean invite;

    @SerializedName("userId")
    private String userId;

    public String getImage() {
        return image;
    }

    public String getCountry() {
        return country;
    }

    public boolean isReserved() {
        return reserved;
    }

    public String getName() {
        return name;
    }

    public boolean isLock() {
        return lock;
    }

    public int getAgoraUid() {
        return agoraUid;
    }

    public boolean isMute() {
        return mute;
    }

    public String getId() {
        return id;
    }

    public int getPosition() {
        return position;
    }

    public boolean isInvite() {
        return invite;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isSpeaking() {
        return isSpeaking;
    }

    public void setSpeaking(boolean speaking) {
        isSpeaking = speaking;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }
}
