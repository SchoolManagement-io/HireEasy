package com.allenhouse.hireeasyuser;

public class ServantRatingModel {
    private String ratingId;
    private String userId;
    private String username;
    private String servantId;
    private float rating;
    private String review;
    private long timestamp;

    public ServantRatingModel() {
    }

    public ServantRatingModel(String ratingId, String userId, String username, String servantId, float rating, String review, long timestamp) {
        this.ratingId = ratingId;
        this.userId = userId;
        this.username = username;
        this.servantId = servantId;
        this.rating = rating;
        this.review = review;
        this.timestamp = timestamp;
    }

    public String getRatingId() {
        return ratingId;
    }

    public void setRatingId(String ratingId) {
        this.ratingId = ratingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getServantId() {
        return servantId;
    }

    public void setServantId(String servantId) {
        this.servantId = servantId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}