package com.example.rayzi.modelclass;

import com.example.rayzi.audioLive.SeatItem;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LiveUserRoot {

    @SerializedName("message")
    private String message;

    @SerializedName("users")
    private List<UsersItem> users;

    @SerializedName("status")
    private boolean status;

    public String getMessage() {
        return message;
    }

    public List<UsersItem> getUsers() {
        return users;
    }

    public boolean isStatus() {
        return status;
    }

    public static class UsersItem {

        @SerializedName("image")
        private String image;

        @SerializedName("country")
        private String country;

        @SerializedName("countryFlagImage")
        private String countryFlagImage;

        @SerializedName("diamond")
        private int diamond;

        @SerializedName("view")
        private int view;

        public int getView() {
            return view;
        }

        public void setView(int view) {
            this.view = view;
        }

        @SerializedName("rCoin")
        private int rCoin;

        @SerializedName("isFake")
        private boolean isFake;

        public String getCountryFlagImage() {
            return countryFlagImage;
        }

        public void setCountryFlagImage(String countryFlagImage) {
            this.countryFlagImage = countryFlagImage;
        }

        public List<SeatItem> getSeat() {
            return seat;
        }

        public void setSeat(List<SeatItem> seat) {
            this.seat = seat;
        }

        @SerializedName("seat")
        private List<SeatItem> seat;

        @SerializedName("name")
        private String name;

        @SerializedName("background")
        private String background;

        public String getBackground() {
            return background;
        }

        public void setBackground(String background) {
            this.background = background;
        }

        @SerializedName("audio")
        private boolean audio;

        public boolean isAudio() {
            return audio;
        }

        public void setAudio(boolean audio) {
            this.audio = audio;
        }

        @SerializedName("channel")
        private String channel;

        @SerializedName("isVIP")
        private boolean isVIP;

        @SerializedName("username")
        private String username;

        @SerializedName("token")
        private String token;

        @SerializedName("liveUserId")
        private String liveUserId;

        @SerializedName("liveStreamingId")
        private String liveStreamingId;
        @SerializedName("_id")
        private String id;

        @SerializedName("roomName")
        private String roomName;

        @SerializedName("roomWelcome")
        private String roomWelcome;

        @SerializedName("roomImage")
        private String roomImage;

        @SerializedName("privateCode")
        private int privateCode;

        @SerializedName("roomOwnerUniqueId")
        private String roomOwnerUniqueId;

        public String getRoomOwnerUniqueId() {
            return roomOwnerUniqueId;
        }

        public int getPrivateCode() {
            return privateCode;
        }

        public void setPrivateCode(int privateCode) {
            this.privateCode = privateCode;
        }

        public String getRoomName() {
            return roomName;
        }

        public String getRoomWelcome() {
            return roomWelcome;
        }

        public String getRoomImage() {
            return roomImage;
        }

        public void setRoomName(String roomName) {
            this.roomName = roomName;
        }

        public void setRoomWelcome(String roomWelcome) {
            this.roomWelcome = roomWelcome;
        }

        public void setRoomImage(String roomImage) {
            this.roomImage = roomImage;
        }

        public String getLiveStreamingId() {
            return liveStreamingId;
        }

        public void setLiveStreamingId(String liveStreamingId) {
            this.liveStreamingId = liveStreamingId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getImage() {
            return image;
        }

        public String getCountry() {
            return country;
        }

        public int getDiamond() {
            return diamond;
        }

        public int getRCoin() {
            return rCoin;
        }

        public String getName() {
            return name;
        }

        public String getChannel() {
            return channel;
        }

        public boolean isIsVIP() {
            return isVIP;
        }

        public String getUsername() {
            return username;
        }

        public String getToken() {
            return token;
        }

        public String getLiveUserId() {
            return liveUserId;
        }

        public boolean isIsFake() {
            return isFake;
        }

        public void setImage(String image) {
            this.image = image;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public void setDiamond(int diamond) {
            this.diamond = diamond;
        }

        public void setrCoin(int rCoin) {
            this.rCoin = rCoin;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public void setVIP(boolean VIP) {
            isVIP = VIP;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public void setLiveUserId(String liveUserId) {
            this.liveUserId = liveUserId;
        }

        @Override
        public String toString() {
            return "UsersItem{" +
                    "image='" + image + '\'' +
                    ", country='" + country + '\'' +
                    ", diamond=" + diamond +
                    ", rCoin=" + rCoin +
                    ", name='" + name + '\'' +
                    ", channel='" + channel + '\'' +
                    ", isVIP=" + isVIP +
                    ", username='" + username + '\'' +
                    ", token='" + token + '\'' +
                    ", liveUserId='" + liveUserId + '\'' +
                    ", liveStreamingId='" + liveStreamingId + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }


    }
}