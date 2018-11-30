package fr.wildcodeschool.rateeverything;

/**
 * Created by wilder on 29/03/18.
 */

public class FollowersModel {
    private String id;
    private String mail;
    private int nbfollowers;
    private int nbphoto;
    private String photbackground;
    private String photouser;
    private String username;

    public FollowersModel() {
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getNbfollowers() {
        return nbfollowers;
    }

    public void setNbfollowers(int nbfollowers) {
        this.nbfollowers = nbfollowers;
    }

    public int getNbphoto() {
        return nbphoto;
    }

    public void setNbphoto(int nbphoto) {
        this.nbphoto = nbphoto;
    }

    public String getPhotouser() {
        return photouser;
    }

    public void setPhotouser(String photouser) {
        this.photouser = photouser;
    }

    public String getPhotbackground() {
        return photbackground;
    }

    public void setPhotbackground(String photbackground) {
        this.photbackground = photbackground;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FollowersModel(String id, String mail, int nbfollowers, int nbphoto, String photbackground, String photouser, String username) {
        this.id = id;
        this.mail = mail;
        this.nbfollowers = nbfollowers;
        this.nbphoto = nbphoto;
        this.photbackground = photbackground;
        this.photouser = photouser;
        this.username = username;
    }
}
