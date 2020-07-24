package com.example.takeit;

import com.google.firebase.database.Exclude;

public class UploadBookImage {
    private String mBookName, mImageUrl, mBookEdition, mBookYear, mBookSem;
    private String mKey, uKey;

    public UploadBookImage(){
        //empty constructor..
    }

    public UploadBookImage(String name, String imageUrl, String edition, String year, String sem){
        mBookName = name;
        mImageUrl = imageUrl;
        mBookEdition = edition;
        mBookYear = year;
        mBookSem = sem;
    }

    public String getName(){
        return mBookName;
    }
    public void setName(String name){
        mBookName = name;
    }

    public String getImageUrl(){
        return mImageUrl;
    }
    public void setImageUrl(String imageUrl){
        mImageUrl = imageUrl;
    }

    public String getEdition(){
        return mBookEdition;
    }
    public void setEdition(String edition){
        mBookEdition = edition;
    }

    public String getYear(){
        return mBookYear;
    }
    public void setYear(String year){
        mBookYear = year;
    }

    public String getSem(){
        return mBookSem;
    }
    public void setSem(String sem){
        mBookSem = sem;
    }

    @Exclude
    public String getKey(){
        return mKey;
    }

    @Exclude
    public void setKey(String key){
        mKey = key;
    }

    @Exclude
    public String getUserKey(){
        return uKey;
    }

    @Exclude
    public void setUserKey(String key){
        uKey = key;
    }

}
