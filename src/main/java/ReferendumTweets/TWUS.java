package ReferendumTweets;

import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.User;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * TWUS - TWitter USer
 *
 * Questa classe fornisce i metodi essenziali per la memorizzazione di tutti gli utenti profilati.
 * politicalPosition == FALSE -> VOTO NO!
 * politicalPosition == TRUE -> VOTO SI!
 *
 * Created by Marco on 11/06/2016.
 */
public class TWUS {

    private Date createdAt;
    private String description;
    private Entity[] descriptionURLEntities;
    private int favouritesCount;
    private int followersCount;
    private int friendsCount;//Followings
    private long userID;
    private String location;
    private String name;
    private String screenName;
    private int statusesCount;
    private boolean isGeoEnabled;

    private Date lastTweet;
    private boolean politicalPosition;
    private boolean ambiguous = false;
    private boolean positionSetted = false;
    private int balance = 0;

    public TWUS() {
    }

    public TWUS(long userID, String location, Date lastTweet) {
        this.userID = userID;
        this.location = location;
        this.lastTweet = lastTweet;
    }

    public TWUS(long userID, String location, boolean politicalPosition) {
        this.userID = userID;
        this.location = location;
        this.politicalPosition = politicalPosition;
        this.positionSetted = true;
    }

    public TWUS(User user){
        int i = 0;
        this.userID = user.getId();
        this.location = user.getLocation();

        this.createdAt = user.getCreatedAt();
        this.description = user.getDescription();

        this.descriptionURLEntities = new Entity[user.getDescriptionURLEntities().length];
        for (URLEntity he : user.getDescriptionURLEntities()){
            this.descriptionURLEntities[i] = new Entity(he);
            i++;
        }

        this.favouritesCount = user.getFavouritesCount();
        this.followersCount = user.getFollowersCount();
        this.friendsCount = user.getFollowersCount();//Followings
        this.userID = user.getId();
        this.location = user.getLocation();
        this.name = user.getName();
        this.screenName = user.getScreenName();
        this.statusesCount = user.getStatusesCount();
        this.isGeoEnabled = user.isGeoEnabled();


    }

    public TWUS(long id, String location) {
        this.userID = id;
        this.location = location;
        Calendar cal = Calendar.getInstance();
        cal.set(1970,1,1);
        this.lastTweet = cal.getTime();
    }

    public long getID() {
        return userID;
    }

    public void setID(long userID) {
        this.userID = userID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isPoliticalPosition() {return politicalPosition;}

    public void setPoliticalPosition(boolean politicalPosition) {
        this.politicalPosition = politicalPosition;
        this.positionSetted = true;
    }

    public void setAmbiguous(){
        ambiguous = true;
    }

    public void notAmbiguous(){ambiguous = false;}

    public boolean isAmbiguous(){
        return ambiguous;
    }

    public boolean isPositionSetted(){return positionSetted;}

    public Date getLastTimeTweet() {
        return lastTweet;
    }

    public void setLastTimeTweet(Date lastTweet) {
        this.lastTweet = lastTweet;
    }

    public int addBalance(){this.balance++;return this.balance;}
    public int subBalance(){this.balance--;return this.balance;}
    public int getBalance(){return this.balance;}

    @Override
    public String toString(){
        return "<" + userID + "> " /*+ screenName + ", " + name*/ + " - " + location + " = " + politicalPosition + "(" + ambiguous + ")";
    }


}
