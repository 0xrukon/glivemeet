package com.example.rayzi.emoji;


import com.example.rayzi.audioLive.SeatItem;

public class UserSelectableClass {

    public SeatItem seatItem;
    public  boolean isSelected=false;

    public UserSelectableClass(SeatItem seatItem) {
        this.seatItem = seatItem;

    }

    public SeatItem getSeatItem() {
        return seatItem;
    }

    public void setSeatItem(SeatItem seatItem) {
        this.seatItem = seatItem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
