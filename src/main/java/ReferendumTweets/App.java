package ReferendumTweets;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        try{
//            TwitterSearchHandler tseh = new TwitterSearchHandler();
//            tseh.printJSON();
            TweetStreamHandler tsh = new TweetStreamHandler();
            tsh.loadJSON();
            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
            new StreamGUI(tsh);
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
}

