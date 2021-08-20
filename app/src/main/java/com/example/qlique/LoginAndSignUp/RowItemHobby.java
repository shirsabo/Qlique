package com.example.qlique.LoginAndSignUp;

/**
 * RowItemHobby
 * a row in the hobbies list.
 */
public class RowItemHobby {
    private int imageId;
    private String hobby;

    /**
     * constructor.
     * @param imageId
     * @param hobby
     */
    public RowItemHobby(int imageId, String hobby) {
        this.imageId = imageId;
        this.hobby = hobby;
    }

    /**
     * returns the image id.
     * @return
     */
    public int getImageId() {
        return imageId;
    }

    /**
     * returns the hobby.
     * @return
     */
    public String getHobby() {
        return hobby;
    }
}
