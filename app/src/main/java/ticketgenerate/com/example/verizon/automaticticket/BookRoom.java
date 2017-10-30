package ticketgenerate.com.example.verizon.automaticticket;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import static ticketgenerate.com.example.verizon.automaticticket.CameraActivity.FetchedText;
import static ticketgenerate.com.example.verizon.automaticticket.CameraActivity.FetchedText1;
import static ticketgenerate.com.example.verizon.automaticticket.CameraActivity.FinalMap;
abstract class BookRoom extends AsyncTask<String,String,String> {
    int i;
    String data, Error;
    String Content;
    HashMap<String, String> map = new HashMap<String, String>();

    @Override
    final protected String doInBackground(String... strings) {
        BufferedReader reader = null;
        try {

            String serverURL = "http://ec2-18-221-218-105.us-east-2.compute.amazonaws.com/roomdetails.php";
            // Defined URL  where to send data
            URL url = new URL(serverURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Application", "Json");
            conn.setDoOutput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");
            i = conn.getResponseCode();
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;
            // Read Server ticketgenerate.com.example.verizon.webservices.Response
            while ((line = reader.readLine()) != null) {
                // Append server response in string
                sb.append(line + " ");
            }
            Content = sb.toString();
            JSONArray json = new JSONArray(Content);// ...
            JSONObject e = new JSONObject();
            for (int i = 0; i < json.length(); i++) {

                e = json.getJSONObject(i);
                map.put("Roomno", e.getString("roomno"));
                String Roomno = map.get("Roomno");
                if (Roomno.equals(FetchedText1)) {
                    map.put("Status", e.getString("status"));
                    String Status = map.get("Status");
                    if (Status.equals("1")) {
                        FinalMap = "Your Conference Room has been booked";
                    } else {
                        FinalMap = "Sorry. Conference room" + FetchedText + " is already booked.";
                    }
                }
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
