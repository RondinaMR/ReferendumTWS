package ReferendumTweets;

/**
 * Created by marco on 26/11/2016.
 */
public class TmpStats {
    Long yes;
    Long no;
    Long other;

    public TmpStats() {
        clearYes();
        clearNo();
        clearOther();
    }

    public TmpStats(Long yes, Long no, Long other) {
        this.yes = yes;
        this.no = no;
        this.other = other;
    }
    /*************************/
    public Long getYes() {
        return yes;
    }

    public void setYes(Long yes) {
        this.yes = yes;
    }

    public void addYes(){
        this.yes++;
    }

    public void clearYes(){
        this.yes = (long) 0;
    }
    /*************************/
    public Long getNo() {
        return no;
    }

    public void setNo(Long no) {
        this.no = no;
    }

    public void addNo(){
        this.no++;
    }

    public void clearNo(){
        this.no = (long) 0;
    }
    /*************************/
    public Long getOther() {
        return other;
    }

    public void setOther(Long other) {
        this.other = other;
    }

    public void addOther(){
        this.other++;
    }

    public void clearOther(){
        this.other = (long) 0;
    }
    /***************************/
    public void clearAll(){
        clearYes();
        clearNo();
        clearOther();
    }
}
