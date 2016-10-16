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

            // get the property value and print it out
            System.out.println(prop.getProperty("OAuthConsumerKey"));
            System.out.println(prop.getProperty("OAuthConsumerSecret"));
            System.out.println(prop.getProperty("OAuthAccessToken"));
            System.out.println(prop.getProperty("OAuthAccessTokenSecret"));

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
        }else{
            return null;
        }
        /*String result = "";
        InputStream inputStream = null;
        String OAuthConsumerKey,OAuthConsumerSecret,OAuthAccessToken,OAuthAccessTokenSecret;
        OAuthConsumerKey = new String("");
        OAuthConsumerSecret = new String("");
        OAuthAccessToken = new String("");
        OAuthAccessTokenSecret = new String("");
        try {
            Properties prop = new Properties();
            String propFileName = "/resources/config.properties";

            inputStream = PropertiesManager.class.getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            // get the property value
            OAuthConsumerKey = prop.getProperty("OAuthConsumerKey");
            OAuthConsumerSecret = prop.getProperty("OAuthConsumerSecret");
            OAuthAccessToken = prop.getProperty("OAuthAccessToken");
            OAuthAccessTokenSecret = prop.getProperty("OAuthAccessTokenSecret");

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        if(name.equals("OAuthConsumerKey")){
            return OAuthConsumerKey;
        }else if(name.equals("OAuthConsumerSecret")){
            return OAuthConsumerSecret;
        }else if(name.equals("OAuthAccessToken")){
            return OAuthAccessToken;
        }else if(name.equals("OAuthAccessTokenSecret")){
            return OAuthAccessTokenSecret;
        }else{
            return new String("ERROR");
        }*/
    }
}
