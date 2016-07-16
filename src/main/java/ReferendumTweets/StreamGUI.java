package ReferendumTweets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;

import javax.swing.*;
import javax.swing.border.LineBorder;
/**
 * Created by Marco on 03/07/2016.
 */
public class StreamGUI extends JFrame{
    private static final long serialVersionUID = 1L;
    private TweetStreamHandler tsh;
    public JLabel l1 = new JLabel("TWEETS: ");
    public JLabel l2 = new JLabel("");
    public JLabel l3 = new JLabel("");
    public JLabel l4 = new JLabel("");
    public JButton stop = new JButton("Stop");
    public JButton start = new JButton("Start");
    public JButton refresh = new JButton("Refresh");
    public JButton exportVI = new JButton("Voting Intentions");
    public JButton exVotingTrend = new JButton("Voting Trend");
    public JButton exPop = new JButton("Popularity");
    public JButton exGeo = new JButton("Geo Voting");
    public JButton exPopVote = new JButton("Voting/Popularity");

    public StreamGUI(TweetStreamHandler tsh){
        this.tsh = tsh;
        start.setEnabled(false);

        setLayout(new BorderLayout());

        JPanel upper = new JPanel();
        upper.setLayout(new FlowLayout());
        upper.add(l1);
        upper.add(l2);
        upper.add(l3);
        upper.add(l4);
        upper.add(refresh);
//        upper.add(login);
        add(upper,BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new FlowLayout());

//        JPanel info = new JPanel();
//        info.add(name);
//        lower.add(info,BorderLayout.NORTH);

//        JPanel details = new JPanel();
//        details.setLayout(new GridLayout(1,1,5,5));

//        details.add(friends);
//        lower.add(details,BorderLayout.CENTER);
        center.add(start);
        center.add(stop);
        add(center,BorderLayout.CENTER);
        center.setBorder(new LineBorder(Color.DARK_GRAY));

        JPanel lower = new JPanel();
        lower.setLayout(new FlowLayout());
        lower.add(exportVI);
        lower.add(exVotingTrend);
        lower.add(exPop);
        lower.add(exGeo);
        lower.add(exPopVote);
        add(lower,BorderLayout.SOUTH);


        setSize(600,300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);

        start.addActionListener((event) -> {
            if(event.getActionCommand().equals("Start")){
                tsh.startStream();
                stop.setEnabled(true);
                start.setEnabled(false);
            }
        });
        stop.addActionListener((event) -> {
            if(event.getActionCommand().equals("Stop")){
                tsh.stopStream();
//                tsh.saveJSON();
                start.setEnabled(true);
                stop.setEnabled(false);
            }
        });
        refresh.addActionListener((event) -> {
            if(event.getActionCommand().equals("Refresh")){
                Long y = tsh.getNumberOfYesUsers();
                Long n = tsh.getNumberOfNoUsers();
                Long tot = y + n;
                Double yp = (((double) y)/tot)*100.0;
                Double yn = (((double) n)/tot)*100.0;
                System.out.println("y: " + y + " / n: " + n + " / tot: "+tot+" / yp: " + yp + "% / yn: " + yn + "%");
                l2.setText(tsh.getNumberOfTweets().toString());
                l3.setText("SI: " + tsh.getNumberOfYesUsers().toString() + " " + String.format("%1$.2f",yp) + "%");
                l4.setText("NO: " + tsh.getNumberOfNoUsers().toString() + " " + String.format("%1$.2f",yn) + "%");
            }
        });
        exportVI.addActionListener((event) -> {
            if(event.getActionCommand().equals("Voting Intentions")){
                refresh.doClick();
                tsh.toJSONVotingIntentions();
                JOptionPane.showMessageDialog(null,"Voting Intentions successfully exported!");
            }
        });
        exVotingTrend.addActionListener((event) -> {
            if(event.getActionCommand().equals("Voting Trend")){
                refresh.doClick();
                tsh.toJSONVotingTrend();
                JOptionPane.showMessageDialog(null,"Voting Trend successfully exported!");
            }
        });
        exPop.addActionListener((event) -> {
            if(event.getActionCommand().equals("Popularity")){
                refresh.doClick();
                tsh.toJSONpopularitySum();
                JOptionPane.showMessageDialog(null,"Popularity successfully exported!");
            }
        });
        exGeo.addActionListener((event) -> {
            if(event.getActionCommand().equals("Geo Voting")){
                refresh.doClick();
//                tsh.toJSONGeoVote();
                JOptionPane.showMessageDialog(null,"Geo Voting successfully exported!");
            }
        });
        exPopVote.addActionListener((event) -> {
            if(event.getActionCommand().equals("Voting/Popularity")){
                refresh.doClick();
                tsh.toJSONpopVoting();
                JOptionPane.showMessageDialog(null,"Voting/Popularity successfully exported!");
            }
        });



//        new Thread(){
//            public void run(){
//                try{Thread.sleep(1000);}catch(InterruptedException ie){ie.printStackTrace();}
//                SwingUtilities.invokeLater(() -> l2.setText(tsh.getNumberOfTweets().toString()));
//            }
//        }.start();


//        id.addKeyListener(new KeyListener(){
//            @Override
//            public void keyTyped(KeyEvent e) {
//
//            }
//            @Override
//            public void keyPressed(KeyEvent e) {
//
//            }
//            @Override
//            public void keyReleased(KeyEvent e) {
//                if(e.getKeyCode() == KeyEvent.VK_ENTER){
//
//                }
//            }
//        });
    }

}
