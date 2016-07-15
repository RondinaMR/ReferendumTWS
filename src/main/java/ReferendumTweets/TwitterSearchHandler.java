package ReferendumTweets;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marco on 03/07/2016.
 */
public class TwitterSearchHandler {
    private HashMap<Long,TWUS> users = new HashMap<>();
    private List<Status> tweetsNO;
    private List<Status> tweetsSI;
    private LinkedList<Status> tweets = new LinkedList<>();
    Twitter twitter;

    long totCount = 0;

    public TwitterSearchHandler() throws TwitterException,FileNotFoundException{
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("Fve1cVc28evpjPFyR1KmXtjVj")
                .setOAuthConsumerSecret("rxOinDLXPQFyOR4FwYCMTRo4KfKkm5jhEVHHgBsUxRPFceRdud")
                .setOAuthAccessToken("96599210-qqvLv1EvrnIF5Ds29GqLoaLWh4i8EaATmYLq8vbEQ")
                .setOAuthAccessTokenSecret("Yud7YJCxXvj5ZEO7Ex8gplsLavgEkbift5ZUbi8eesP25")
                .setJSONStoreEnabled(true);
        TwitterFactory tf = new TwitterFactory(cb.build());
        // gets Twitter instance with default credentials
        this.twitter = tf.getInstance();

    }
    public void printJSON(){
        try {
            String id = "751719506029019136";
            Status status = twitter.showStatus(Long.parseLong(id));
            String rawJSON = TwitterObjectFactory.getRawJSON(status);
            System.out.println(rawJSON);
            Status tweet2 = TwitterObjectFactory.createStatus(rawJSON);
            System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText() + " (" + status.getCreatedAt().getTime()+")");
            HashtagEntity[] he = tweet2.getHashtagEntities();
            for(HashtagEntity h : he){
                System.out.println("HE: "+ h.getText());
            }
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public long cercoNO() throws TwitterException{
        long count;
        Query queryNO = new Query("#iovotono");
        QueryResult resultNO;
        do {
            resultNO = twitter.search(queryNO);
            tweetsNO = resultNO.getTweets();
            for (Status tweet : tweetsNO) {
                TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getScreenName(),tweet.getUser().getName(),tweet.getUser().getLocation(),false);
                if(!users.containsKey(tweet.getUser().getId())) users.put(tweet.getUser().getId(), t);
                TWUS user = users.get(tweet.getUser().getId());
                if((user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
                user.addTweetToUser(tweet);
                System.out.println("[NO] @"+tweet.getUser().getScreenName() + " - N:" + tweet.getUser().getName() + " - L: " + tweet.getUser().getLocation() + " - ID: " + tweet.getUser().getId());
            }
        } while ((queryNO = resultNO.nextQuery()) != null);
        count = tweetsNO.size();
        System.out.println("TOTAL NO: "+count);
        return count;
    }

    public long cercoSI() throws TwitterException{
        long count;
        Query querySI = new Query("#bastaunsi");
        QueryResult resultSI;
        do {
            resultSI = twitter.search(querySI);
            tweetsSI = resultSI.getTweets();
            for (Status tweet : tweetsSI) {
                TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getScreenName(),tweet.getUser().getName(),tweet.getUser().getLocation(),false);
                if(!users.containsKey(tweet.getUser().getId())) users.put(tweet.getUser().getId(), t);
                TWUS user = users.get(tweet.getUser().getId());
                if((!user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
                user.addTweetToUser(tweet);
                System.out.println("[SI] @"+tweet.getUser().getScreenName() + " - N:" + tweet.getUser().getName() + " - L: " + tweet.getUser().getLocation() + " - ID: " + tweet.getUser().getId());
            }
        } while ((querySI = resultSI.nextQuery()) != null);
        count = tweetsSI.size();
        System.out.println("TOTAL NO: "+count);
        return count;
    }
    public void saveJson(String filename){
//        Gson gson = new Gson();
//        String userJson = gson.toJson(tweets);
        String userJson = "";
        try{
            PrintStream out = new PrintStream(new FileOutputStream(filename));
            out.print(userJson);
        }catch(Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

}
