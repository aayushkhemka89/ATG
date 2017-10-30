package ticketgenerate.com.example.verizon.automaticticket;

/**
 * Created by user on 9/2/2017.
 */

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/***
 Restful service helper class that can deal mainly with String input and output.
 Can also accept and return JSON objects.
 */
public class WebHelper {

    int connectionTimeout = 10; // seconds
    int readTimeout = 10;
    HttpURLConnection connection;
    int responseCode;
    HashMap<String, String> properties;
    //JSONParser jParser;

    public WebHelper() {
        //jParser = new JSONParser();
        properties = new HashMap<String, String>();
    }

    public int getResponseCode () {
        return (responseCode);
    }

    public void setProperty (String name, String value) {
        properties.put(name, value);
    }

    public void clearProperties () {
        properties.clear();
    }

    // TODO
    public JSONObject get (String url, JSONObject data) throws Exception {
        return null;
    }

    /***
     Returns the server response as a String.
     On server read error, returns null.
     On connection error, throws exception.
     */
    public String get (String url) throws Exception {

        URL serviceUrl = new URL(url);
        this.connection = (HttpURLConnection) serviceUrl.openConnection();
        connection.setRequestMethod("GET");
        //connection.setConnectionTimeout(connectionTimeout*1000);
        connection.setReadTimeout(readTimeout*1000);
        if (properties.size() > 0)
            for (Map.Entry<String, String> entry : properties.entrySet())
                connection.setRequestProperty (entry.getKey(), entry.getValue());

        String response = null;
        connection.connect();
        this.responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK)
            response = readStream(connection.getInputStream());
        return (response);
    }

    public JSONObject post (String url, JSONObject data) throws Exception {
        Log.d("webhelper","jsonobj");
        String str = post(url, data.toString());
        return (new JSONObject(str));

    }

    /***
     // Encode a single json object as a one-length array
     // The caller needs to cast it to a single JSON object or an array
     // TODO: how will you send a full array ?
     public Object post (String url, JSONArray data) throws Exception {
     String str = post(url, data[0].toString());
     return (JSONParser.parse(str));
     }
     ***/

    public String post (String url, String data) throws Exception {
        Log.d("webhelper","string");
        URL serviceUrl = new URL(url);
        this.connection = (HttpURLConnection) serviceUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setReadTimeout(readTimeout*1000);

        if (properties.size() > 0)
            for (Map.Entry<String, String> entry : properties.entrySet())
                connection.setRequestProperty (entry.getKey(), entry.getValue());

        OutputStream ostream = connection.getOutputStream();
        writeStream (ostream, data);

        String response = null;
        connection.connect();
        this.responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_OK)
            response = readStream(connection.getInputStream());
        return (response);
    }

    private String readStream (InputStream istream) {

        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(istream));
            String line = "";
            while ((line = reader.readLine()) != null)
                response.append(line);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (reader != null) {
                try {reader.close();}
                catch (IOException e)
                {e.printStackTrace();}
            }
        }
        return response.toString();
    }

    private void writeStream (OutputStream ostream, String data) throws Exception {

        DataOutputStream dos = new DataOutputStream(ostream);
        dos.writeBytes(data);
        dos.flush();
        dos.close();
        ostream.close();
        /****
         // Aliter (for character data) :
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ostream));
         writer.write(data);
         writer.flush();
         writer.close();
         ostream.close();
         ****/
    }


    public static void main(String... args) throws Exception {

        String url = "";
        WebHelper helper = new WebHelper();

        url = "http://ip.jsontest.com/";
        G.trace(helper.get(url));

        helper.setProperty("User-Agent", "Mozilla/5.0");
        helper.setProperty("Accept-Language", "en-US,en;q=0.5");
        helper.setProperty("Accept", "application/json");
        helper.setProperty("Content-Type", "application/json;charset=UTF-8");
        //helper.setProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        //helper.setProperty("Authorization", "Bearer " + accessToken);

        url = "http://headers.jsontest.com/";
        G.trace(helper.get(url));
        helper.clearProperties();
        G.trace(helper.get(url));

        url = "http://validate.jsontest.com/?json={\"name\":\"raja\",\"age\":55}";
        G.trace(helper.get(url));
        G.trace(helper.getResponseCode());
        // Aliter:
        G.trace(helper.post(url, new JSONObject()));  // POST with dummy payload
        G.trace(helper.getResponseCode());
    }
}
/***--------------------------------------------------------------------------------------------***
 class G {
 public static boolean debug = true;

 public static void trace (Object o) {
 if (!debug) return;
 if (o==null) o = "(null)";
 System.out.println(o.toString());
 }

 public static void trace2 (Object o) {
 if (o==null) o = "(null)";
 System.out.println(o.toString());
 }
 }
 ***---------------------------------------------------------------------------------------------***/




