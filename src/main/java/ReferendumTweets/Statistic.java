package ReferendumTweets;

import java.util.Date;

/**
 * Created by Marco on 16/07/2016.
 */
public class Statistic {
    private Date date = null;
    private Long yes = (long) 0;
    private Long no = (long) 0;
    private Long other = (long) 0;
    private Long yesUsers = (long) 0;
    private Long noUsers = (long) 0;
    private Long otherUsers = (long) 0;

    public Statistic(Date date, Long yes, Long no, Long other, Long yesUsers, Long noUsers, Long otherUsers) {
        this.date = date;
        this.yes = yes;
        this.no = no;
        this.other = other;
        this.yesUsers = yesUsers;
        this.noUsers = noUsers;
        this.otherUsers = otherUsers;
    }

    public Statistic() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Long getYes() {
        return yes;
    }

    public void setYes(Long yes) {
        this.yes = yes;
    }

    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public Long getOther() {
        return other;
    }

    public void setOther(Long other) {
        this.other = other;
    }

    public Long getYesUsers() {
        return yesUsers;
    }

    public void setYesUsers(Long yesUsers) {
        this.yesUsers = yesUsers;
    }

    public Long getNoUsers() {
        return noUsers;
    }

    public void setNoUsers(Long noUsers) {
        this.noUsers = noUsers;
    }

    public Long getOtherUsers() {
        return otherUsers;
    }

    public void setOtherUsers(Long otherUsers) {
        this.otherUsers = otherUsers;
    }

    public Long getNumberOfTweets(){
        return yes+no+other;
    }

    public void addYesNoOtherTU(Long yesT, Long noT, Long otherT,Long yesU, Long noU, Long otherU){
        this.yes += yesT;
        this.no += noT;
        this.other += otherT;
        this.yesUsers += yesU;
        this.noUsers += noU;
        this.otherUsers += otherU;
    }
}
