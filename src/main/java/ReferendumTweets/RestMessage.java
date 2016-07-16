package ReferendumTweets;

/**
 * Created by Marco on 16/07/2016.
 */
public class RestMessage {
    private final long id;
    private final long tweets;
    private final long users;
    private final AppController.State content;



    public RestMessage(long id, long tweets, long users, AppController.State content) {
        this.id = id;
        this.tweets = tweets;
        this.users = users;

        this.content = content;
    }

    public long getId() {
        return id;
    }

    public long getTweets() {
        return tweets;
    }

    public long getUsers() {
        return users;
    }

    public AppController.State getContent() {
        return content;
    }
}
