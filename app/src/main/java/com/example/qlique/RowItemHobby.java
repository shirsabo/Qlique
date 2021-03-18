package com.example.qlique;

public class RowItemHobby {
    private int imageId;
    private String hobby;

    public RowItemHobby(int imageId, String hobby) {
        this.imageId = imageId;
        this.hobby = hobby;
    }

    public int getImageId() {
        return imageId;
    }

    public String getHobby() {
        return hobby;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }
}
