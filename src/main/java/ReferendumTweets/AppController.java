package ReferendumTweets;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;

/**
 * Created by Marco on 16/07/2016.
 */
@RestController
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
            tsh.loadJSON();
//            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
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
            return new RestMessage(status,State.Running);
        }else if(name.equalsIgnoreCase("stop") && status == 1){
            tsh.stopStream();
            System.out.println("Stream stopped.");
            status = 0;
            return new RestMessage(status,State.Stopped);
        }else if(name.equalsIgnoreCase("status")){
            if(status==0){
                return new RestMessage(status,State.Stopped);
            }else{
                return new RestMessage(status,State.Running);
            }
        }else if(name.equalsIgnoreCase("tweets")){
            if (status == 0) {
                return new RestMessage(tsh.getNumberOfTweets(),State.Stopped);
            }else if(status == 1){
                return new RestMessage(tsh.getNumberOfTweets(),State.Running);
            }else{
                return new RestMessage(tsh.getNumberOfTweets(),State.Unknown);
            }
        }else{
            return new RestMessage(status,State.Unknown);
        }
    }
}
