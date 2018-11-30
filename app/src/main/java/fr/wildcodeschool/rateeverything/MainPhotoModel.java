package fr.wildcodeschool.rateeverything;

import java.util.Date;

/**
 * Created by wilder on 27/03/18.
 */

public class MainPhotoModel {
    String description;
    String iduser;
    String keyphoto;
    int nbnote;
    String photo;
    long timestamp;
    String title;
    int totalnote;
    FollowersModel followersModel;

    public MainPhotoModel() {
    }

    public MainPhotoModel(String description, String iduser, String keyphoto, int nbnote, String photo, long timestamp, String title, int totalnote) {
        this.description = description;
        this.iduser = iduser;
        this.keyphoto = keyphoto;
        this.nbnote = nbnote;
        this.photo = photo;
        this.timestamp = timestamp;
        this.title = title;
        this.totalnote = totalnote;
    }

    public FollowersModel getFollowersModel() {
        return followersModel;
    }

    public void setFollowersModel(FollowersModel followersModel) {
        this.followersModel = followersModel;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIduser() {
        return iduser;
    }

    public void setIduser(String iduser) {
        this.iduser = iduser;
    }

    public String getKeyphoto() {
        return keyphoto;
    }

    public void setKeyphoto(String keyphoto) {
        this.keyphoto = keyphoto;
    }

    public int getNbnote() {
        return nbnote;
    }

    public void setNbnote(int nbnote) {
        this.nbnote = nbnote;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTotalnote() {
        return totalnote;
    }

    public void setTotalnote(int totalnote) {
        this.totalnote = totalnote;
    }
}

