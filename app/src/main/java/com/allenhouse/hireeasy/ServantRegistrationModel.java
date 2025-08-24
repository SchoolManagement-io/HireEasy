package com.allenhouse.hireeasy;

import android.os.Parcel;
import android.os.Parcelable;

public class ServantRegistrationModel implements Parcelable {
    private String servant_id, name, mobile, currentAddress, area, experience, aadharNumber, category, gender, availability, urgentCharge, expectedSalary, agentId, createdAt;
    private String profilePhoto;
    private boolean isVerified;

    public ServantRegistrationModel() {}

    public ServantRegistrationModel(String name, String servant_id, String mobile, String currentAddress, String area,
                                    String experience, String aadharNumber, String category, String gender,
                                    String availability, String urgentCharge, String expectedSalary, boolean isVerified,
                                    String agentId, String createdAt, String profilePhoto) {
        this.name = name;
        this.servant_id = servant_id;
        this.mobile = mobile;
        this.currentAddress = currentAddress;
        this.area = area;
        this.experience = experience;
        this.aadharNumber = aadharNumber;
        this.category = category;
        this.gender = gender;
        this.availability = availability;
        this.urgentCharge = urgentCharge;
        this.expectedSalary = expectedSalary;
        this.isVerified = isVerified;
        this.agentId = agentId;
        this.createdAt = createdAt;
        this.profilePhoto = profilePhoto;
    }

    // Parcelable implementation
    protected ServantRegistrationModel(Parcel in) {
        servant_id = in.readString();
        name = in.readString();
        mobile = in.readString();
        currentAddress = in.readString();
        area = in.readString();
        experience = in.readString();
        aadharNumber = in.readString();
        category = in.readString();
        gender = in.readString();
        availability = in.readString();
        urgentCharge = in.readString();
        expectedSalary = in.readString();
        agentId = in.readString();
        createdAt = in.readString();
        profilePhoto = in.readString();
        isVerified = in.readByte() != 0; // Read boolean as byte
    }

    public static final Creator<ServantRegistrationModel> CREATOR = new Creator<ServantRegistrationModel>() {
        @Override
        public ServantRegistrationModel createFromParcel(Parcel in) {
            return new ServantRegistrationModel(in);
        }

        @Override
        public ServantRegistrationModel[] newArray(int size) {
            return new ServantRegistrationModel[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(servant_id);
        dest.writeString(name);
        dest.writeString(mobile);
        dest.writeString(currentAddress);
        dest.writeString(area);
        dest.writeString(experience);
        dest.writeString(aadharNumber);
        dest.writeString(category);
        dest.writeString(gender);
        dest.writeString(availability);
        dest.writeString(urgentCharge);
        dest.writeString(expectedSalary);
        dest.writeString(agentId);
        dest.writeString(createdAt);
        dest.writeString(profilePhoto);
        dest.writeByte((byte) (isVerified ? 1 : 0)); // Write boolean as byte
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters and Setters
    public String getId() { return servant_id; }
    public void setId(String id) { this.servant_id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getExperience() { return experience; }
    public void setExperience(String experience) { this.experience = experience; }
    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
    public String getUrgentCharge() { return urgentCharge; }
    public void setUrgentCharge(String urgentCharge) { this.urgentCharge = urgentCharge; }
    public String getExpectedSalary() { return expectedSalary; }
    public void setExpectedSalary(String expectedSalary) { this.expectedSalary = expectedSalary; }
    public boolean getIsVerified() { return isVerified; }
    public void setIsVerified(boolean isVerified) { this.isVerified = isVerified; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
}