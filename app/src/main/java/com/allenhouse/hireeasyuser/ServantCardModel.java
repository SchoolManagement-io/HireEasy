package com.allenhouse.hireeasyuser;

public class ServantCardModel {
    private String servantId, name, category, experience, area, expectedSalary, profilePhoto, availability, createdAt;
    private double avgRating;
    private String urgentCharge;
    private boolean isVerified;

    public ServantCardModel(String servantId, String name, String category, String experience, String area, String expectedSalary, double avgRating, String availability, String profilePhoto, boolean isVerified, String urgentCharge) {
        this.servantId = servantId;
        this.name = name;
        this.category = category;
        this.experience = experience;
        this.area = area;
        this.expectedSalary = expectedSalary;
        this.avgRating = avgRating;
        this.availability = availability;
        this.profilePhoto = profilePhoto;
        this.isVerified = isVerified;
        this.createdAt = "";
        this.urgentCharge = urgentCharge != null ? urgentCharge : "";
    }

    public String getUrgentCharge() {
        return urgentCharge;
    }

    public void setUrgentCharge(String urgentCharge) {
        this.urgentCharge = urgentCharge;
    }

    public String getServantId() { return servantId; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getExperience() { return experience; }
    public String getArea() { return area; }
    public String getExpectedSalary() { return expectedSalary; }
    public double getAvgRating() { return avgRating; }
    public String getAvailability() { return availability; }
    public String getProfilePhoto() { return profilePhoto; }
    public boolean isVerified() { return isVerified; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt != null ? createdAt : ""; }
}