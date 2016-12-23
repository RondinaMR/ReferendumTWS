package ReferendumTweets;

import org.mongojack.Id;
import twitter4j.*;

import java.util.Date;

/**
 * Created by marco on 18/11/2016.
 * TWitter STatus
 */
public class TWST {

    @Id
    private Long id;

    private TWUS user;
//    private Long userID;
    private Date createdAt;
    private String userScreenName;
    private String text;
    private String userLocation;
    private GeoLocation geoLocation;
    private Place place;
    private Entity[] hashtagEntities;
    private Entity[] userMentionEntities;
    private Long[] userIDMentionEntities;
    private Entity[] urlEntities;
    private Long inReplyToStatusID;
    private Long inReplyToUserID;
    private Long quotedStatusID;
    private Long retweetedStatusID;
    private String source;
    private int userFollowersCount;
    private int userStatusesCount;

    public TWST() {
    }

    public TWST(Status tweet) {
        int i = 0;
        this.id = tweet.getId();
        this.user = new TWUS(tweet.getUser());
        this.createdAt = tweet.getCreatedAt();
        this.userScreenName = tweet.getUser().getScreenName();
        this.text = tweet.getText();
        if(tweet.getUser().getLocation()!=null){
            this.userLocation = tweet.getUser().getLocation();
        }

        if(tweet.getGeoLocation()!=null){
             this.geoLocation = new GeoLocation(tweet.getGeoLocation().getLatitude(),tweet.getGeoLocation().getLongitude());
        }

        if(tweet.getPlace()!=null){
            this.place = new Place(tweet.getPlace());
        }

        hashtagEntities = new Entity[tweet.getHashtagEntities().length];
        for (HashtagEntity he : tweet.getHashtagEntities()){
            this.hashtagEntities[i] = new Entity(he);
            i++;
        }
        i=0;
        userMentionEntities = new Entity[tweet.getUserMentionEntities().length];
        for (UserMentionEntity ume : tweet.getUserMentionEntities()){
            this.userMentionEntities[i] = new Entity(ume);
            i++;
        }
        i=0;
        userIDMentionEntities = new Long[tweet.getUserMentionEntities().length];
        for (UserMentionEntity ume : tweet.getUserMentionEntities()){
            this.userIDMentionEntities[i] = ume.getId();
            i++;
        }
        i=0;
        urlEntities = new Entity[tweet.getURLEntities().length];
        for(URLEntity ue : tweet.getURLEntities()){
            this.urlEntities[i] = new Entity(ue);
            i++;
        }


        this.inReplyToStatusID = tweet.getInReplyToStatusId();
        this.inReplyToUserID = tweet.getInReplyToUserId();
        this.quotedStatusID = tweet.getQuotedStatusId();
        if(tweet.getRetweetedStatus()!=null){
            this.retweetedStatusID = tweet.getRetweetedStatus().getId();
        }
        this.source = tweet.getSource();
        this.userFollowersCount = tweet.getUser().getFollowersCount();
        this.userStatusesCount = tweet.getUser().getStatusesCount();
    }

    public Long getID() {
        return id;
    }

    public void setID(Long ID) {
        this.id = ID;
    }

//    public Long getUserID() {
//        return user.getUserID();
//    }


    public TWUS getUser() {
        return user;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public String getText() {
        return text;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public Place getPlace() {
        return place;
    }

    public Entity[] getHashtagEntities() {
        return hashtagEntities;
    }

    public Entity[] getUserMentionEntities() {
        return userMentionEntities;
    }

    public Long[] getUserIDMentionEntities() {
        return userIDMentionEntities;
    }

    public Entity[] getUrlEntities() {
        return urlEntities;
    }

    public Long getInReplyToStatusID() {
        return inReplyToStatusID;
    }

    public Long getInReplyToUserID() {
        return inReplyToUserID;
    }

    public Long getQuotedStatusID() {
        return quotedStatusID;
    }

    public Long getRetweetedStatusID() {
        return retweetedStatusID;
    }

    public String getSource() {
        return source;
    }

    public int getUserFollowersCount() {
        return userFollowersCount;
    }

    public int getUserStatusesCount() {
        return userStatusesCount;
    }

    @Override
    public String toString(){
        return "<" + this.id + " " + this.createdAt + "> ["+ this.userScreenName + " - " + this.user.getID() + " - " +
                this.userLocation + "] " + this.text + "(" + this.geoLocation + " - " + this.place + ")";
    }
}
