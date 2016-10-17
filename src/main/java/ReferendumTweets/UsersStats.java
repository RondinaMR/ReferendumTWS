package ReferendumTweets;

import java.util.Date;

/**
 * Created by marco on 17/09/2016.
 */
public class UsersStats {
    private Date date = null;
    private Long yesUsers = (long) 0;
    private Long noUsers = (long) 0;
    private Long otherUsers = (long) 0;

    public UsersStats(Date date, Long yesUsers, Long noUsers, Long otherUsers) {
        this.date = date;
        this.yesUsers = yesUsers;
        this.noUsers = noUsers;
        this.otherUsers = otherUsers;
    }

    public UsersStats() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getYesUsers() {
        return yesUsers;
    }

    public void setYesUsers(Long yesUsers) {
        this.yesUsers = yesUsers;
    }

    public void addYesUsers(Long yesUsers){
        this.yesUsers += yesUsers;
    }

    public Long getNoUsers() {
        return noUsers;
    }

    public void setNoUsers(Long noUsers) {
        this.noUsers = noUsers;
    }

    public void addNoUsers(Long NoUsers){
        this.noUsers += noUsers;
    }

    public Long getOtherUsers() {
        return otherUsers;
    }

    public void setOtherUsers(Long otherUsers) {
        this.otherUsers = otherUsers;
    }

    public void addOtherUsers(Long otherUsers){
        this.otherUsers += otherUsers;
    }

    public Long getTotUsers(){ return this.noUsers + this.yesUsers + this.otherUsers;}
}
