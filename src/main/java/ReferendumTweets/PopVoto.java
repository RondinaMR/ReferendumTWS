package ReferendumTweets;

/**
 * Created by Marco on 14/07/2016.
 */
public class PopVoto {
    private Long tot;
    private Double yes;
    private Double no;
    private Double other;

    public PopVoto(Long tot, Double yes, Double no, Double other) {
        this.tot = tot;
        this.yes = yes;
        this.no = no;
        this.other = other;
    }

    public Long getTot() {
        return tot;
    }

    public void setTot(Long tot) {
        this.tot = tot;
    }

    public Double getYes() {
        return yes;
    }

    public void setYes(Double yes) {
        this.yes = yes;
    }

    public Double getNo() {
        return no;
    }

    public void setNo(Double no) {
        this.no = no;
    }

    public Double getOther() {
        return other;
    }

    public void setOther(Double other) {
        this.other = other;
    }
}
