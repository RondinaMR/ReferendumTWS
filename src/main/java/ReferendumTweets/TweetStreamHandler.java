package ReferendumTweets;
import twitter4j.*;
import twitter4j.conf.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;

/**
 * Created by Marco on 03/07/2016.
 */
public class TweetStreamHandler {
        private HashMap<Long,TWUS> users = new HashMap<>();
        private LinkedList<Status> tweets = new LinkedList<>();
        private TwitterStream twitterStream;
        private StatusListener listener;
        private String searchQuery = new String("");
        private String[] queries;

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
                tweets.add(status);
                addUser(status);
                saveStatusToJSON(status);
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
        searchQuery = sb.toString();
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
        searchQuery = sb.toString();
        twitterStream.addListener(listener);
        twitterStream.filter(strings);
    }

    public void stopStream(){
        twitterStream.shutdown();
        System.out.println("Stream shutted down");
    }

    private void addUser(Status tweet){
        TWUS t = new TWUS(tweet.getUser().getId(),tweet.getUser().getScreenName(),tweet.getUser().getName(),tweet.getUser().getLocation());
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
        user.addTweetToUser(tweet);
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
        System.out.println("Starting loading data...");
        try {
            int n=0;
            File[] files = new File("statuses").listFiles((dir,name) -> name.endsWith(".json"));
            for (File file : files) {
                String rawJSON = readFirstLine(file);
                Status tweet = TwitterObjectFactory.createStatus(rawJSON);
                tweets.add(tweet);
                n++;
                if(n%1000==0){
                    System.out.println(n);
                }
//                System.out.println(n++);
//                System.out.println("@"+tweet.getUser().getId() + " - " + tweet.getText() + " (" + tweet.getCreatedAt()+")");
                addUser(tweet);
            }
            System.out.println("All data successfully loaded!");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Failed to store tweets: " + ioe.getMessage());
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to deserialize JSON: " + te.getMessage());
            System.exit(-1);
        }

        System.out.println("Loaded "+ tweets.size() + " tweets by " + users.size() + " users.");
        users.values().stream()
                .filter(u -> u.isPositionSetted())
                .filter(u -> !u.isAmbiguous())
                .collect(groupingBy(TWUS::isPoliticalPosition,counting()))
                .entrySet().stream()
                .map(e -> e.getKey() + " : " + e.getValue())
                .forEach(System.out::println);
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

    public Integer getNumberOfUsers(){
        return users.size();
    }

    public Integer getNumberOfTweets(){
        return tweets.size();
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

    private String readFile(String path, Charset encoding)throws IOException{
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

//    public void saveJSON(){
//        //new Date().toString(); On the form: dow mon dd hh:mm:ss zzz yyyy
//        try {
//            DateFormat df = new SimpleDateFormat("yyyyMMdd_hhmm");
//            String date = df.format(new Date());
//            searchQuery.replaceAll("[^\\w#.-]", "_");
//            String filename = new StringBuilder().append(date).append("_").append(searchQuery).append(".json").toString();
//            Gson gson = new Gson();
//            String userJson = gson.toJson(tweets);
//            storeJSON(filename,userJson);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void saveStringOnFile(String filename, String s){
//        try{
//            PrintStream out = new PrintStream(new FileOutputStream(filename));
//            out.print(s);
//            System.out.println("JSON saved");
//        }catch(FileNotFoundException ex) {
//            System.out.println(ex.getMessage());
//        }
//    }

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
            Long y = this.getNumberOfYesUsers();
            Long n = this.getNumberOfNoUsers();
            Long tot = y+n;
            Double yp = (((double) y)/tot);
            Double np = (((double) n)/tot);

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

    private Double listToPerc(List<Status> statuses, Boolean other, Integer mode){
        HashSet<Long> userY = new HashSet<>();
        HashSet<Long> userN = new HashSet<>();
        HashSet<Long> userO = new HashSet<>();

        Double y;
        Double n;
        Double o;
        Double tot;
        int pp;
        Long id;
        for(Status s : statuses){
            pp = scanTweet(s);
            id = s.getUser().getId();
            if(pp>0){
                if(userN.contains(id)){
                    userN.remove(id);
                }else if(!userY.contains(id)){
                    userY.add(id);
                    if(userO.contains(id)) userO.remove(userO);
                }
            }else if(pp<0){
                if(userY.contains(id)){
                    userY.remove(id);
                }else if(!userN.contains(id)){
                    if(userO.contains(id)) userO.remove(id);
                    userN.add(id);
                }
            }else if(pp==0){
                if(!(userY.contains(id) || userN.contains(id))){
                    userO.add(id);
                }
            }
        }
        y = Double.valueOf(userY.size());
        n = Double.valueOf(userN.size());
        o = Double.valueOf(userO.size());
        if(other){
            tot = y+n+o;
        }else{
            tot = y+n;
        }

        if(tot == 0.0){
            return 0.0;
        }else{
            if(mode > 0){
                return y/tot;
            }else if(mode < 0){
                return n/tot;
            }else{
                if(other){
                    return o/tot;
                }else{
                    return null;
                }
            }
        }
    }

    public void toJSONVotingTrend(){
        try {
            Map<Date,Double> out = new TreeMap<>();
            Double yp;

            List<Status> sortedTweets;
            sortedTweets = tweets.stream().sorted((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())).collect(toList());
            Iterator<Status> itr = sortedTweets.iterator();

            Optional<Status> firstStatus = tweets.stream().collect(minBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Optional<Status> lastStatus = tweets.stream().collect(maxBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Date ds = firstStatus.get().getCreatedAt();
            Date de = lastStatus.get().getCreatedAt();

            Date startPeriod;
            Date endPeriod;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\'T\'HH.mm.ss");
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(ds);
            c1.set(Calendar.MINUTE,0);
            c1.set(Calendar.SECOND,0);
            c1.set(Calendar.MILLISECOND,0);
            c2.setTime(de);
            c2.add(Calendar.HOUR_OF_DAY,1);
            c2.set(Calendar.MINUTE,0);
            c2.set(Calendar.SECOND,0);
            c2.set(Calendar.MILLISECOND,0);
            startPeriod = c1.getTime();

            List<Status> tmpList = new LinkedList<>();
            List<Status> oldList = new LinkedList<>();
            List<Status> thisList = new LinkedList<>();
            Status tmpS;
            System.out.println("Exporting votingTrend.json...");
            int xyz=0;
            StringBuilder jsondata = new StringBuilder("");
            jsondata.append("[");
//        System.out.println("Date is : " + sdf.format(c1.getTime()));
            while(c1.compareTo(c2)<= 0){
                c1.add(Calendar.HOUR_OF_DAY,1);
                endPeriod = c1.getTime();
//                System.out.println(sdf.format(endPeriod));
                while(itr.hasNext() && ((tmpS = itr.next()).getCreatedAt().compareTo(endPeriod) < 0)){
                    tmpList.add(tmpS);
//                    System.out.println(xyz++);
                }
                thisList.addAll(tmpList);
                tmpList.addAll(oldList);

                yp = listToPerc(tmpList,false,1);
//sdf.format(startPeriod)
                jsondata.append("{\"date\":").append(startPeriod.getTime()).append(",");
                jsondata.append("\"SI\":").append(String.format(Locale.US,"%1$.4f",yp)).append(",");
                jsondata.append("\"NO\":").append(String.format(Locale.US,"%1$.4f",(1-yp))).append("}");
                if(c1.compareTo(c2)<= 0){
                    jsondata.append(",");
                }
//                System.out.println("<" + tmpList.size() + ">");
                startPeriod = endPeriod;
                oldList.addAll(thisList);
                tmpList.clear();
                thisList.clear();
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

    private String listToSYNO(List<Status> statuses){
        Long y = Long.valueOf(0);
        Long n = Long.valueOf(0);
        Long o = Long.valueOf(0);
        int pp = 0;
        for(Status s : statuses){
            pp = scanTweet(s);
            if(pp>0){
                y++;
            }else if(pp<0){
                n++;
            }else{
                o++;
            }
        }
        return String.format("\"SI\":%1$d,\"NO\":%2$d,\"Altro\":%3$d",y,n,o);
    }

    public void toJSONpopularitySum(){
        try {

            List<Status> sortedTweets;
            sortedTweets = tweets.stream().sorted((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())).collect(toList());
            Iterator<Status> itr = sortedTweets.iterator();

            Optional<Status> firstStatus = tweets.stream().collect(minBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Optional<Status> lastStatus = tweets.stream().collect(maxBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Date ds = firstStatus.get().getCreatedAt();
            Date de = lastStatus.get().getCreatedAt();

            Date startPeriod;
            Date endPeriod;
            int xyz=0;
            Status tmpS;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(ds);
            c1.set(Calendar.MINUTE,0);
            c1.set(Calendar.SECOND,0);
            c1.set(Calendar.MILLISECOND,0);
            c2.setTime(de);
            c2.set(Calendar.MINUTE,0);
            c2.set(Calendar.SECOND,0);
            c2.set(Calendar.MILLISECOND,0);
            startPeriod = c1.getTime();

            List<Status> tmpList = new LinkedList<>();

            StringBuilder jsondata = new StringBuilder("").append("[");
            System.out.println("Exporting popularitySum.json...");

            while(c1.compareTo(c2)<= 0){
                c1.add(Calendar.HOUR_OF_DAY,1);
                endPeriod = c1.getTime();
//                System.out.println(sdf.format(endPeriod));
                while(itr.hasNext() && ((tmpS = itr.next()).getCreatedAt().compareTo(endPeriod) < 0)){
                    tmpList.add(tmpS);
//                    System.out.println(xyz++);
                }

                jsondata.append("{\"date\":").append(startPeriod.getTime()).append(",");
                jsondata.append(listToSYNO(tmpList)).append("}");
                if(c1.compareTo(c2)<=0){
                    jsondata.append(",");
                }

//                System.out.println("<" + tmpList.size() + ">");
                startPeriod = endPeriod;
                tmpList.clear();
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

    public void toJSONGeoVote(){
        try {
            boolean first = true;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
            List<Status> sortedTweets = tweets.stream()
                    .filter(t -> cleanLocation(t))
                    .sorted((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                    .collect(toList());
            System.out.println("NGEO: " + sortedTweets.size());
            StringBuilder jsondata = new StringBuilder("").append("[");
            for(Status s : sortedTweets){
                if(!first){
                    jsondata.append(",");
                }
                first = false;
                jsondata.append("{\"date\":\"").append(sdf.format(s.getCreatedAt())).append("\",");
                jsondata.append("\"tplace\":\"").append(s.getUser().getLocation()).append("\",");
                jsondata.append("\"position\":").append(scanTweet(s)).append("}");
            }
            jsondata.append("]");
            String filename = "exports/" + "geoVoting.json";
            new File("exports").mkdir();
            storeJSON(jsondata.toString(),filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void toJSONpopVoting(){
        try {
            List<Status> sortedTweets;
            sortedTweets = tweets.stream().sorted((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())).collect(toList());
            Iterator<Status> itr = sortedTweets.iterator();

            Optional<Status> firstStatus = tweets.stream().collect(minBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Optional<Status> lastStatus = tweets.stream().collect(maxBy((t1,t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt())));
            Date ds = firstStatus.get().getCreatedAt();
            Date de = lastStatus.get().getCreatedAt();

            Date endPeriod;
            Status tmpS;
            LinkedList<PopVoto> pl = new LinkedList<>();
            List<PopVoto> spl;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(ds);
            c2.setTime(de);

            List<Status> tmpList = new LinkedList<>();

            StringBuilder jsondata = new StringBuilder("").append("[");
            System.out.println("Exporting popularityVote.json...");
            while(c1.compareTo(c2)<= 0){
                c1.add(Calendar.HOUR_OF_DAY,1);
                endPeriod = c1.getTime();
                while(itr.hasNext() && ((tmpS = itr.next()).getCreatedAt().compareTo(endPeriod) < 0)){
                    tmpList.add(tmpS);
                }
                pl.add(new PopVoto(tmpList.size(),listToPerc(tmpList,true,1),listToPerc(tmpList,true,-1),listToPerc(tmpList,true,0)));

//                System.out.println("<" + tmpList.size() + ">");

                tmpList.clear();
            }
            spl = pl.stream().sorted(comparing(PopVoto::getTot)).collect(toList());
            boolean first = true;
            for(PopVoto pop : spl){
                if(!first){
                    jsondata.append(",");
                }
                jsondata.append("{\"popularity\":").append(pop.getTot()).append(",");
                jsondata.append("\"yes\":").append(pop.getYes()).append(",");
                jsondata.append("\"other\":").append(pop.getOther()).append(",");
                jsondata.append("\"no\":").append(pop.getNo()).append("}");
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
