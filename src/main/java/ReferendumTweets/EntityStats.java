package ReferendumTweets;

import twitter4j.TweetEntity;

/**
 * Created by marco on 17/09/2016.
 */
public class EntityStats {
    private TweetEntity entity;
    private long yesMentions = (long) 0;
    private long noMentions = (long) 0;
    private long otherMentions = (long) 0;

    public EntityStats(TweetEntity entity) {

        this.entity = entity;
    }

    public TweetEntity getEntity() {
        return entity;
    }

    public long getYesMentions() {
        return yesMentions;
    }

    public long getNoMentions() {
        return noMentions;
    }

    public long getOtherMentions() {
        return otherMentions;
    }

    public void addYesMention(){
        this.yesMentions++;
    }

    public void addNoMention(){
        this.noMentions++;
    }

    public void addOtherMention(){
        this.otherMentions++;
    }

    public long getTotalMentions(){
        return yesMentions+noMentions+otherMentions;
    }

}
