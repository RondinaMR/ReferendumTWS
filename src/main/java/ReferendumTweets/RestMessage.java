package ReferendumTweets;

/**
 * Created by Marco on 16/07/2016.
 */
public class RestMessage {
    private final long id;
    private final AppController.State content;

    public RestMessage(long id, AppController.State content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public AppController.State getContent() {
        return content;
    }
}
