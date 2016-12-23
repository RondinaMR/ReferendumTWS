package ReferendumTweets;

import twitter4j.HashtagEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * Created by marco on 07/12/2016.
 */
public class Entity {
    private String text;
    private int start;
    private int end;

    public Entity() {
    }

    public Entity(String text, int start, int end){
        this.text = text;
        this.start = start;
        this.end =end;
    }

    public Entity(HashtagEntity he){
        this.text = he.getText();
        this.start = he.getStart();
        this.end = he.getEnd();
    }
    public Entity(UserMentionEntity he){
        this.text = he.getText();
        this.start = he.getStart();
        this.end = he.getEnd();
    }
    public Entity(URLEntity he){
        this.text = he.getText();
        this.start = he.getStart();
        this.end = he.getEnd();
    }

    public String getText(){
        return text;
    }

    public int getStart(){
        return start;
    }

    public int getEnd(){
        return end;
    }
}
