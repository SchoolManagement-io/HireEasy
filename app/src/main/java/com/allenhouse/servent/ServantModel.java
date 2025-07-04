package com.allenhouse.servent;

public class ServantModel {
    private String id;
    private String name;
    private String mobile;
    private String currentAddress;
    private String area;
    private String experience;
    private String aadharNumber;
    private String category;
    private String gender;
    private String availability;
    private String verified;

    public ServantModel() {}

    public ServantModel(String name, String mobile, String currentAddress, String area, String experience,
                        String aadharNumber, String category, String gender, String availability, String verified) {
        this.name = name;
        this.mobile = mobile;
        this.currentAddress = currentAddress;
        this.area = area;
        this.experience = experience;
        this.aadharNumber = aadharNumber;
        this.category = category;
        this.gender = gender;
        this.availability = availability;
        this.verified = verified;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
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
    public String getVerified() { return verified; }
    public void setVerified(String verified) { this.verified = verified; }
}