package ticketgenerate.com.example.verizon.automaticticket;

import android.content.Context;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static ticketgenerate.com.example.verizon.automaticticket.CameraActivity.FinalMap;
import static ticketgenerate.com.example.verizon.automaticticket.CameraActivity.User;

/**
 * Created by verizon on 23/10/17.
 */

abstract  class CreateIncident extends AsyncTask<String,String,String> {
    int i;
    String FetchedText,Status;
    TextToSpeech tts;
    String data, Error;
    String Content;
    HashMap<String, String> map = new HashMap<String, String>();
    @Override
    final protected  String doInBackground(String... strings) {
        BufferedReader reader = null;
        try {

            String serverURL = "http://ec2-18-221-218-105.us-east-2.compute.amazonaws.com/CreateIncident.php";
            String urlParameters  = "eid="+User+"&desc="+FetchedText;
            // Defined URL  where to send data
            URL url = new URL(serverURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Application", "Json");
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            DataOutputStream dStream = new DataOutputStream(conn.getOutputStream());
            dStream.writeBytes(urlParameters); //Writes out the string to the underlying output stream as a sequence of bytes<br />
            dStream.flush(); // Flushes the data output stream.<br />
            dStream.close();
            i = conn.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + " ");
            }
            Content = sb.toString();
            JSONArray json = new JSONArray(Content);// ...
            JSONObject e = new JSONObject();
            e = json.getJSONObject(0);
            map.put("Status", e.getString("status"));
            Status = map.get("Status");
            if (Status.equals("Created")) {
                FinalMap = "Your Ticket has been created. Thank You for using Service Now";
            }
            else {
                FinalMap = "Sorry. Please try again.";
            }
        } catch (Exception ex) {
            Error = ex.getMessage();
        } finally {
            try {

                reader.close();
            } catch (Exception ex) {
            }
        }

        return FinalMap;
    }


}

