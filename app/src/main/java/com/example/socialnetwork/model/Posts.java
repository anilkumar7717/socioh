package com.example.socialnetwork.model;

public class Posts {
    public String uid, time, date, postImage, fullname, description, profileimage;
    public Object timestamp;


    public Posts() {
    }

    public Posts(String uid, String time, String date, String postimage, String fullname, String description, String profileimage, Object timestamp) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postimage;
        this.fullname = fullname;
        this.description = description;
        this.profileimage = profileimage;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
