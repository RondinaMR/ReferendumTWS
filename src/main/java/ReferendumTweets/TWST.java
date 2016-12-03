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

    private Long userID;
    private Date createdAt;
    private String userScreenName;
    private String text;
    private String userLocation;
    private GeoLocation geoLocation;
    private Place place;
    private String[] hashtagEntities;
    private String[] userMentionEntities;
    private Long[] userIDMentionEntities;
    private String[] urlEntities;
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
        this.userID = tweet.getUser().getId();
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

        hashtagEntities = new String[tweet.getHashtagEntities().length];
        for (HashtagEntity he : tweet.getHashtagEntities()){
            this.hashtagEntities[i] = he.getText();
            i++;
        }
        i=0;
        userMentionEntities = new String[tweet.getUserMentionEntities().length];
        for (UserMentionEntity ume : tweet.getUserMentionEntities()){
            this.userMentionEntities[i] = ume.getScreenName();
            i++;
        }
        i=0;
        userIDMentionEntities = new Long[tweet.getUserMentionEntities().length];
        for (UserMentionEntity ume : tweet.getUserMentionEntities()){
            this.userIDMentionEntities[i] = ume.getId();
            i++;
        }
        i=0;
        urlEntities = new String[tweet.getURLEntities().length];
        for(URLEntity ue : tweet.getURLEntities()){
            this.urlEntities[i] = ue.getText();
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

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserScreenName() {
        return userScreenName;
    }

    public void setUserScreenName(String userScreenName) {
        this.userScreenName = userScreenName;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public String[] getHashtagEntities() {
        return hashtagEntities;
    }

    public void setHashtagEntities(String[] hashtagEntities) {
        this.hashtagEntities = hashtagEntities;
    }

    public String[] getUserMentionEntities() {
        return userMentionEntities;
    }

    public void setUserMentionEntities(String[] userMentionEntities) {
        this.userMentionEntities = userMentionEntities;
    }

    public Long[] getUserIDMentionEntities() {
        return userIDMentionEntities;
    }

    public void setUserIDMentionEntities(Long[] userIDMentionEntities) {
        this.userIDMentionEntities = userIDMentionEntities;
    }

    public String[] getUrlEntities() {
        return urlEntities;
    }

    public void setUrlEntities(String[] urlEntities) {
        this.urlEntities = urlEntities;
    }

    public Long getInReplyToStatusID() {
        return inReplyToStatusID;
    }

    public void setInReplyToStatusID(Long inReplyToStatusID) {
        this.inReplyToStatusID = inReplyToStatusID;
    }

    public Long getInReplyToUserID() {
        return inReplyToUserID;
    }

    public void setInReplyToUserID(Long inReplyToUserID) {
        this.inReplyToUserID = inReplyToUserID;
    }

    public Long getQuotedStatusID() {
        return quotedStatusID;
    }

    public void setQuotedStatusID(Long quotedStatusID) {
        this.quotedStatusID = quotedStatusID;
    }

    public Long getRetweetedStatusID() {
        return retweetedStatusID;
    }

    public void setRetweetedStatusID(Long retweetedStatusID) {
        this.retweetedStatusID = retweetedStatusID;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getUserFollowersCount() {
        return userFollowersCount;
    }

    public void setUserFollowersCount(int userFollowersCount) {
        this.userFollowersCount = userFollowersCount;
    }

    public int getUserStatusesCount() {
        return userStatusesCount;
    }

    public void setUserStatusesCount(int userStatusesCount) {
        this.userStatusesCount = userStatusesCount;
    }

    @Override
    public String toString(){
        return "<" + this.id + " " + this.createdAt + "> ["+ this.userScreenName + " - " + this.userID + " - " +
                this.userLocation + "] " + this.text + "(" + this.geoLocation + " - " + this.place + ")";
    }
}
