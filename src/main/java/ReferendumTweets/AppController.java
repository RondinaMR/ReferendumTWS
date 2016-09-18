package ReferendumTweets;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by Marco on 16/07/2016.
 */
@RestController
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class AppController {

    public enum State { Stopped, Running, Unknown}

    public static void main(String[] args) throws Exception {
        SpringApplication.run(AppController.class, args);
    }

    public AppController() {
        System.out.println("START INIT");
        init();
        System.out.println("END INIT");
    }
    private int status = 0;
    private TweetStreamHandler tsh;
    private Timer timer = new Timer();
    private TimerTask hourlyTask = new TimerTask() {
        @Override
        public void run () {
            System.out.println("***** Starting exporting data at " + System.currentTimeMillis() + " *****");
            tsh.exportAllJSON();
            System.out.println("***** Exported data at " + System.currentTimeMillis() + " *****");
        }
    };

    private void init(){
        try {
            tsh = new TweetStreamHandler();
//            tsh.loadJSON();
//            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
            tsh.loadStatistics();
            System.out.println("Starting TIMER");
            timer.schedule(hourlyTask, 0, 1000*60*60);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/referendum")
    public RestMessage controller(@RequestParam(value="cmd", defaultValue="null") String name) {
        if(name.equalsIgnoreCase("start") && status == 0){
            System.out.println("Starting stream...");
            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
            status = 1;
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Running);
        }else if(name.equalsIgnoreCase("stop") && status == 1){
            tsh.stopStream();
            System.out.println("Stream stopped.");
            status = 0;
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
        }else if(name.equalsIgnoreCase("status")){
            if(status==0){
                return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
            }else{
                return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Running);
            }
        }else if(name.equalsIgnoreCase("loadfiles") && status == 0){
            tsh.loadJSON();
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
        }else if(name.equalsIgnoreCase("loadstat") && status == 0){
            tsh.loadStatistics();
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
        }else if(name.equalsIgnoreCase("savestat") && status == 0){
            tsh.toJSONstatistics();
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
        }else if(name.equalsIgnoreCase("export") && status == 0){
            tsh.exportAllJSON();
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Stopped);
        }else{
            return new RestMessage(status,tsh.getNumberOfTweets(),tsh.getNumberOfUsers(),State.Unknown);
        }
    }

    @CrossOrigin()
    @RequestMapping(value="/referendum/voting_intentions",produces="application/json")
    public @ResponseBody Json apiVI() {
        return new Json(tsh.toStringVotingIntentions());
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/voting_trend",produces="application/json")
    public @ResponseBody Json apiVT() {
        return new Json(tsh.toStringVotingTrend());
    }
    @RequestMapping(value="/referendum/voting_day_trend",produces="application/json")
    public @ResponseBody Json apiVDT() {
        return new Json(tsh.toStringVotingDayTrend());
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/voting_week_trend",produces="application/json")
    public @ResponseBody Json apiVWT() {
        return new Json(tsh.toStringVotingWeekTrend());
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/popularity_sum",produces="application/json")
    public @ResponseBody Json apiPS() {
        return new Json(tsh.toStringPopularitySum());
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/popularity_vote",produces="application/json")
    public @ResponseBody Json apiPU() {
        return new Json(tsh.toStringPopularityVote());
    }
    //Hashtags
    @CrossOrigin()
    @RequestMapping(value="/referendum/hashtags_no",produces="application/json")
    public @ResponseBody Json apiHN() {
        return new Json(tsh.toStringEntity("hashtags","No"));
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/hashtags_yes",produces="application/json")
    public @ResponseBody Json apiHY() {
        return new Json(tsh.toStringEntity("hashtags","Yes"));
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/hashtags_tot",produces="application/json")
    public @ResponseBody Json apiHT() {
        return new Json(tsh.toStringEntity("hashtags","Tot"));
    }
    //Mentions
    @CrossOrigin()
    @RequestMapping(value="/referendum/mentions_no",produces="application/json")
    public @ResponseBody Json apiMN() {
        return new Json(tsh.toStringEntity("mentions","No"));
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/mentions_yes",produces="application/json")
    public @ResponseBody Json apiMY() {
        return new Json(tsh.toStringEntity("mentions","Yes"));
    }
    @CrossOrigin()
    @RequestMapping(value="/referendum/mentions_tot",produces="application/json")
    public @ResponseBody Json apiMT() {
        return new Json(tsh.toStringEntity("mentions","Tot"));
    }

    class Json {

        private final String value;

        public Json(String value) {
            this.value = value;
        }

        @JsonValue
        @JsonRawValue
        public String value() {
            return value;
        }
    }
}
