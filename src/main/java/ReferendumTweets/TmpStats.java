package ReferendumTweets;

import java.util.LinkedHashSet;

/**
 * Created by marco on 26/11/2016.
 */
public class TmpStats {
    private Long yes;
    private Long no;
    private Long other;
    private LinkedHashSet<Long> users;

    public TmpStats() {
        users = new LinkedHashSet<>();
        clearAll();
    }

    public TmpStats(Long yes, Long no, Long other) {
        this.yes = yes;
        this.no = no;
        this.other = other;
        users = new LinkedHashSet<>();
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
        clearUsers();
    }

    /***************************/
    public boolean containUser(Long id){return users.contains(id);}
    public void addUser(Long id){if(!users.contains(id)){users.add(id);}}
    public void clearUsers(){users.clear();}
    /***************************/

    @Override
    public String toString() {
        return "TmpStats{" +
                "yes=" + yes +
                ", no=" + no +
                ", other=" + other +
                '}';
    }
}
