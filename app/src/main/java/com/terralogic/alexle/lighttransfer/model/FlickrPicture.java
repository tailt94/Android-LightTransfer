package com.terralogic.alexle.lighttransfer.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by alex.le on 26-Sep-17.
 */

public class FlickrPicture implements Serializable {
    private String id;
    private String owner;
    private String secret;
    private String server;
    private int farm;
    private String title;
    private int isPublic;
    private int isFriend;
    private int isFamily;
    private String url;

    public FlickrPicture(JSONObject json) {
        id = json.optString("id");
        owner = json.optString("owner");
        secret = json.optString("secret");
        server = json.optString("server");
        farm = json.optInt("farm");
        title = json.optString("title");
        isPublic = json.optInt("ispublic");
        isFriend = json.optInt("isfriend");
        isFamily = json.optInt("isfamily");
        url = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret + ".jpg";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getFarm() {
        return farm;
    }

    public void setFarm(int farm) {
        this.farm = farm;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(int isPublic) {
        this.isPublic = isPublic;
    }

    public int getIsFriend() {
        return isFriend;
    }

    public void setIsFriend(int isFriend) {
        this.isFriend = isFriend;
    }

    public int getIsFamily() {
        return isFamily;
    }

    public void setIsFamily(int isFamily) {
        this.isFamily = isFamily;
    }

    public String getUrl() {
        return url;
    }
}
