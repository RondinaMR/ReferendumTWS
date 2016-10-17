package ReferendumTweets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import twitter4j.*;
import twitter4j.conf.*;

import java.io.*;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;



/**
 * Created by Marco on 03/07/2016.
 */
public class TweetStreamHandler {

        final int WEEK_HOURS = 168;
        final long HOUR_MILLIS = 1000*60*60;
        final long DAY_MILLIS = HOUR_MILLIS * 24;
        final long WEEK_MILLIS = DAY_MILLIS * 7;
        public boolean post_block = false;
        //Statistica oraria di tweets
        private LinkedList<TweetsStats> hourTweets = new LinkedList<>();
        //Statistica CUMULATIVA di utenti / h  <--> Storico
        private LinkedList<UsersStats> statistics = new LinkedList<>();
        //Statistica settimanale di utenti / h
        private LinkedList<UsersStats> statisticsWeek = new LinkedList<>();
        //Statistica giornaliera di utenti / h
        private LinkedList<UsersStats> statisticsDay = new LinkedList<>();
        //Statistica oraria di utenti / h
        private LinkedList<UsersStats> statisticsHour = new LinkedList<>();
        private HashMap<String,EntityStats> hashtags = new HashMap<>();
        private HashMap<String,EntityStats> mentions = new HashMap<>();
        private HashMap<Long,TWUS> users = new HashMap<>();
        private TwitterStream twitterStream;
        private StatusListener listener;
        private String[] queries;
        private Status firstStatus;
        private Calendar clastH = Calendar.getInstance();
        private Calendar clastD = Calendar.getInstance();
        private Calendar clastW = Calendar.getInstance();
        private Long[] numHT = {(long) 0, (long) 0, (long) 0};//Number of Hour's Tweets
        private Long[] numHU = {(long) 0, (long) 0, (long) 0};//Number of Hour's Users
        private Long[] numDU = {(long) 0, (long) 0, (long) 0};//Number of Day's Users
        private Long[] numWU = {(long) 0, (long) 0, (long) 0};//Number of Week's Users



    public TweetStreamHandler() throws FileNotFoundException{

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(propertyString("OAuthConsumerKey"))
                .setOAuthConsumerSecret(propertyString("OAuthConsumerSecret"))
                .setOAuthAccessToken(propertyString("OAuthAccessToken"))
                .setOAuthAccessTokenSecret(propertyString("OAuthAccessTokenSecret"))
                .setJSONStoreEnabled(true);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {

                TweetsStats tmpstat;
                TWUS t;
                int thisPosition;

                thisPosition = scanTweet(status);

                if(thisPosition>0){
                    numHT[0]++;
                }else if(thisPosition<0){
                    numHT[1]++;
                }else{
                    numHT[2]++;
                }

                if(users.containsKey(status.getUser().getId())){
                    if(!((clastH.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < HOUR_MILLIS)){
                        //L'utente non ha twittato nell'ultima ora
                        if(thisPosition>0){
                            numHU[0]++;
                        }else if(thisPosition<0){
                            numHU[1]++;
                        }else{
                            numHU[2]++;
                        }
                        if(!((clastD.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < DAY_MILLIS)){
                            //L'utente non ha twittato nell'ultimo giorno
                            if(thisPosition>0){
                                numDU[0]++;
                            }else if(thisPosition<0){
                                numDU[1]++;
                            }else{
                                numDU[2]++;
                            }
                            if(!((clastW.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < WEEK_MILLIS)){
                                //L'utente non ha twittato nell'ultima settimana
                                if(thisPosition>0){
                                    numWU[0]++;
                                }else if(thisPosition<0){
                                    numWU[1]++;
                                }else{
                                    numWU[2]++;
                                }
                            }
                        }
                    }
                }else{
                    if(thisPosition>0){
                        numHU[0]++;
                        numDU[0]++;
                        numWU[0]++;
                    }else if(thisPosition<0){
                        numHU[1]++;
                        numDU[1]++;
                        numWU[1]++;
                    }else{
                        numHU[2]++;
                        numDU[2]++;
                        numWU[2]++;
                    }
                }

                addUser(status);

                saveStatusToJSON(status);

                System.out.println("THIS: " + status.getCreatedAt() + " / LOAD AT: " + clastH.getTime());

                //HOUR
                if(status.getCreatedAt().compareTo(clastH.getTime()) > 0){
                    statistics.add(new UsersStats(clastH.getTime(),getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));
                    statisticsHour.add(new UsersStats(clastH.getTime(),numHU[0],numHU[1],numHU[2]));
                    hourTweets.add(new TweetsStats(clastH.getTime(),numHT[0],numHT[1],numHT[2]));
                    numHT[0] = numHT[1] = numHT[2] = (long)0;
                    numHU[0] = numHU[1] = numHU[2] = (long)0;
                    clastH.add(Calendar.HOUR_OF_DAY,1);
                    if(!post_block){
                        post("hourtweets");
                        post("hourusers");
                    }
                }
                //DAY
                if(status.getCreatedAt().compareTo(clastD.getTime()) > 0){
                    statisticsDay.add(new UsersStats(clastD.getTime(),numDU[0],numDU[1],numDU[2]));
                    numDU[0] = numDU[1] = numDU[2] = (long) 0;
                    clastD.add(Calendar.DAY_OF_YEAR,1);
                    if(!post_block) {
                        post("dayusers");
                    }
                }
                //WEEK
                if(status.getCreatedAt().compareTo(clastW.getTime()) > 0){
                    statisticsWeek.add(new UsersStats(clastW.getTime(),numWU[0],numWU[1],numWU[2]));
                    numWU[0] = numWU[1] = numWU[2] = (long) 0;
                    clastW.add(Calendar.WEEK_OF_YEAR,1);
                }

                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText() + " (" + status.getCreatedAt() + ") <" + status.getPlace() + " / " + status.getUser().getLocation() + ">");
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onStallWarning(StallWarning warning) {
                System.out.println("Got stall warning:" + warning);
            }

            @Override
            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };
    }

    private String propertyString(String name){
        //Funzione che recupera le keys
        PropertiesManager config_properties = new PropertiesManager();
        //config_properties.setPropValues();
        String res = new String("");
        try {
            res=config_properties.getPropValues(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void startStream(){
        String[] strings = queries;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<strings.length;i++){
            sb.append(strings[i]);
            sb.append("_");
        }
        twitterStream.addListener(listener);
        twitterStream.filter(strings);
    }

    public void startStream(String... strings){

        queries = strings;
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i<strings.length;i++){
            sb.append(strings[i]);
            sb.append("_");
        }
        twitterStream.addListener(listener);
        twitterStream.filter(strings);
    }

    public void stopStream(){
        twitterStream.shutdown();
        System.out.println("Stream shutted down");
    }

    private TWUS addUser(Status tweet){
//        TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getScreenName(),tweet.getUser().getName(),tweet.getUser().getLocation());
        TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getLocation(),tweet.getCreatedAt());
        if(!users.containsKey(tweet.getUser().getId())){
            users.put(tweet.getUser().getId(), t);
        }
        TWUS user = users.get(tweet.getUser().getId());

        String text = tweet.getText().toLowerCase();
        if(NoCondition(text)){
            user.setPoliticalPosition(false);
            if((user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
        }else if(YesCondition(text)){
            user.setPoliticalPosition(true);
            if((!user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
        }
        user.setLastTimeTweet(tweet.getCreatedAt());
        return user;
    }

    private boolean NoCondition(String text){
        text.toLowerCase();
        if(text.contains("#iovotono") || text.contains("#iodicono") || (text.contains(("#riformacostituzionale")) && text.contains("#dicono")) || (text.contains(("#riformacostituzionale")) && text.contains("#votono")) || (text.contains(("#referendumcostituzionale")) && text.contains("#dicono")) || (text.contains(("#referendumcostituzionale")) && text.contains("#votono"))){
            return true;
        }else{
            return false;
        }
    }

    private boolean YesCondition(String text){
        text.toLowerCase();
        if(text.contains("#bastaunsi") || text.contains("#iovotosi") || text.contains("#iodicosi") || text.contains(("#italiachedicesi")) || (text.contains(("#riformacostituzionale")) && text.contains("#dicosi")) || (text.contains(("#riformacostituzionale")) && text.contains("#votosi")) || (text.contains(("#referendumcostituzionale")) && text.contains("#dicosi")) || (text.contains(("#referendumcostituzionale")) && text.contains("#votosi"))){
            return true;
        }else{
            return false;
        }
    }

    private void saveHashtags(Status tweet, int position){
        HashtagEntity[] thisHashtags = tweet.getHashtagEntities();
        for(HashtagEntity he : thisHashtags){
            if(he.getText().compareToIgnoreCase("iovotono")!=0 && he.getText().compareToIgnoreCase("bastaunsi")!=0 && he.getText().compareToIgnoreCase("referendumcostituzionale")!=0 && he.getText().compareToIgnoreCase("votono")!=0 && he.getText().compareToIgnoreCase("votosi")!=0 && he.getText().compareToIgnoreCase("italiachedicesi")!=0 && he.getText().compareToIgnoreCase("iovotosi")!=0 && he.getText().compareToIgnoreCase("iodicosi")!=0 && he.getText().compareToIgnoreCase("iodicono")!= 0 && he.getText().compareToIgnoreCase("riformacostituzionale")!=0){
                if(!(hashtags.containsKey(he.getText().toLowerCase()))){
                    hashtags.put(he.getText().toLowerCase(),new EntityStats(he.getText().toLowerCase()));
                }
                if(position>0){
                    hashtags.get(he.getText().toLowerCase()).addYesMention();
                }else if(position<0){
                    hashtags.get(he.getText().toLowerCase()).addNoMention();
                }else{
                    hashtags.get(he.getText().toLowerCase()).addOtherMention();
                }
            }
        }
    }
    private void saveMentions(Status tweet, int position){
        UserMentionEntity[] thisMentions = tweet.getUserMentionEntities();
        for(UserMentionEntity me : thisMentions){
            if(!(mentions.containsKey(me.getText()))){
                mentions.put(me.getText(),new EntityStats(me.getText().toLowerCase()));
            }
            if(position>0){
                mentions.get(me.getText()).addYesMention();
            }else if(position<0){
                mentions.get(me.getText()).addNoMention();
            }else{
                mentions.get(me.getText()).addOtherMention();
            }
        }
    }

    public void loadJSON(){
        hourTweets.clear();
        statistics.clear();
        statisticsHour.clear();
        statisticsDay.clear();
        statisticsWeek.clear();
        users.clear();
        TWUS t;
        System.out.println("Starting loading data...");
        boolean first = true;

        Calendar ch = Calendar.getInstance();//Calendario limite orario
        Calendar cw = Calendar.getInstance();//Calendario limite settimanale
        Calendar cd = Calendar.getInstance();//Calendario limite giornaliero
        numHT[0] = numHT[1] = numHT[2] = (long)0;
        numHU[0] = numHU[1] = numHU[2] = (long)0;
        numDU[0] = numDU[1] = numDU[2] = (long)0;
        numWU[0] = numWU[1] = numWU[2] = (long)0;
        Integer thisPosition;
        try {
            long n=0;
            File[] files = new File("statuses").listFiles((dir,name) -> name.endsWith(".json"));
            /**********************************************/
            class Pair implements Comparable {
                public long t;
                public File f;

                public Pair(File file) {
                    f = file;
                    t = file.lastModified();
                }

                public int compareTo(Object o) {
                    long u = ((Pair) o).t;
                    return t < u ? -1 : t == u ? 0 : 1;
                }
            }

            // Obtain the array of (file, timestamp) pairs.
            Pair[] pairs = new Pair[files.length];
            for (int i = 0; i < files.length; i++)
                pairs[i] = new Pair(files[i]);

            // Sort them by timestamp.
            Arrays.sort(pairs);

            // Take the sorted pairs and extract only the file part, discarding the timestamp.
            for (int i = 0; i < files.length; i++)
                files[i] = pairs[i].f;
            /***********************************************/
            for (File file : files) {
                String rawJSON = readFirstLine(file);
                Status tweet = TwitterObjectFactory.createStatus(rawJSON);

                if(first){
                    firstStatus = tweet;

                    ch.setTime(firstStatus.getCreatedAt());
                    cw.setTime(firstStatus.getCreatedAt());
                    cd.setTime(firstStatus.getCreatedAt());

                    ch.set(Calendar.MINUTE,0);
                    ch.set(Calendar.SECOND,0);
                    ch.set(Calendar.MILLISECOND,0);
                    ch.add(Calendar.HOUR_OF_DAY,1);

                    cd.set(Calendar.HOUR_OF_DAY,0);
                    cd.set(Calendar.MINUTE,0);
                    cd.set(Calendar.SECOND,0);
                    cd.set(Calendar.MILLISECOND,0);
                    cd.add(Calendar.DAY_OF_YEAR,1);

//                    cw.set(Calendar.DAY_OF_WEEK,0);
//                    cw.set(Calendar.HOUR_OF_DAY,0);
//                    cw.set(Calendar.MINUTE,0);
//                    cw.set(Calendar.SECOND,0);
//                    cw.set(Calendar.MILLISECOND,0);
                    cw.add(Calendar.WEEK_OF_YEAR,1);

                    first = false;
                }

                thisPosition = scanTweet(tweet);

                if(thisPosition>0){
                    numHT[0]++;
                }else if(thisPosition<0){
                    numHT[1]++;
                }else{
                    numHT[2]++;
                }

                saveHashtags(tweet,thisPosition);
                saveMentions(tweet,thisPosition);

                if(users.containsKey(tweet.getUser().getId())){
                    if(!((ch.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < HOUR_MILLIS)){
                        //L'utente non ha twittato nell'ultima ora
                        if(thisPosition>0){
                            numHU[0]++;
                        }else if(thisPosition<0){
                            numHU[1]++;
                        }else{
                            numHU[2]++;
                        }
                        if(!((cd.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < DAY_MILLIS)){
                            //L'utente non ha twittato nell'ultimo giorno
                            if(thisPosition>0){
                                numDU[0]++;
                            }else if(thisPosition<0){
                                numDU[1]++;
                            }else{
                                numDU[2]++;
                            }
                            if(!((cw.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < WEEK_MILLIS)){
                                //L'utente non ha twittato nell'ultima settimana
                                if(thisPosition>0){
                                    numWU[0]++;
                                }else if(thisPosition<0){
                                    numWU[1]++;
                                }else{
                                    numWU[2]++;
                                }
                            }
                        }
                    }
                }else{
                    if(thisPosition>0){
                        numHU[0]++;
                        numDU[0]++;
                        numWU[0]++;
                    }else if(thisPosition<0){
                        numHU[1]++;
                        numDU[1]++;
                        numWU[1]++;
                    }else{
                        numHU[2]++;
                        numDU[2]++;
                        numWU[2]++;
                    }
                }

                addUser(tweet);

//                System.out.println("[numWU] - [0]: "+numWU[0]+" [1]: "+numWU[1]+" [2]: "+numWU[2]);
                //HOUR
                if(tweet.getCreatedAt().compareTo(ch.getTime()) > 0){
                    statistics.add(new UsersStats(ch.getTime(),getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));
                    statisticsHour.add(new UsersStats(ch.getTime(),numHU[0],numHU[1],numHU[2]));
                    hourTweets.add(new TweetsStats(ch.getTime(),numHT[0],numHT[1],numHT[2]));
                    numHT[0] = numHT[1] = numHT[2] = (long)0;
                    numHU[0] = numHU[1] = numHU[2] = (long)0;
                    ch.add(Calendar.HOUR_OF_DAY,1);
                    clastH.setTime(ch.getTime());
                }
                //DAY
                if(tweet.getCreatedAt().compareTo(cd.getTime()) > 0){
                    statisticsDay.add(new UsersStats(cd.getTime(),numDU[0],numDU[1],numDU[2]));
                    numDU[0] = numDU[1] = numDU[2] = (long) 0;
                    cd.add(Calendar.DAY_OF_YEAR,1);
                    clastD.setTime(cd.getTime());
                }
                //WEEK
                if(tweet.getCreatedAt().compareTo(cw.getTime()) > 0){
                    System.out.println("[*][numWU] - [0]: "+numWU[0]+" [1]: "+numWU[1]+" [2]: "+numWU[2]);
                    statisticsWeek.add(new UsersStats(cw.getTime(),numWU[0],numWU[1],numWU[2]));
                    numWU[0] = numWU[1] = numWU[2] = (long) 0;
                    System.out.println("[**][numWU] - [0]: "+numWU[0]+" [1]: "+numWU[1]+" [2]: "+numWU[2]);
                    cw.add(Calendar.WEEK_OF_YEAR,1);
                    clastW.setTime(cw.getTime());
                }

                n++;
                if(n%1000==0){
                    System.out.println(n + " : " + tweet.getCreatedAt() + " : " + cw.getTime());
                }

            }
            System.out.println("All data successfully loaded...");
            toJSONstatistics();
            System.out.println("...and exported!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to store tweets: " + ioe.getMessage());
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to deserialize JSON: " + te.getMessage());
            System.exit(-1);
        }

        System.out.println("Loaded "+ getNumberOfTweets() + " tweets by " + users.size() + " users.");
        users.values().stream()
                .filter(u -> u.isPositionSetted())
                .filter(u -> !u.isAmbiguous())
                .collect(groupingBy(TWUS::isPoliticalPosition,counting()))
                .entrySet().stream()
                .map(e -> e.getKey() + " : " + e.getValue())
                .forEach(System.out::println);
    }

    public void toJSONstatistics(){
        try {
            ObjectMapper mapper = new ObjectMapper();
            //statistics to JSON in String
            String jsondata = mapper.writeValueAsString(statistics);
            String filename = "exports/" + "statistics.json";
            new File("exports").mkdir();
            storeJSON(jsondata,filename);
            //users to JSON in String
            jsondata = mapper.writeValueAsString(users);
            filename = "exports/" + "users.json";
            new File("exports").mkdir();
            storeJSON(jsondata,filename);
            System.out.println("Successfully exported statistics and users in *.json!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadStatistics(){
        try {
            statistics.clear();
            users.clear();
            ObjectMapper mapper = new ObjectMapper();

            //JSON from file to Object
            statistics = mapper.readValue(new File("exports/statistics.json"),  new TypeReference<LinkedList<TweetsStats>>(){});
            users = mapper.readValue(new File("exports/users.json"),  new TypeReference<Map<Long,TWUS>>(){});
            clastH.setTime(statistics.getLast().getDate());
            clastH.add(Calendar.HOUR_OF_DAY,1);
            System.out.println("Loaded "+ getNumberOfTweets() + " tweets by " + users.size() + " users.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Long getNumberOfYesUsers(){
        return users.values().stream()
                .filter(u -> u.isPositionSetted())
                .filter(u -> !u.isAmbiguous())
                .filter(u -> u.isPoliticalPosition())
                .collect(counting());
    }

    public Long getNumberOfNoUsers(){
        return users.values().stream()
                .filter(u -> u.isPositionSetted())
                .filter(u -> !u.isAmbiguous())
                .filter(u -> !u.isPoliticalPosition())
                .collect(counting());
    }

    public Long getNumberOfOtherUsers(){
        return users.values().stream()
                .filter(u -> (!u.isPositionSetted() || u.isAmbiguous()))
                .collect(counting());
    }

    public Long getNumberOfUsers(){
        return users.values().stream()
                .collect(counting());
    }

    public Long getNumberOfTweets(){
        return hourTweets.stream()
                .collect(summingLong(TweetsStats::numberOfTweets));
    }

    private static String readFirstLine(File fileName) throws IOException {
        FileInputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis, "UTF-8");
            br = new BufferedReader(isr);
            return br.readLine();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignore) {
                }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (IOException ignore) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    private void saveStatusToJSON(Status status){
        try {
            new File("statuses").mkdir();
            String rawJSON = TwitterObjectFactory.getRawJSON(status);
            String fileName = "statuses/" + status.getId() + ".json";
            storeJSON(rawJSON, fileName);
            System.out.println(fileName + " - " + status.getText());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to store tweets: " + ioe.getMessage());
        }
    }

    private static void storeJSON(String rawJSON, String fileName) throws IOException {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try {
            fos = new FileOutputStream(fileName);
            osw = new OutputStreamWriter(fos, "UTF-8");
            bw = new BufferedWriter(osw);
            bw.write(rawJSON);
            bw.flush();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException ignore) {
                }
            }
            if (osw != null) {
                try {
                    osw.close();
                } catch (IOException ignore) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignore) {
                }
            }
        }
    }

    public void toJSONVotingIntentions(){
        try {
            Long y = statistics.getLast().getYesUsers();
            Long n = statistics.getLast().getNoUsers();
            Double yp = numToPerc(y,n,(long)0,1);
            Double np = 1 - yp;
            Map<Boolean,Double> votingIntentions = new TreeMap<>();
            votingIntentions.put(true,yp);
            votingIntentions.put(false,np);
            String x = new String();

            StringBuilder jsondata = new StringBuilder("");
            jsondata.append("[");
            jsondata.append("{\"Fazione\":\"SI\",\"Percentuale\":").append(String.format(Locale.US,"%1$.4f",yp)).append("}");
            jsondata.append(",");
            jsondata.append("{\"Fazione\":\"NO\",\"Percentuale\":").append(String.format(Locale.US,"%1$.4f",np)).append("}");
            jsondata.append("]");

////            For dot instead of comma:
//            String s = new Formatter(Locale.US).format("%.2f", price);
////            or do this at application startup, e.g. in your main() method
//            Locale.setDefault(Locale.US);

            String filename = "exports/" + "votingIntentions.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported votingIntentions.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int scanTweet(Status tweet){
        String text = tweet.getText().toLowerCase();
        int res = 0;
        if(NoCondition(text)){
            res -= 1;
        }
        if(YesCondition(text)){
            res += 1;
        }
        return res;
    }

    private Double numToPerc(Long yes, Long no, Long other, Integer mode){
        Double tot = (double) (yes+no+other);
        if(tot == 0.0){
            return 0.0;
        }else{
            if(mode > 0){
                return yes/tot;
            }else if(mode < 0){
                return no/tot;
            }else{
                return other/tot;
            }
        }
    }

    public void toJSONVotingTrend(String type){
        try {

            Iterator<UsersStats> itr;

            if(type.compareToIgnoreCase("hour")==0){
                itr = statisticsHour.iterator();
            }else if(type.compareToIgnoreCase("day")==0){
                itr = statisticsDay.iterator();
            }else if(type.compareToIgnoreCase("week")==0){
                itr = statisticsWeek.iterator();
            }else{
                itr = statistics.iterator();
            }

            Double yp = 0.0;

            UsersStats last;

            System.out.println("Exporting voting"+type+"Trend.json...");

            StringBuilder jsondata = new StringBuilder("");
            jsondata.append("[");

            while(itr.hasNext()){
                last = itr.next();
                yp = numToPerc(last.getYesUsers(),last.getNoUsers(),(long)0,1);

                jsondata.append("{\"date\":").append(last.getDate().getTime()).append(",");
                jsondata.append("\"SI\":").append(String.format(Locale.US,"%1$.4f",yp)).append(",");
                jsondata.append("\"NO\":").append(String.format(Locale.US,"%1$.4f",(1-yp))).append("}");
                if(itr.hasNext()){
                    jsondata.append(",");
                }
            }
            jsondata.append("]");
            String filename = "exports/" + "voting"+type+"Trend.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported voting"+type+"Trend.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toJSONpopularitySum(){
        try {
            TweetsStats last;
            Iterator<TweetsStats> itr = hourTweets.iterator();
            StringBuilder jsondata = new StringBuilder("").append("[");

            System.out.println("Exporting popularitySum.json...");

            while(itr.hasNext()){
                last = itr.next();
                jsondata.append("{\"date\":").append(last.getDate().getTime()).append(",");
                jsondata.append("\"SI\":").append(last.getYes()).append(",");
                jsondata.append("\"NO\":").append(last.getNo()).append(",");
                jsondata.append("\"Altro\":").append(last.getOther()).append("}");
                if(itr.hasNext()){
                    jsondata.append(",");
                }
            }
            jsondata.append("]");

            String filename = "exports/" + "popularitySum.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported popularitySum.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean cleanLocation(Status t){
        if((t.getUser().getLocation() != null)&&(!(t.getUser().getLocation().equalsIgnoreCase("italy") || t.getUser().getLocation().equalsIgnoreCase("italia") || t.getUser().getLocation().equalsIgnoreCase("italia") || t.getUser().getLocation().equalsIgnoreCase("Italy-World")))){
            return true;
        }else{
            return false;
        }
    }

//    public void toJSONGeoVote(){
//        try {
//            boolean first = true;
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
//            List<Status> sortedTweets = tweets.stream()
//                    .filter(t -> cleanLocation(t))
//                    .sorted((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
//                    .collect(toList());
//            System.out.println("NGEO: " + sortedTweets.size());
//            StringBuilder jsondata = new StringBuilder("").append("[");
//            for(Status s : sortedTweets){
//                if(!first){
//                    jsondata.append(",");
//                }
//                first = false;
//                jsondata.append("{\"date\":\"").append(sdf.format(s.getCreatedAt())).append("\",");
//                jsondata.append("\"tplace\":\"").append(s.getUser().getLocation()).append("\",");
//                jsondata.append("\"position\":").append(scanTweet(s)).append("}");
//            }
//            jsondata.append("]");
//            String filename = "exports/" + "geoVoting.json";
//            new File("exports").mkdir();
//            storeJSON(jsondata.toString(),filename);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void toJSONpopVoting(){
        try {
            Integer groupDimension = 25;
            Integer size = 0;
            Integer count = 0;
            PopVoto thispv;
            Long difYes;
            Long difNo;
            Long difOther;
            Double y;
            Double n;
            Double o;
            y = n = o = 0.0;
            boolean first = true;
            TweetsStats lastT,beforeT = new TweetsStats();
            UsersStats lastU,beforeU = new UsersStats();
            Iterator<TweetsStats> itrT = hourTweets.iterator();
            Iterator<UsersStats> itrU = statisticsHour.iterator();
            StringBuilder jsondata = new StringBuilder("").append("[");

            LinkedList<PopVoto> pl = new LinkedList<>();
            List<PopVoto> spl;
            List<PopVoto> gpl = new LinkedList<>();

            while(itrT.hasNext() && itrU.hasNext()){
                lastT = itrT.next();
                lastU = itrU.next();
                difYes = lastU.getYesUsers();
                difNo = lastU.getNoUsers();
                difOther = lastU.getOtherUsers();
                pl.add(new PopVoto(lastT.numberOfTweets(),numToPerc(difYes,difNo,difOther,1),numToPerc(difYes,difNo,difOther,-1),numToPerc(difYes,difNo,difOther,0)));
            }
            spl = pl.stream().sorted(comparing(PopVoto::getTot)).collect(toList());
            Iterator<PopVoto> ipv = spl.iterator();

            while(ipv.hasNext()){
                thispv = ipv.next();
                n += thispv.getNo();
                y += thispv.getYes();
                o += thispv.getOther();
                count++;
                if(thispv.getTot()>size){
                    gpl.add(new PopVoto((long)size,y/count,n/count,o/count));
                    count=0;
                    y = n = o = 0.0;
                    size += groupDimension;
                }
            }

            first = true;
            for(PopVoto pop : gpl){
                if(!first){
                    jsondata.append(",");
                }
                jsondata.append("{\"popularity\":").append(pop.getTot()).append(",");
                jsondata.append("\"yes\":").append(String.format(Locale.US,"%1$.4f",pop.getYes())).append(",");
                jsondata.append("\"other\":").append(String.format(Locale.US,"%1$.4f",pop.getOther())).append(",");
                jsondata.append("\"no\":").append(String.format(Locale.US,"%1$.4f",pop.getNo())).append("}");
                first = false;
            }
            jsondata.append("]");

            String filename = "exports/" + "popularityVote.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported popularityVote.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toJSONentity(String type,String mode){
        try {
            EntityStats last;
            Iterator<EntityStats> itr;

            if(type.compareToIgnoreCase("mentions")==0){
                if(mode.compareToIgnoreCase("yes")==0){
                    itr = mentions.values().stream().sorted(comparing(EntityStats::getYesMentions)).iterator();
                }else if(mode.compareToIgnoreCase("no")==0){
                    itr = mentions.values().stream().sorted(comparing(EntityStats::getNoMentions)).iterator();
                }else{
                    itr = mentions.values().stream().sorted(comparing(EntityStats::getTotalMentions)).iterator();
                }
            }else{
                //Hashtags
                if(mode.compareToIgnoreCase("yes")==0){
                    itr = hashtags.values().stream().sorted(comparing(EntityStats::getYesMentions)).iterator();
                }else if(mode.compareToIgnoreCase("no")==0){
                    itr = hashtags.values().stream().sorted(comparing(EntityStats::getNoMentions)).iterator();
                }else{
                    itr = hashtags.values().stream().sorted(comparing(EntityStats::getTotalMentions)).iterator();
                }
            }

            System.out.println("Exporting "+type+mode+".json...");

            StringBuilder jsondata = new StringBuilder("");
            jsondata.append("[");

            while(itr.hasNext()){
                last = itr.next();

                jsondata.append("{\"entity\":\"").append(last.getEntity()).append("\" ,");

                if(mode.compareToIgnoreCase("tot")==0){
                    jsondata.append("\"num\":").append(last.getTotalMentions()).append("}");
                }else if(mode.compareToIgnoreCase("yes")==0){
                    jsondata.append("\"num\":").append(last.getYesMentions()).append("}");
                }else if(mode.compareToIgnoreCase("no")==0){
                    jsondata.append("\"num\":").append(last.getNoMentions()).append("}");
                }

                if(itr.hasNext()){
                    jsondata.append(",");
                }
            }
            jsondata.append("]");
            String filename = "exports/" + type + mode + ".json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported "+type+mode+".json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportAllJSON(){
        this.toJSONVotingIntentions();
        this.toJSONVotingTrend("");
        this.toJSONVotingTrend("Hour");
        this.toJSONVotingTrend("Day");
        this.toJSONVotingTrend("Week");
        this.toJSONpopularitySum();
        this.toJSONpopVoting();
        this.toJSONentity("hashtags","Tot");
        this.toJSONentity("hashtags","Yes");
        this.toJSONentity("hashtags","No");
        this.toJSONentity("mentions","Tot");
        this.toJSONentity("mentions","Yes");
        this.toJSONentity("mentions","No");
        toJSONstatistics();
    }

    public String toStringVotingIntentions(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/votingIntentions.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }
    public String toStringVotingTrend(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/votingTrend.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }

    public String toStringVotingHourTrend(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/votingHourTrend.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }

    public String toStringVotingDayTrend(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/votingDayTrend.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }
    public String toStringVotingWeekTrend(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/votingWeekTrend.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }
    public String toStringPopularitySum(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/popularitySum.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }
    public String toStringPopularityVote(){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/popularityVote.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }
    public String toStringEntity(String type,String mode){
        String rawJSON = "";
        try {
            rawJSON = readFirstLine(new File("exports/"+type+mode+".json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawJSON;
    }

    private String postHourTweets(){
        StringBuilder status = new StringBuilder("");
        status.append("Nell'ultima ora ");
        if(hourTweets.getLast().getNo()>hourTweets.getLast().getYes()){
            status.append(hourTweets.getLast().getNo()).append(" tweets per il NO e ").append(hourTweets.getLast().getYes()).append(" per il SI");
        }else{
            status.append(hourTweets.getLast().getYes()).append(" tweets per il SI e").append(hourTweets.getLast().getNo()).append(" per il NO");
        }

        status.append(" #ReferendumCostituzionale ").append("http://www.suffragium.it/");

        return status.toString();
    }

    private String postAbsoluteUsersP(){
        Double yp = numToPerc(statistics.getLast().getYesUsers(),statistics.getLast().getNoUsers(),(long)0,1);
        Double np = 1 - yp;
        yp = yp*100;
        np = np*100;
        StringBuilder status = new StringBuilder("");
        status.append("Statistiche totali per il #ReferendumCostituzionale (senza indecisi): ");
        if(np>yp){
            status.append("NO ").append(String.format(Locale.US,"%1$.2f",np)).append("%, SI ").append(String.format(Locale.US,"%1$.2f",yp)).append("% ");
        }else{
            status.append("SI ").append(String.format(Locale.US,"%1$.2f",yp)).append("%, NO ").append(String.format(Locale.US,"%1$.2f",np)).append("% ");
        }
        status.append(" (Altro su ").append("http://www.suffragium.it/)");
        System.out.println(status.toString());
        return status.toString();
    }

    private String postHourUsers(){
        StringBuilder status = new StringBuilder("");
        status.append("Nell'ultima ora hanno twittato ").append(statisticsHour.getLast().getTotUsers()).append(" utenti, di cui ");
        if(statisticsHour.getLast().getNoUsers()>statisticsHour.getLast().getYesUsers()){
            status.append(statisticsHour.getLast().getNoUsers()).append(" per il NO e ").append(statisticsHour.getLast().getYesUsers()).append(" per il SI");
        }else{
            status.append(statisticsHour.getLast().getYesUsers()).append(" per il SI e ").append(statisticsHour.getLast().getNoUsers()).append(" per il NO");
        }
        status.append(" #ReferendumCostituzionale ").append("http://www.suffragium.it/");
        return status.toString();
    }

    private String postDayUsers(){
        StringBuilder status = new StringBuilder("");
        status.append("Oggi hanno twittato ").append(statisticsDay.getLast().getTotUsers()).append(" utenti, di cui");
        if(statisticsDay.getLast().getNoUsers()>statisticsDay.getLast().getYesUsers()){
            status.append(statisticsDay.getLast().getNoUsers()).append(" per il NO e ").append(statisticsDay.getLast().getYesUsers()).append(" per il SI");
        }else{
            status.append(statisticsDay.getLast().getYesUsers()).append(" per il SI e ").append(statisticsDay.getLast().getNoUsers()).append(" per il NO");
        }
        status.append(" #ReferendumCostituzionale ").append("http://www.suffragium.it/");
        return status.toString();
    }

    private String postGlobalInfo(){
        StringBuilder status = new StringBuilder("");
        status.append("Sono stati analizzati un totale di ")
                .append(getNumberOfTweets()).append(" tweets e ")
                .append(getNumberOfUsers()).append(" utenti ")
                .append("#ReferendumCostituzionale #4dicembre http://www.suffragium.it/");
        return status.toString();
    }

    public void post(String name){
        String newStatus = null;
        // The factory instance is re-useable and thread safe.

        ConfigurationBuilder cc = new ConfigurationBuilder();
        cc.setDebugEnabled(true)
                .setOAuthConsumerKey(propertyString("OAuthConsumerKey"))
                .setOAuthConsumerSecret(propertyString("OAuthConsumerSecret"))
                .setOAuthAccessToken(propertyString("OAuthAccessToken"))
                .setOAuthAccessTokenSecret(propertyString("OAuthAccessTokenSecret"))
                .setJSONStoreEnabled(true);

        TwitterFactory tf = new TwitterFactory(cc.build());
        Twitter twitter = tf.getInstance();

        //Twitter twitter = TwitterFactory.getSingleton();

        if(name.equalsIgnoreCase("hourtweets")){
            newStatus = postHourTweets();
        }else if(name.equalsIgnoreCase("hourusers")){
            newStatus = postHourUsers();
        }else if(name.equalsIgnoreCase("dayusers")){
            newStatus = postDayUsers();
        }else if(name.equalsIgnoreCase("absoluteusersperc")){
            newStatus = postAbsoluteUsersP();
        }else if(name.equalsIgnoreCase("globalinfo")){
            newStatus = postGlobalInfo();
        }
        Status status = null;
        try {
            status = twitter.updateStatus(newStatus);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
    }

}
