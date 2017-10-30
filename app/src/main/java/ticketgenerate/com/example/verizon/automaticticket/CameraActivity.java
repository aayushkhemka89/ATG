package ticketgenerate.com.example.verizon.automaticticket;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
public class CameraActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;
    RelativeLayout gameBoard;
    LinearLayout LL;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    ImageButton btn;
    public TextToSpeech tts;
    private static final int REQ_CODE_SPEECH_INPUT = 100;
    TextView Hidden;
    String FetchedVoice, txt="Please Wait";
    static  String FinalMap,FetchedText1,FetchedText,User;
    String Keywordfile = "Keyword",MapFile="Map",wrong = "Sorry. I am expecting a different response",mainkey,mainValue;
    ArrayList<String> result;
    int t=0,x=0;
    HashMap<String, String> map = new HashMap<String, String>();
      View.OnClickListener ClickListener = new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              int selected_item = (Integer) v.getTag();
              if (selected_item == 4) {
                  t=0;
                 speech();
              } else if (selected_item == 1 || selected_item == 2 || selected_item == 3) {
                  t=1;
                  if (FetchedText.equals("room")) {
                           Toast.makeText(getApplicationContext(),"Contacting please wait",Toast.LENGTH_LONG).show();
                           new Room().execute();
                      }
                  else if (FetchedText.equals("printer")) {
                      Toast.makeText(getApplicationContext(),"Contacting please wait",Toast.LENGTH_LONG).show();
                      new RaiseTicket().execute();

                  }
              }

          }
      };

    public void speech() {
        t=0;
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                int result = tts.setLanguage(Locale.US);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    Toast.makeText(getApplicationContext(),txt,Toast.LENGTH_LONG).show();
                    HashMap<String, String> myHashAlarm = new HashMap<>();
                    myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID1");
                    txt = LoadKeywords(Keywordfile, FetchedText);
                    tts.speak(txt, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
//                    FetchedText = null;

                }
                //Speach input automatically turns on
                tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

                    @Override
                    public void onUtteranceCompleted(final String utteranceId) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //UI changes
                                String r = Hidden.getText().toString();
                                if (utteranceId.equalsIgnoreCase("ID1")) {
                                    startVoiceInput();
                                }
                                else if (utteranceId.equalsIgnoreCase("ID2") && x==1 ) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra("EXIT", true);
                                    startActivity(intent);
                                } else if (utteranceId.equalsIgnoreCase("ID3")) {
                                    IntentIntegrator integrator = new IntentIntegrator(CameraActivity.this);
                                    integrator.setPrompt("Scan a QRCode");
                                    integrator.setOrientationLocked(true);
                                    integrator.setCameraId(0);  // Use a specific camera of the device
                                    integrator.setBeepEnabled(true);
                                    integrator.setCaptureActivity(CaptureActivityPortrait.class);
//                                    integrator.setBarcodeImageEnabled(true);
                                    integrator.initiateScan();
                                }



                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_preview);
        Bundle bundle2 = getIntent().getExtras();
        FetchedText = bundle2.getString("id");
        User = bundle2.getString("eid");
        User=User.toLowerCase();
        FetchedText1=FetchedText;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        FetchedText = LoadKeywords(MapFile, FetchedText);
        Hidden = (TextView) findViewById(R.id.Hidden);
        mCamera = Camera.open();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview1);
        preview.addView(mPreview);
        gameBoard = (RelativeLayout) findViewById(R.id.camera_preview);
        for (int i = 1;i<=4;i++){
            btn = new ImageButton(this);
            btn.setId(i);
            if(FetchedText.equals("printer")) {
                if (i == 1)
                    btn.setImageResource(R.drawable.printer_running_out_of_ink);
                if (i == 2)
                    btn.setImageResource(R.drawable.printer_out_of_paper);
                if (i == 3)
                    btn.setImageResource(R.drawable.printer_not_working);
                if (i == 4) {
                    btn.setImageResource(R.mipmap.ic_mic);
                }
            }
            else if(FetchedText.equals("room")) {
                if (i == 1)
                    btn.setImageResource(R.mipmap.ic_booknow);
                if (i == 2)
                    btn.setImageResource(R.mipmap.ic_avail);
                if (i == 4)
                    btn.setImageResource(R.mipmap.ic_mic);
            }

            btn.setLayoutParams(new RelativeLayout.LayoutParams(100, 100));

            btn.setOnClickListener(ClickListener);
            btn.setBackgroundColor(Color.TRANSPARENT);
            btn.setTag(i);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200,200);
            lp.setMargins(10,10,10,10);
            if (i > 0 && i!=4 ) {
                lp.addRule(RelativeLayout.RIGHT_OF, btn.getId() - 1);
            }
            if(i==4)
                lp.addRule(RelativeLayout.CENTER_IN_PARENT);

            btn.setLayoutParams(lp);
            gameBoard.addView(btn);
        }
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {

                speech();

                // TODO Auto-generated method stub
            }
            @Override
            public void onShake(String tilt) {
    if (FetchedText.equals("room")) {
        Toast.makeText(getApplicationContext(),"Contacting please wait",Toast.LENGTH_LONG).show();
        new Room().execute();
    }
    else if (FetchedText.equals("printer")) {
        Toast.makeText(getApplicationContext(),"Contacting please wait",Toast.LENGTH_LONG).show();
        new RaiseTicket().execute();

    }

            }

        });
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent setIntent = new Intent(CameraActivity.this,MainActivity.class);
            startActivity(setIntent);
            // your code
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        Intent setIntent = new Intent(CameraActivity.this,MainActivity.class);
        startActivity(setIntent);
    }
    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(), "Sorry", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (data != null) {
            switch (requestCode) {
                case REQ_CODE_SPEECH_INPUT: {
                    if (resultCode == RESULT_OK && null != data) {
                        result = data
                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        Hidden.setText(result.get(0));
                    } else {
                        Hidden.setText(wrong);
                    }
                    FetchedVoice = Hidden.getText().toString();
                    FetchedVoice=LoadKeywords(MapFile, FetchedVoice);
                    if (FetchedText.equals("room") && FetchedVoice.equals("y")) {
                        new  Room().execute();
                    }
                    else if (FetchedText.equals("printer") && FetchedVoice.equals("y")) {
                       new RaiseTicket().execute();
                    }
                    else
                    {
                        String txt = LoadKeywords(Keywordfile, FetchedVoice);
                        HashMap<String, String> myHashAlarm = new HashMap<>();
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID7");
                        tts.speak(txt, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    }
                    FetchedVoice=null;
                    break;

                }

            }

        }
        else if (scanResult != null && scanResult.getContents()!=null ) {
            String re = scanResult.getContents();
            Log.d("code", re);
            FetchedText=re;
//            Intent intent = new Intent(CameraActivity.this, CameraActivity.class);
//            startActivity(intent);
        }
        else
        {
            finish();
            Toast.makeText(getApplicationContext(),"OOPS!! you didnt capture anything",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        super.onResume();
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }
    public String LoadKeywords(String inFile, String Result) {

        try {

            InputStream stream = getAssets().open(inFile);
            HashMap<String, String> map = new HashMap<String, String>();
            BufferedReader r = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = r.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0];
                    String value = parts[1];
                    map.put(key, value);
                }
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    mainkey = entry.getKey();
                    mainValue = entry.getValue();
                    if (mainkey.equals(Result)) {
                        FinalMap = mainValue;
                        return FinalMap;
                    }
                    else{
                        FinalMap=wrong;
                    }
                }
            }
            r.close();
            stream.close();
            return FinalMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return FinalMap;
    }
    public class RaiseTicket extends CreateIncident{
        @Override
        protected void onPreExecute() {
                   }

        // Call after onPreExec
        protected void onPostExecute(String result) {

            if (Error != null) {
            } else {
                if(FinalMap.equals("Your Ticket has been created. Thank You for using Service Now")) {
                    x=1;
                    if(t==1) {

                        Toast.makeText(getApplicationContext(), FinalMap, Toast.LENGTH_SHORT).show();
                    }
                    else {
                           HashMap<String, String> myHashAlarm = new HashMap<>();
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID2");
                        tts.speak(FinalMap, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    }
                }
                else if(FinalMap.equals("Sorry. Please try again.")) {
                    x=0;
                    if(t==1) {
                        Toast.makeText(getApplicationContext(), FinalMap, Toast.LENGTH_SHORT).show();
                    }
                    else {
                        HashMap<String, String> myHashAlarm = new HashMap<>();
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                        tts.speak(FinalMap, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    }
                }
            }
        }
    }

    public class Room extends BookRoom {
        @Override
        protected void onPreExecute() {
            try {
                // Set Request parameter
                data += "&" + URLEncoder.encode("data", "UTF-8");

            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        // Call after onPreExec
        protected void onPostExecute(String result) {

            if (Error != null) {
            } else {
                if(FinalMap.equals("Your Conference Room has been booked")) {
                    x=1;
                    if(t==1) {
                        Toast.makeText(getApplicationContext(), FinalMap, Toast.LENGTH_LONG).show();
                    }
                    else {
                        HashMap<String, String> myHashAlarm = new HashMap<>();
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID2");
                        tts.speak(FinalMap, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    }
                }
                else if(FinalMap.equals("Sorry. Conference room"+FetchedText+" is already booked.")) {
                    x=0;
                    if(t==1) {
                        Toast.makeText(getApplicationContext(), FinalMap, Toast.LENGTH_LONG).show();
                    }
                    else {
                        HashMap<String, String> myHashAlarm = new HashMap<>();
                        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                        tts.speak(FinalMap, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
                    }
                }

            }
               }
    }
}
