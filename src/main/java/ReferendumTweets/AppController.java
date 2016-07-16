package ReferendumTweets;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Marco on 16/07/2016.
 */
@RestController
public class AppController {


    private TweetStreamHandler tsh;
    private Timer timer = new Timer();
    private TimerTask hourlyTask = new TimerTask() {
        @Override
        public void run () {
            tsh.exportAllJSON();
            System.out.println("***** Exported data at " + System.currentTimeMillis() + " *****");
        }
    };


    private void init(){
        try {
            tsh = new TweetStreamHandler();
            tsh.loadJSON();
            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
            timer.schedule(hourlyTask, 0, 1000*60*60);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public AppController() {
        init();
        System.out.println("INIZIALIZZATO");
    }

    @RequestMapping("/referendum")
    public void controller(@RequestParam(value="cmd", defaultValue="null") String name) {
        if(name.equalsIgnoreCase("start")){
            tsh.startStream("#iovotono","#bastaunsi","#iovotosi","#italiachedicesi","#referendumcostituzionale");
        }else if(name.equalsIgnoreCase("stop")){
            tsh.stopStream();

        }
    }
}
