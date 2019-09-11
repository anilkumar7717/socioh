package com.example.socialnetwork.model;

public class User {
    String username, status, relationshipstatus, profileimage, gender, fullname, dob, country;

    public User() {
    }

    public User(String username, String status, String relationshipstatus, String profileimage, String gender, String fullname, String dob, String country) {
        this.username = username;
        this.status = status;
        this.relationshipstatus = relationshipstatus;
        this.profileimage = profileimage;
        this.gender = gender;
        this.fullname = fullname;
        this.dob = dob;
        this.country = country;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRelationshipstatus() {
        return relationshipstatus;
    }

    public void setRelationshipstatus(String relationshipstatus) {
        this.relationshipstatus = relationshipstatus;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
