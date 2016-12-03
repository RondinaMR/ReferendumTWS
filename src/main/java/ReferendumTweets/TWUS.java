package ReferendumTweets;

import twitter4j.Status;

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
//    private String screenName;
//    private String name;
    private long userID;
    private String location;
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

//    public TWUS(long userID, String screenName, String name, String location) {
////        this.screenName = screenName;
////        this.name = name;
//        this.userID = userID;
//        this.location = location;
//    }
//
//    public TWUS(long userID, String screenName, String name, String location, boolean politicalPosition) {
////        this.screenName = screenName;
////        this.name = name;
//        this.userID = userID;
//        this.location = location;
//        this.politicalPosition = politicalPosition;
//        this.positionSetted = true;
//    }

//    public String getScreenName(){
//        return screenName;
//    }
//
//    public void setScreenName(String screenName) {
//        this.screenName = screenName;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
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
