package ReferendumTweets;

import java.util.Date;

/**
 * Created by Marco on 16/07/2016.
 */
public class TweetsStats {
    private Date date = null;
    private Long yes = (long) 0;
    private Long no = (long) 0;
    private Long other = (long) 0;


    public TweetsStats() {

    }

    public TweetsStats(Date date, Long yes, Long no, Long other) {
        this.date = date;
        this.yes = yes;
        this.no = no;
        this.other = other;
    }

    /**********DATE**********/
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    /**********YES**********/
    public Long getYes() {
        return yes;
    }
    public void setYes(Long yes) {
        this.yes = yes;
    }
    /**********NO**********/
    public Long getNo() {
        return no;
    }
    public void setNo(Long no) {
        this.no = no;
    }
    /**********OTHER**********/
    public Long getOther() {
        return other;
    }
    public void setOther(Long other) {
        this.other = other;
    }
    /**********COMMON**********/
    public Long numberOfTweets() {
        return yes + no + other;
    }
    public void addYesNoOtherTU(Long yesT, Long noT, Long otherT) {
        this.yes += yesT;
        this.no += noT;
        this.other += otherT;
    }
    /**********VARIOUS**********/
    @Override
    public String toString() {
        return "TweetsStats{" +
                "date=" + date +
                ", yes=" + yes +
                ", no=" + no +
                ", other=" + other +
                '}';
    }
}
