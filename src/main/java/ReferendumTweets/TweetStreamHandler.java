package ReferendumTweets;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.*;
import java.net.UnknownHostException;
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
        private Calendar clastH = Calendar.getInstance();
        private Calendar clastD = Calendar.getInstance();
        private Calendar clastW = Calendar.getInstance();

        private TmpStats numHourTweets = new TmpStats();
        private TmpStats numHourUsers = new TmpStats();
        private TmpStats numDayUsers = new TmpStats();
        private TmpStats numWeekUsers = new TmpStats();

        private DB db = initLocalhostDB();

//        private Long[] numHT = {(long) 0, (long) 0, (long) 0};//Number of Hour's Tweets (tmp)
//        private Long[] numHU = {(long) 0, (long) 0, (long) 0};//Number of Hour's Users (tmp)
//        private Long[] numDU = {(long) 0, (long) 0, (long) 0};//Number of Day's Users (tmp)
//        private Long[] numWU = {(long) 0, (long) 0, (long) 0};//Number of Week's Users (tmp)


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
                Calendar now = Calendar.getInstance();
                if(now.getTimeInMillis() - clastH.getTimeInMillis() > HOUR_MILLIS){
                    clastH = Calendar.getInstance();

                    clastH.set(Calendar.MINUTE,0);
                    clastH.set(Calendar.SECOND,0);
                    clastH.set(Calendar.MILLISECOND,0);
                    clastH.add(Calendar.HOUR_OF_DAY,1);
                }

                if(now.getTimeInMillis() - clastD.getTimeInMillis() > DAY_MILLIS){
                    clastD = Calendar.getInstance();

                    clastD.set(Calendar.HOUR_OF_DAY,0);
                    clastD.set(Calendar.MINUTE,0);
                    clastD.set(Calendar.SECOND,0);
                    clastD.set(Calendar.MILLISECOND,0);
                    clastD.add(Calendar.DAY_OF_YEAR,1);
                }

                if(now.getTimeInMillis() - clastW.getTimeInMillis() > WEEK_MILLIS){
                    clastW = Calendar.getInstance();

                    clastW.set(Calendar.DAY_OF_WEEK,0);
                    clastW.set(Calendar.HOUR_OF_DAY,0);
                    clastW.set(Calendar.MINUTE,0);
                    clastW.set(Calendar.SECOND,0);
                    clastW.set(Calendar.MILLISECOND,0);
                    clastW.add(Calendar.WEEK_OF_YEAR,1);
                }

                thisPosition = scanTweet(new TWST(status));

                positionCounterUpdate(numHourTweets,thisPosition);

//                if(thisPosition>0){
//                    numHourTweets.addYes();
////                    numHT[0]++;
//                }else if(thisPosition<0){
//                    numHourTweets.addNo();
////                    numHT[1]++;
//                }else{
//                    numHourTweets.addOther();
////                    numHT[2]++;
//                }

                if(users.containsKey(status.getUser().getId())){
                    if(!((clastH.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < HOUR_MILLIS)){
                        //L'utente non ha twittato nell'ultima ora
                        positionCounterUpdate(numHourUsers,thisPosition);
//                        if(thisPosition>0){
//                            numHourUsers.addYes();
////                            numHU[0]++;
//                        }else if(thisPosition<0){
//                            numHourUsers.addNo();
////                            numHU[1]++;
//                        }else{
//                            numHourUsers.addOther();
////                            numHU[2]++;
//                        }
                        if(!((clastD.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < DAY_MILLIS)){
                            //L'utente non ha twittato nell'ultimo giorno
                            positionCounterUpdate(numDayUsers,thisPosition);
//                            if(thisPosition>0){
//                                numDayUsers.addYes();
////                                numDU[0]++;
//                            }else if(thisPosition<0){
//                                numDayUsers.addNo();
////                                numDU[1]++;
//                            }else{
//                                numDayUsers.addOther();
////                                numDU[2]++;
//                            }
                            if(!((clastW.getTime().getTime() - users.get(status.getUser().getId()).getLastTimeTweet().getTime()) < WEEK_MILLIS)){
                                //L'utente non ha twittato nell'ultima settimana
                                positionCounterUpdate(numWeekUsers,thisPosition);
//                                if(thisPosition>0){
//                                    numWeekUsers.addYes();
////                                    numWU[0]++;
//                                }else if(thisPosition<0){
//                                    numWeekUsers.addNo();
////                                    numWU[1]++;
//                                }else{
//                                    numWeekUsers.addOther();
////                                    numWU[2]++;
//                                }
                            }
                        }
                    }
                }else{
                    if(thisPosition>0){
                        numHourUsers.addYes();
                        numDayUsers.addYes();
                        numWeekUsers.addYes();
//                        numHU[0]++;
//                        numDU[0]++;
//                        numWU[0]++;
                    }else if(thisPosition<0){
                        numHourUsers.addNo();
                        numDayUsers.addNo();
                        numWeekUsers.addNo();
//                        numHU[1]++;
//                        numDU[1]++;
//                        numWU[1]++;
                    }else{
                        numHourUsers.addOther();
                        numDayUsers.addOther();
                        numWeekUsers.addOther();
//                        numHU[2]++;
//                        numDU[2]++;
//                        numWU[2]++;
                    }
                }

                addUser(status);

//                saveStatusToJSON(status);
                loadTweetOnMongoDB(new TWST(status),db);

                System.out.println("THIS: " + status.getCreatedAt() + " / LOAD AT: " + clastH.getTime());

                //HOUR
                if(status.getCreatedAt().compareTo(clastH.getTime()) > 0){
                    statistics.add(new UsersStats(clastH.getTime(),getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));
                    statisticsHour.add(new UsersStats(clastH.getTime(),numHourUsers.getYes(),numHourUsers.getNo(),numHourUsers.getOther()));
                    hourTweets.add(new TweetsStats(clastH.getTime(),numHourTweets.getYes(),numHourTweets.getNo(),numHourTweets.getOther()));

//                    statisticsHour.add(new UsersStats(clastH.getTime(),numHU[0],numHU[1],numHU[2]));
//                    hourTweets.add(new TweetsStats(clastH.getTime(),numHT[0],numHT[1],numHT[2]));
                    numHourTweets.clearAll();
                    numHourUsers.clearAll();
//                    numHT[0] = numHT[1] = numHT[2] = (long)0;
//                    numHU[0] = numHU[1] = numHU[2] = (long)0;
                    clastH.add(Calendar.HOUR_OF_DAY,1);
                    if(!post_block){
                        post("hour");
                        //post("hourtweets");
                        //post("hourusers");
                    }
                }
                //DAY
                if(status.getCreatedAt().compareTo(clastD.getTime()) > 0){
                    statisticsDay.add(new UsersStats(clastD.getTime(),numDayUsers.getYes(),numDayUsers.getNo(),numDayUsers.getOther()));
                    numDayUsers.clearAll();
//                    numDU[0] = numDU[1] = numDU[2] = (long) 0;
                    clastD.add(Calendar.DAY_OF_YEAR,1);
                    if(!post_block) {
                        post("dayusers");
                    }
                }
                //WEEK
                if(status.getCreatedAt().compareTo(clastW.getTime()) > 0){
                    statisticsWeek.add(new UsersStats(clastW.getTime(),numWeekUsers.getYes(),numWeekUsers.getNo(),numWeekUsers.getOther()));
//                    statisticsWeek.add(new UsersStats(clastW.getTime(),numWU[0],numWU[1],numWU[2]));
                    numWeekUsers.clearAll();
//                    numWU[0] = numWU[1] = numWU[2] = (long) 0;
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
            user.subBalance();
            if((user.getBalance()>-2) && (user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
            if((user.getBalance()<-1) && (!user.isPoliticalPosition()) && (user.isAmbiguous())) user.notAmbiguous();
        }else if(YesCondition(text)){
            user.setPoliticalPosition(true);
            user.addBalance();
            if((user.getBalance()<2) && (!user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
            if((user.getBalance()>1) && (user.isPoliticalPosition()) && (user.isAmbiguous())) user.notAmbiguous();
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

    private void saveHashtags(TWST tweet, int position){
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

    private void saveMentions(TWST tweet, int position){
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

    private void loadTweetOnMongoDB(TWST tweet, DB db){
        //Recupero della collezione
        DBCollection collectionTweets = db.getCollection("tweets");
        JacksonDBCollection<TWST,Long> coll = JacksonDBCollection.wrap(collectionTweets,TWST.class,Long.class);
        //Inserimento del documento
        WriteResult<TWST, Long> result = coll.insert(tweet);

//        ObjectMapper mapper = new ObjectMapper();
//        String jsondata = null;
//        try {
//            jsondata = mapper.writeValueAsString(new TWST(tweet));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        // inserimento del documento
//        DBObject jsonDocument = (DBObject) JSON.parse(jsondata);
//        db.getCollection("tweets").insert(jsonDocument);
    }

    private void clearAllCollections(){
        hourTweets.clear();
        statistics.clear();
        statisticsHour.clear();
        statisticsDay.clear();
        statisticsWeek.clear();
        users.clear();
    }

    private DB initLocalhostDB(){
        //Inizializzazione collegamento database MongoDB
        MongoClient client = null;
        try {
            client = new MongoClient("localhost", 27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        DB db = client.getDB("ReferendumDB");
        return db;
    }

    private void zeroCounters(int mode){
        switch (mode){
            //Azzera tutto
            case 0:
                numHourTweets.clearAll();
                numHourUsers.clearAll();
                numDayUsers.clearAll();
                numWeekUsers.clearAll();
//                numHT[0] = numHT[1] = numHT[2] = (long)0;
//                numHU[0] = numHU[1] = numHU[2] = (long)0;
//                numDU[0] = numDU[1] = numDU[2] = (long)0;
//                numWU[0] = numWU[1] = numWU[2] = (long)0;
                break;
            //Azzera Tweet orari
            case 1:
                numHourTweets.clearAll();
//                numHT[0] = numHT[1] = numHT[2] = (long)0;
                break;
            //Azzera Utenti orari
            case 2:
                numHourTweets.clearAll();
//                numHU[0] = numHU[1] = numHU[2] = (long)0;
                break;
            //Azzera Utenti giornalieri
            case 3:
                numDayUsers.clearAll();
//                numDU[0] = numDU[1] = numDU[2] = (long)0;
                break;
            //Azzera Utenti settimanali
            case 4:
                numWeekUsers.clearAll();
//                numWU[0] = numWU[1] = numWU[2] = (long)0;
                break;
        }
    }

    private void positionCounterUpdate(TmpStats counter,int position){
        if(position>0){
            counter.addYes();
        }else if(position<0){
            counter.addNo();
        }else{
            counter.addOther();
        }
    }

    public void loadAllFromDB(){
//        db.getCollection("tweets").find();
//        ObjectMapper mapper = new ObjectMapper();
        DBCollection collectionTweets = db.getCollection("tweets");
        JacksonDBCollection<TWST,Long> coll = JacksonDBCollection.wrap(collectionTweets,TWST.class,Long.class);
        DBObject statusJson;
        TWST status;
//        System.out.println("Collection tweets selected successfully");
//        List<BlogPost> posts = coll.find().sort(DBSort.desc("date")).toArray();
        DBCursor<TWST> cursor = coll.find();
        int i = 0;
        boolean first = true;
        while (cursor.hasNext()) {
//            statusJson = cursor.next();
            status = cursor.next();

            loadSingleStatus(status,first,false);
            first=false;
            i++;

            //JSON from file to Object
//            try {
//                status = mapper.readValue(statusJson.toString(),  new TypeReference<TWST>(){});
//                System.out.println(status);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
    }

    private void loadSingleStatus(TWST tweet,boolean first,boolean loadDB){
        long n=0;
        TWUS t;
        TWST firstStatus;
        //Fisso una data di partenza per la lettura dei file (necessaria per la sincronizzazione DB locale/server
        Calendar dateStart = Calendar.getInstance();
        dateStart.set(2016,10,16,0,0);
        System.out.println("Starting loading data...");

        Calendar ch = Calendar.getInstance();//Calendario limite orario
        Calendar cw = Calendar.getInstance();//Calendario limite settimanale
        Calendar cd = Calendar.getInstance();//Calendario limite giornaliero
        zeroCounters(0);

        Integer thisPosition;


        if(tweet.getCreatedAt().compareTo(dateStart.getTime())<0){
            return;
        }
//                System.out.println(tweet.toString());

        if(first){
            firstStatus = tweet;

            System.out.println("CLASTW: " + clastW.getTime().toString() + " / " + "FIRST: " + firstStatus.getCreatedAt().toString());
            //Se sono state caricate le statistiche, sistemo il tempo, altrimenti no
            if(clastW.getTimeInMillis()<firstStatus.getCreatedAt().getTime())
            {
                System.out.println("Data correction for Statistics");
                if(firstStatus.getCreatedAt().getTime() - clastW.getTimeInMillis() < HOUR_MILLIS){
                    ch.setTime(clastW.getTime());
                }else{
                    ch.setTime(firstStatus.getCreatedAt());
                }

                if(firstStatus.getCreatedAt().getTime() - clastW.getTimeInMillis() < DAY_MILLIS){
                    cd.setTime(clastW.getTime());
                }else{
                    cd.setTime(firstStatus.getCreatedAt());
                }

                if(firstStatus.getCreatedAt().getTime() - clastW.getTimeInMillis() < WEEK_MILLIS){
                    cw.setTime(clastW.getTime());
                }else{
                    cw.setTime(firstStatus.getCreatedAt());
                }
            }else{
                ch.setTime(firstStatus.getCreatedAt());
                cd.setTime(firstStatus.getCreatedAt());
                cw.setTime(firstStatus.getCreatedAt());
            }

            //Aggiusto l'orario del limite per il salvataggio dei nuovi tweet
            System.out.println("[CH] " + ch.getTime());
            ch.set(Calendar.MINUTE,0);
            ch.set(Calendar.SECOND,0);
            ch.set(Calendar.MILLISECOND,0);
            ch.add(Calendar.HOUR_OF_DAY,1);
            System.out.println("[CH] " + ch.getTime());
            System.out.println("[CD] " + cd.getTime());
            cd.set(Calendar.HOUR_OF_DAY,0);
            cd.set(Calendar.MINUTE,0);
            cd.set(Calendar.SECOND,0);
            cd.set(Calendar.MILLISECOND,0);
            cd.add(Calendar.DAY_OF_YEAR,1);
            System.out.println("[CD] " + cd.getTime());
            System.out.println("[CW] " + cw.getTime());
            cw.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
            cw.set(Calendar.HOUR_OF_DAY,0);
            cw.set(Calendar.MINUTE,0);
            cw.set(Calendar.SECOND,0);
            cw.set(Calendar.MILLISECOND,0);
            cw.add(Calendar.DAY_OF_YEAR,1);
//                    cw.add(Calendar.DAY_OF_YEAR,1);
//                    cw.add(Calendar.WEEK_OF_YEAR,1);
            System.out.println("[CW] " + cw.getTime());
            System.out.println("CH: " + ch.getTime().toString() + " / " + "CD: " + cd.getTime().toString() + " / " + "CW: " + cw.getTime().toString());
        }

        if(loadDB){
            loadTweetOnMongoDB(tweet,db);
        }

        thisPosition = scanTweet(tweet);

        positionCounterUpdate(numHourTweets,thisPosition);
//
//        if(thisPosition>0){
//            numHourTweets.addYes();
////                    numHT[0]++;
//        }else if(thisPosition<0){
//            numHourTweets.addNo();
////                    numHT[1]++;
//        }else{
//            numHourTweets.addOther();
////                    numHT[2]++;
//        }

        saveHashtags(tweet,thisPosition);
        saveMentions(tweet,thisPosition);

        if(users.containsKey(tweet.getUser().getId())){
            if(!((ch.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < HOUR_MILLIS)){
                //L'utente non ha twittato nell'ultima ora
                positionCounterUpdate(numHourUsers,thisPosition);
//                if(thisPosition>0){
//                    numHourUsers.addYes();
////                            numHU[0]++;
//                }else if(thisPosition<0){
//                    numHourUsers.addNo();
////                            numHU[1]++;
//                }else{
//                    numHourUsers.addOther();
////                            numHU[2]++;
//                }
                if(!((cd.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < DAY_MILLIS)){
                    //L'utente non ha twittato nell'ultimo giorno
                    positionCounterUpdate(numDayUsers,thisPosition);
//                    if(thisPosition>0){
//                        numDayUsers.addYes();
////                                numDU[0]++;
//                    }else if(thisPosition<0){
//                        numDayUsers.addNo();
////                                numDU[1]++;
//                    }else{
//                        numDayUsers.addOther();
////                                numDU[2]++;
//                    }
                    if(!((cw.getTime().getTime() - users.get(tweet.getUser().getId()).getLastTimeTweet().getTime()) < WEEK_MILLIS)){
                        //L'utente non ha twittato nell'ultima settimana
                        positionCounterUpdate(numWeekUsers,thisPosition);
//                        if(thisPosition>0){
//                            numWeekUsers.addYes();
////                                    numWU[0]++;
//                        }else if(thisPosition<0){
//                            numWeekUsers.addNo();
////                                    numWU[1]++;
//                        }else{
//                            numWeekUsers.addOther();
////                                    numWU[2]++;
//                        }
                    }
                }
            }
        }else{
            if(thisPosition>0){
                numHourTweets.addYes();
                numDayUsers.addYes();
                numWeekUsers.addYes();
//                        numHU[0]++;
//                        numDU[0]++;
//                        numWU[0]++;
            }else if(thisPosition<0){
                numHourTweets.addNo();
                numDayUsers.addNo();
                numWeekUsers.addNo();
//                        numHU[1]++;
//                        numDU[1]++;
//                        numWU[1]++;
            }else{
                numHourTweets.addOther();
                numDayUsers.addOther();
                numWeekUsers.addOther();
//                        numHU[2]++;
//                        numDU[2]++;
//                        numWU[2]++;
            }
        }

        addUser(tweet);

//                System.out.println("[numWU] - [0]: "+numWU[0]+" [1]: "+numWU[1]+" [2]: "+numWU[2]);
        //HOUR
        if(tweet.getCreatedAt().compareTo(ch.getTime()) > 0){
            statistics.add(new UsersStats(ch.getTime(),getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));

            statisticsHour.add(new UsersStats(ch.getTime(),numHourUsers.getYes(),numHourUsers.getNo(),numHourUsers.getOther()));
            hourTweets.add(new TweetsStats(ch.getTime(),numHourTweets.getYes(),numHourTweets.getNo(),numHourTweets.getOther()));

//                    statisticsHour.add(new UsersStats(ch.getTime(),numHU[0],numHU[1],numHU[2]));
//                    hourTweets.add(new TweetsStats(ch.getTime(),numHT[0],numHT[1],numHT[2]));
            numHourTweets.clearAll();
            numHourUsers.clearAll();
//                    numHT[0] = numHT[1] = numHT[2] = (long)0;
//                    numHU[0] = numHU[1] = numHU[2] = (long)0;
            ch.add(Calendar.HOUR_OF_DAY,1);
            clastH.setTime(ch.getTime());
        }
        //DAY
        if(tweet.getCreatedAt().compareTo(cd.getTime()) > 0){
            statisticsDay.add(new UsersStats(cd.getTime(),numDayUsers.getYes(),numDayUsers.getNo(),numDayUsers.getOther()));
            numDayUsers.clearAll();
//                    numDU[0] = numDU[1] = numDU[2] = (long) 0;
            cd.add(Calendar.DAY_OF_YEAR,1);
            clastD.setTime(cd.getTime());
        }
        //WEEK
        if(tweet.getCreatedAt().compareTo(cw.getTime()) > 0){
            System.out.println("[CREATEDAT] " + tweet.getCreatedAt());
            System.out.println("[*][numWU] - [0]: "+numWeekUsers.getYes()+" [1]: "+numWeekUsers.getNo()+" [2]: "+numWeekUsers.getOther());
            statisticsWeek.add(new UsersStats(cw.getTime(),numWeekUsers.getYes(),numWeekUsers.getNo(),numWeekUsers.getOther()));
            numWeekUsers.clearAll();
//                    numWU[0] = numWU[1] = numWU[2] = (long) 0;
            System.out.println("[**][numWU] - [0]: "+numWeekUsers.getYes()+" [1]: "+numWeekUsers.getNo()+" [2]: "+numWeekUsers.getOther());
            System.out.println("[CW] " + cw.getTime());
            cw.add(Calendar.WEEK_OF_YEAR,1);
            System.out.println("[CW] " + cw.getTime());
            System.out.println("[CLASTW] " + clastW.getTime());
            clastW.setTime(cw.getTime());
            System.out.println("[CLASTW] " + clastW.getTime());
        }

        n++;
        if(n%1000==0){
            System.out.println(n + " : " + tweet.getCreatedAt() + " : " + cw.getTime());
        }
    }

    public void loadJSON(boolean loadDB){
        clearAllCollections();
        boolean first = true;

        try {

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
                if(rawJSON==null){
                    continue;
                }
                Status tweet = TwitterObjectFactory.createStatus(rawJSON);

                loadSingleStatus(tweet,first,loadDB);
                first = false;

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
            String jsondata;
            String filename;

            ObjectMapper mapper = new ObjectMapper();

            new File("exports").mkdir();

            //statistics to JSON in String
            jsondata = mapper.writeValueAsString(hourTweets);
            filename = "exports/" + "hourTweets.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(statistics);
            filename = "exports/" + "statistics.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(statisticsHour);
            filename = "exports/" + "statisticsHour.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(statisticsDay);
            filename = "exports/" + "statisticsDay.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(statisticsWeek);
            filename = "exports/" + "statisticsWeek.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(hashtags);
            filename = "exports/" + "hashtags.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(mentions);
            filename = "exports/" + "mentions.json";
            storeJSON(jsondata,filename);
            jsondata = mapper.writeValueAsString(users);
            filename = "exports/" + "users.json";
            storeJSON(jsondata,filename);

            System.out.println("Successfully exported data in *.json!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadStatistics(){
        try {
            hourTweets.clear();
            statistics.clear();
            statisticsHour.clear();
            statisticsDay.clear();
            statisticsWeek.clear();
            hashtags.clear();
            mentions.clear();
            users.clear();

            ObjectMapper mapper = new ObjectMapper();

            //JSON from file to Object
            hourTweets = mapper.readValue(new File("exports/hourTweets.json"),  new TypeReference<LinkedList<TweetsStats>>(){});
            statistics = mapper.readValue(new File("exports/statistics.json"),  new TypeReference<LinkedList<UsersStats>>(){});
            statisticsHour = mapper.readValue(new File("exports/statisticsHour.json"),  new TypeReference<LinkedList<UsersStats>>(){});
            statisticsDay = mapper.readValue(new File("exports/statisticsDay.json"),  new TypeReference<LinkedList<UsersStats>>(){});
            statisticsWeek = mapper.readValue(new File("exports/statisticsWeek.json"),  new TypeReference<LinkedList<UsersStats>>(){});
            hashtags = mapper.readValue(new File("exports/hashtags.json"),  new TypeReference<HashMap<String,EntityStats>>(){});
            mentions = mapper.readValue(new File("exports/mentions.json"),  new TypeReference<HashMap<String,EntityStats>>(){});
            users = mapper.readValue(new File("exports/users.json"),  new TypeReference<HashMap<Long,TWUS>>(){});

            //HOUR
            clastH.setTime(statistics.getLast().getDate());
            //DAY
            clastD.setTime(statistics.getLast().getDate());
            //WEEK
            clastW.setTime(statistics.getLast().getDate());

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
//            new File("statuses").mkdir();
//            String rawJSON = TwitterObjectFactory.getRawJSON(status);
//            String fileName = "statuses/" + status.getId() + ".json";
//            storeJSON(rawJSON, fileName);
//            System.out.println(fileName + " - " + status.getText());
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

    public void toJSONVotingIntentionsWAmbiguos(){
        try {
            Long y = statistics.getLast().getYesUsers();
            Long n = statistics.getLast().getNoUsers();
            Long o = statistics.getLast().getOtherUsers();
            Double yp = numToPerc(y,n,o,1);
            Double np = numToPerc(y,n,o,-1);
            Double op = numToPerc(y,n,o,0);
            Map<Boolean,Double> votingIntentions = new TreeMap<>();
            votingIntentions.put(true,yp);
            votingIntentions.put(false,np);
            String x = new String();

            StringBuilder jsondata = new StringBuilder("");
            jsondata.append("[");
            jsondata.append("{\"Fazione\":\"SI\",\"Percentuale\":").append(String.format(Locale.US,"%1$.4f",yp)).append("}");
            jsondata.append(",");
            jsondata.append("{\"Fazione\":\"NO\",\"Percentuale\":").append(String.format(Locale.US,"%1$.4f",np)).append("}");
            jsondata.append(",");
            jsondata.append("{\"Fazione\":\"ALTRI\",\"Percentuale\":").append(String.format(Locale.US,"%1$.4f",op)).append("}");
            jsondata.append("]");
            String filename = "exports/" + "votingIntentionsWAmbiguos.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported votingIntentionsWAmbiguos.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int scanTweet(TWST tweet){
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
        this.toJSONVotingIntentionsWAmbiguos();
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

    private String postHour(){
        StringBuilder status = new StringBuilder("");
        Double yp,np,op;
        //Percentuale utenti SI
        yp = numToPerc(statistics.getLast().getYesUsers(),statistics.getLast().getNoUsers(),statistics.getLast().getOtherUsers(),1);
        //Percentuale utenti NO
        np = numToPerc(statistics.getLast().getYesUsers(),statistics.getLast().getNoUsers(),statistics.getLast().getOtherUsers(),-1);
        //Percentuale utenti indecisi
        op = numToPerc(statistics.getLast().getYesUsers(),statistics.getLast().getNoUsers(),statistics.getLast().getOtherUsers(),0);



        status.append("Statistiche #ReferendumCostituzionale (ultima ora): ");
        if(statisticsHour.getLast().getNoUsers()>statisticsHour.getLast().getYesUsers()){
            status.append(String.format(Locale.US,"%1$.2f",np)).append("% NO - ").append(String.format(Locale.US,"%1$.2f",yp)).append("% SI - ").append(String.format(Locale.US,"%1$.2f",op)).append("% Altri");
        }else{
            status.append(String.format(Locale.US,"%1$.2f",yp)).append("% SI - ").append(String.format(Locale.US,"%1$.2f",np)).append("% NO - ").append(String.format(Locale.US,"%1$.2f",op)).append("% Altri");
        }
        status.append(" [").append(statisticsHour.getLast().getTotUsers()).append(" utenti] ").append("http://www.suffragium.it/");
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

    private String postMentions(String party){
        Optional<EntityStats> es;
        StringBuilder status = new StringBuilder("");
        status.append("L'utente più menzionato in assoluto dai sostenitori del ").append(party.toUpperCase())
                .append(" è @");
        if(party.equalsIgnoreCase("si")){
            es = mentions.values().stream().sorted(comparing(EntityStats::getYesMentions)).findFirst();
            status.append(es.get().getEntity())
                    .append(" (").append(es.get().getYesMentions()).append(") ");
        }else{
            es = mentions.values().stream().sorted(comparing(EntityStats::getNoMentions)).findFirst();
            status.append(es.get().getEntity())
                    .append(" (").append(es.get().getNoMentions()).append(") ");
        }
        status
                .append(" #ReferendumCostituzionale http://www.suffragium.it/");
        return status.toString();
    }

    private String postHashtags(String party){
        Optional<EntityStats> es;
        StringBuilder status = new StringBuilder("");
        status.append("L'hashtag più utilizzato in assoluto dai sostenitori del ").append(party.toUpperCase())
                .append(" è #");
        if(party.equalsIgnoreCase("si")){
            es = hashtags.values().stream().sorted(comparing(EntityStats::getYesMentions)).findFirst();
            status.append(es.get().getEntity())
                    .append(" (").append(es.get().getYesMentions()).append(") ");
        }else{
            es = hashtags.values().stream().sorted(comparing(EntityStats::getNoMentions)).findFirst();
            status.append(es.get().getEntity())
                    .append(" (").append(es.get().getNoMentions()).append(") ");
        }
        status.append(" #ReferendumCostituzionale http://www.suffragium.it/");
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

        if( name.equalsIgnoreCase("hour")){
            newStatus = postHour();
        }else if(name.equalsIgnoreCase("hourtweets")){
            newStatus = postHourTweets();
        }else if(name.equalsIgnoreCase("hourusers")){
            newStatus = postHourUsers();
        }else if(name.equalsIgnoreCase("dayusers")){
            newStatus = postDayUsers();
        }else if(name.equalsIgnoreCase("absoluteusersperc")){
            newStatus = postAbsoluteUsersP();
        }else if(name.equalsIgnoreCase("globalinfo")){
            newStatus = postGlobalInfo();
        }else if(name.equalsIgnoreCase("postMentionsSI")){
            newStatus = postMentions("si");
        }else if(name.equalsIgnoreCase("postMentionsNO")){
            newStatus = postMentions("no");
        }else if(name.equalsIgnoreCase("postHashtagsSI")){
            newStatus = postHashtags("si");
        }else if(name.equalsIgnoreCase("postHashtagsNO")){
            newStatus = postHashtags("no");
        }
        Status status = null;
        /*
        try {
            status = twitter.updateStatus(newStatus);
        } catch (TwitterException e) {
            e.printStackTrace();
        }*/
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
    }

}
