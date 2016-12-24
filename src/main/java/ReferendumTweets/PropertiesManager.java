package ReferendumTweets;

import java.io.*;
import java.util.Properties;

/**
 * Created by marco on 16/10/2016.
 */
public class PropertiesManager {

    public void setPropValues(){
        Properties prop = new Properties();
        OutputStream output = null;

        try {

            output = new FileOutputStream("resources/config.properties");

            // set the properties value
            prop.setProperty("propertyName", "propertyValue");

            // save properties to project root folder
            prop.store(output, null);

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    public String getPropValues(String name) throws IOException {

        Properties prop = new Properties();
        InputStream input = null;

        try {

            input = new FileInputStream("resources/config.properties");

            // load a properties file
            prop.load(input);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(name.equals("OAuthConsumerKey")){
            return prop.getProperty("OAuthConsumerKey");
        }else if(name.equals("OAuthConsumerSecret")){
            return prop.getProperty("OAuthConsumerSecret");
        }else if(name.equals("OAuthAccessToken")){
            return prop.getProperty("OAuthAccessToken");
        }else if(name.equals("OAuthAccessTokenSecret")){
            return prop.getProperty("OAuthAccessTokenSecret");
        }else if(name.equals("SecureKey")){
            return prop.getProperty("SecureKey");
        }else{
            return null;
        }
    }
}
