package ticketgenerate.com.example.verizon.automaticticket;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by user on 9/2/2017.
 */

public class Kairos {

    public static final int HTTP_OK = 200;
    private static final String APP_ID  = "25d1728a";
    private static final String APP_KEY = "67e08861eb721f37c3bb24c4f5b1d04a";
    private static String TAG = "gallery";
    String defaultGallery = "MyGallery";
    private WebHelper webHelper;
    private String url = null;
    private JSONObject responseObject = null;
    private int responseCode = 0;
    private float confidenceThreshold = 0.65f;
    private ArrayList networkError;
    private ArrayList imageError;
    private ArrayList noFaceDetected;
    private ArrayList personNotIdentified;
    private ArrayList registrationSuccess;
    private ArrayList galleryDeleted;
    private ArrayList speechnotbound;

    public Kairos() {
        G.trace("Kairos is starting...");
        webHelper = new WebHelper();
        setProperties();
        G.trace("Default image gallery: "+defaultGallery);
        G.trace("Confidence Threshold: "+confidenceThreshold);

        // TODO: convert all these dangerous stings to static constants
        networkError = new ArrayList();
        networkError.add("Error:"); networkError.add("Network");
        imageError = new ArrayList();
        imageError.add("Error:"); imageError.add("Image");
        noFaceDetected = new ArrayList();
        noFaceDetected.add("Error:"); noFaceDetected.add("No face");
        personNotIdentified = new ArrayList();
        personNotIdentified.add("Identity:"); personNotIdentified.add("FAILED");
        speechnotbound = new ArrayList();
       speechnotbound.add("Identity:"); speechnotbound.add("SpeechFailed");
        registrationSuccess = new ArrayList();
        registrationSuccess.add("Registration:"); registrationSuccess.add("SUCCESS");
        galleryDeleted = new ArrayList();
        galleryDeleted.add("Success:"); personNotIdentified.add("Selected gallery deleted");
    }

    public void setDefaultGallery(String galleryName) {
        defaultGallery = galleryName;
        G.trace("Default image gallery is set to: "+defaultGallery);
    }

    public void setConfidenceThreshold (float threshold) {
        if (threshold > 0.05 && threshold <= 1.0)
            confidenceThreshold = threshold;
        else
            G.trace("Invalid threshold value");
        G.trace("Confidence Threshold is set to: "+confidenceThreshold);
    }

    public void setProperties () {
        webHelper.clearProperties();
        //webHelper.setProperty("Accept", "application/json");
        webHelper.setProperty("Content-Type", "application/json;charset=UTF-8");
        webHelper.setProperty("app_id", APP_ID);
        webHelper.setProperty("app_key", APP_KEY);
    }

    // utility method to get the public IP address of the client device.
    // This is not part of the Kairos API.
    public String getIPAddress () {
        url = "http://ip.jsontest.com/";
        String ip = "Error:Cannot get the IP address";
        try {
            ip = webHelper.get(url);
        } catch (Exception e)
        {
            G.trace2("NETWORK ERROR: " +e.getMessage());
            e.printStackTrace();
        }
        return ip;
    }

    // this has to be called immediately after a transacion, before making a new call
    public int getResponseCode() {
        return (webHelper.getResponseCode());
    }

    // this has to be called immediately after a transacion, before making a new call
    public JSONObject getResponse() {
        return (responseObject);
    }

    public ArrayList<String> getAllGalleries() {
        url = "https://api.kairos.com/gallery/list_all";
        ArrayList<String> galleries = new ArrayList<>();
        JSONObject data=new JSONObject();

        try {
            responseObject = webHelper.post(url, data); // dummy payload is necessary !
            G.trace(responseObject);
            G.trace(webHelper.getResponseCode());
            if (webHelper.getResponseCode()==HTTP_OK && !responseObject.has("Errors"))
            {
                //galleries =  (ArrayList<String>)responseObject.get("gallery_ids");
                JSONArray jarray = (JSONArray)responseObject.get("gallery_ids");
                Log.d(TAG,"JSON ARRAY:"+jarray);
                galleries=new ArrayList<>(jarray.length());
                for(int i=0;i<jarray.length();i++)
                {
                    galleries.add(jarray.get(i).toString());
                }
                Log.d(TAG,"Galleries are hello:"+galleries);
            }
        }
        catch (Exception e) {e.printStackTrace();}
        return galleries;
    }

    public ArrayList<String> deleteGallery(String galleryName) throws Exception {

        url = "https://api.kairos.com/gallery/remove";
        JSONObject data = new JSONObject();
        data.put("gallery_name", galleryName);

        try {
            responseObject = webHelper.post(url, data);
            G.trace(webHelper.getResponseCode());
            G.trace(responseObject);
            if (webHelper.getResponseCode()!=HTTP_OK)
                return networkError;
            if (responseObject.has("Errors"))
                return noFaceDetected;
        }
        catch (Exception e)
        {
            G.trace2("-- ERROR: could not enroll image --");
            e.printStackTrace();
            return networkError;
        }
        return galleryDeleted;
    }





    public ArrayList<String> getSubject() throws Exception {
        return getSubject(defaultGallery);
    }

    public ArrayList<String> getSubject(String galleryName) throws Exception {

        url = "https://api.kairos.com/gallery/view";
        ArrayList<String> personIDs = new ArrayList<String>();
        JSONObject data = new JSONObject();
        data.put("gallery_name", galleryName);
        try {
            responseObject = webHelper.post(url, data);
            G.trace(webHelper.getResponseCode());
            G.trace(responseObject);
            if (webHelper.getResponseCode()==HTTP_OK && !responseObject.has("Errors"));
            // personIDs =  (ArrayList<String>)responseObject.get("subject_ids");
            JSONArray jarray = (JSONArray)responseObject.get("subject_ids");
            Log.d(TAG,"JSON ARRAY:"+jarray);
            personIDs=new ArrayList<>(jarray.length());
            for(int i=0;i<jarray.length();i++)
            {
                personIDs.add(jarray.get(i).toString());
            }
            Log.d(TAG,"Subjects are hello:"+personIDs);
        }
        catch (Exception e) {e.printStackTrace();}
        return personIDs;
    }


    public ArrayList<String> deleteSubject(String subject, String galleryName) throws Exception {

        url = "https://api.kairos.com/gallery/remove_subject";
        JSONObject data = new JSONObject();
        data.put("gallery_name", galleryName);
        data.put("subject_id", subject);

        try {
            responseObject = webHelper.post(url, data);
            G.trace(webHelper.getResponseCode());
            G.trace(responseObject);
            if (webHelper.getResponseCode()!=HTTP_OK)
                return networkError;
            if (responseObject.has("Errors"))
                return noFaceDetected;
        }
        catch (Exception e)
        {
            G.trace2("-- ERROR: could not enroll image --");
            e.printStackTrace();
            return networkError;
        }
        return galleryDeleted;
    }

/*

    public ArrayList enroll (String imageFilePath, String personName) throws Exception {
        return (enroll(imageFilePath, personName, defaultGallery));
    }


    public ArrayList enroll (String imageFilePath, String personName, String galleryName) throws Exception {

        url = "https://api.kairos.com/enroll";
        ArrayList resultList = new ArrayList();
        String processed = preProcess (imageFilePath);
        if (processed==null)
            return (imageError);

        JSONObject data = new JSONObject();
        data.put("image", processed);
        data.put("subject_id", personName);
        data.put("gallery_name", galleryName);

        try {
            responseObject = webHelper.post(url, data);
            G.trace(webHelper.getResponseCode());
            G.trace(responseObject);
            if (webHelper.getResponseCode()!=HTTP_OK)
                return networkError;
            if (responseObject.has("Errors"))
                return noFaceDetected;
        }
        catch (Exception e)
        {
            G.trace2("-- ERROR: could not enroll image --");
            e.printStackTrace();
            return networkError;
        }
        return registrationSuccess;
    }   */


    public ArrayList identify (String base64, String galleryName) throws Exception {
        url = "https://api.kairos.com/recognize";
        ArrayList resultList = new ArrayList();
       // String processed = preprocess(processedImage);
        if (base64==null)
            return (imageError);

        JSONObject data = new JSONObject();
        data.put("image", base64);
        data.put("gallery_name", galleryName);
        data.put("threshold", confidenceThreshold);
        try {
            this.responseObject = webHelper.post(url, data);
            G.trace(webHelper.getResponseCode());
            G.trace(responseObject);
            if (webHelper.getResponseCode()!=HTTP_OK)
                return networkError;
            if (responseObject.has("Errors"))
                return noFaceDetected;
            resultList = prepareResponse(responseObject);
            Log.d(TAG,"Values in identify;Resultlist:"+resultList);

        }
        catch (Exception e)
        {
            G.trace2("-- ERROR: could not identify image --");
            e.printStackTrace();
            return networkError;
        }
        return resultList;
    }
/*private String preprocess(Bitmap mbitmap){
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    mbitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
    byte[] byteArray = byteArrayOutputStream .toByteArray();
    String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
    return encoded;
}*/
    private ArrayList prepareResponse (JSONObject jobject) throws JSONException {

        JSONArray jarray = (JSONArray)jobject.get("images");
        //G.trace(jarray);
        JSONObject firstobject = (JSONObject)jarray.get(0);
        JSONObject jtxn = (JSONObject)firstobject.get("transaction");
        // TODO: look for status= success or failure
        if (jtxn.has("subject_id") && jtxn.has("confidence")) {
            ArrayList tuple = new ArrayList();
            tuple.add ((String)jtxn.get("subject_id"));
            Object obj = jtxn.get("confidence");
            int confidence = Math.round(100* Float.parseFloat(obj.toString()));
            tuple.add ("Conf: " +confidence +" %");
            return tuple;
        }
        return (personNotIdentified);
    }
}


