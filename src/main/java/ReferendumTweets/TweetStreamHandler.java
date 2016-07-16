package ReferendumTweets;
import com.fasterxml.jackson.core.JsonProcessingException;
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
        private LinkedList<Statistic> statistics = new LinkedList<>();
        private HashMap<Long,TWUS> users = new HashMap<>();
        private TwitterStream twitterStream;
        private StatusListener listener;
        private String[] queries;
        private Status firstStatus;
        private Calendar clast = Calendar.getInstance();

    public TweetStreamHandler() throws FileNotFoundException{
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("Fve1cVc28evpjPFyR1KmXtjVj")
                .setOAuthConsumerSecret("rxOinDLXPQFyOR4FwYCMTRo4KfKkm5jhEVHHgBsUxRPFceRdud")
                .setOAuthAccessToken("96599210-qqvLv1EvrnIF5Ds29GqLoaLWh4i8EaATmYLq8vbEQ")
                .setOAuthAccessTokenSecret("Yud7YJCxXvj5ZEO7Ex8gplsLavgEkbift5ZUbi8eesP25")
                .setJSONStoreEnabled(true);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        listener = new StatusListener() {
            @Override
            public void onStatus(Status status) {
                Long yes = (long) 0;
                Long no = (long) 0;
                Long other = (long) 0;
                int thisPosition;
                thisPosition = scanTweet(status);
                if(thisPosition>0){
                    yes++;
                }else if(thisPosition<0){
                    no++;
                }else{
                    other++;
                }
                addUser(status);
                saveStatusToJSON(status);
                System.out.println("THIS: " + status.getCreatedAt() + " / LOAD AT: " + clast.getTime());
                if(status.getCreatedAt().compareTo(clast.getTime())>=0){
                    statistics.add(new Statistic(clast.getTime(),yes,no,other,getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));
                    yes = (long)0;
                    no = (long)0;
                    other = (long)0;
                    clast.add(Calendar.HOUR_OF_DAY,1);
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

    private void addUser(Status tweet){
//        TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getScreenName(),tweet.getUser().getName(),tweet.getUser().getLocation());
        TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getLocation());
        if(!users.containsKey(tweet.getUser().getId())) users.put(tweet.getUser().getId(), t);
        TWUS user = users.get(tweet.getUser().getId());

        String text = tweet.getText().toLowerCase();
        if(NoCondition(text)){
            user.setPoliticalPosition(false);
            if((user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
        }else if(YesCondition(text)){
            user.setPoliticalPosition(true);
            if((!user.isPoliticalPosition()) && (!user.isAmbiguous())) user.setAmbiguous();
        }
    }

    private boolean NoCondition(String text){
        text.toLowerCase();
        if(text.contains("#iovotono") || (text.contains(("#referendumcostituzionale")) && text.contains("#votono"))){
            return true;
        }else{
            return false;
        }
    }

    private boolean YesCondition(String text){
        text.toLowerCase();
        if(text.contains("#bastaunsi") || text.contains("#iovotosi") || text.contains(("#italiachedicesi")) || (text.contains(("#referendumcostituzionale")) && text.contains("#votosi"))){
            return true;
        }else{
            return false;
        }
    }

    public void loadJSON(){
        statistics.clear();
        users.clear();
        System.out.println("Starting loading data...");
        boolean first = true;
        Calendar c1 = Calendar.getInstance();
        Long yes = (long) 0;
        Long no = (long) 0;
        Long other = (long) 0;
        Integer thisPosition;
        try {
            int n=0;
            File[] files = new File("statuses").listFiles((dir,name) -> name.endsWith(".json"));
            for (File file : files) {
                String rawJSON = readFirstLine(file);
                Status tweet = TwitterObjectFactory.createStatus(rawJSON);

                if(first){
                    firstStatus = tweet;
                    c1.setTime(firstStatus.getCreatedAt());
                    c1.set(Calendar.MINUTE,0);
                    c1.set(Calendar.SECOND,0);
                    c1.set(Calendar.MILLISECOND,0);
                    c1.add(Calendar.HOUR_OF_DAY,1);
                    first = false;
                }

                thisPosition = scanTweet(tweet);
                if(thisPosition>0){
                    yes++;
                }else if(thisPosition<0){
                    no++;
                }else{
                    other++;
                }

                addUser(tweet);

                if(tweet.getCreatedAt().compareTo(c1.getTime()) > 0){
                    statistics.add(new Statistic(c1.getTime(),yes,no,other,getNumberOfYesUsers(),getNumberOfNoUsers(),getNumberOfOtherUsers()));
//                    System.out.println("Tweets: " + getNumberOfTweets());
                    yes = (long)0;
                    no = (long)0;
                    other = (long)0;
                    c1.add(Calendar.HOUR_OF_DAY,1);
                    clast.setTime(c1.getTime());
                }

                n++;
                if(n%1000==0){
                    System.out.println(n);
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
            statistics = mapper.readValue(new File("exports/statistics.json"),  new TypeReference<LinkedList<Statistic>>(){});
            users = mapper.readValue(new File("exports/users.json"),  new TypeReference<Map<Long,TWUS>>(){});

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
        return statistics.stream()
                .collect(summingLong(Statistic::numberOfTweets));
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

    public void toJSONVotingTrend(){
        try {
            Long yn = (long) 0;
            Long nn = (long) 0;
            Double yp = 0.0;
            Statistic last;
            Iterator<Statistic> itr = statistics.iterator();

            System.out.println("Exporting votingTrend.json...");

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
            String filename = "exports/" + "votingTrend.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
            System.out.println("Successfully exported votingTrend.json!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toJSONpopularitySum(){
        try {
            Statistic last;
            Iterator<Statistic> itr = statistics.iterator();
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
            Statistic last,before = new Statistic();
            Iterator<Statistic> itr = statistics.iterator();
            StringBuilder jsondata = new StringBuilder("").append("[");

            LinkedList<PopVoto> pl = new LinkedList<>();
            List<PopVoto> spl;
            List<PopVoto> gpl = new LinkedList<>();;

            while(itr.hasNext()){
                last = itr.next();
                if(first){
                    before = last;
                    first = false;
                }

                difYes = last.getYesUsers() - before.getYesUsers();
                difNo = last.getNoUsers() - before.getNoUsers();
                difOther = last.getOtherUsers() - before.getOtherUsers();

                if(difYes<0){
                    difYes = before.getYesUsers();
                }else if(difNo<0){
                    difNo = before.getNoUsers();
                }else if(difOther<0){
                    difOther = before.getOtherUsers();
                }

                pl.add(new PopVoto(last.numberOfTweets(),numToPerc(difYes,difNo,difOther,1),numToPerc(difYes,difNo,difOther,-1),numToPerc(difYes,difNo,difOther,0)));
                before = last;
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

    public void exportAllJSON(){
        this.toJSONVotingIntentions();
        this.toJSONVotingTrend();
        this.toJSONpopularitySum();
        this.toJSONpopVoting();
    }
}
