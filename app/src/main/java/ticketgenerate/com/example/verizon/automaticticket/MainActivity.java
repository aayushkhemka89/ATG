package ticketgenerate.com.example.verizon.automaticticket;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Dialog;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.images.Size;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public final class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "FaceTracker";
    static Boolean isQuit = false;
    private CameraSource mCameraSource = null;
    private Button mButton;
    private Bitmap mBitmapImage;
    public Bitmap croppedBitmap;
    private int mRotationAngle = -90;  // TODO: validate, & make it configurable
    private int mImageWidth = 500;   // width of file saved on the disk
    private ImageView imageView;
    private CameraSourcePreview mPreview;
    Boolean FaceOutOfBound=false;
    private GraphicOverlay mGraphicOverlay;
    private String mTempFile = "TempImage.jpg";
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private String pictureDirectory;
    public String encoded;
    public TextToSpeech t1;
    //*******************************************************************
    private static char FLOW_CONTROL = '*';
    private static final String MODE_IDENTIFY = "identify";
    private Kairos mKairos;
    private String mKairosMode;
    private String mSubjectName = "Guest";
    private String mGalleryName = "MyGallery5";
    private ArrayList mRegistrationResult;
    private ArrayList mIdentificationResult;
    private Random mRandGenerator = new Random();
//***********************************************************************

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
//        createCameraSource();
        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
        }
        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


//         Check for the camera permission before accessing the camera.  If the
//         permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
//            createCameraSource();
        }

        //  mButton=(Button) findViewById(R.id.button2);
        //  mButton.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        mKairos = new Kairos();

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {

            @Override
            public void onInit(int status) {

                t1.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {

                    @Override
                    public void onUtteranceCompleted(final String utteranceId) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                //UI changes

                                if (utteranceId.equalsIgnoreCase("ID1")) {

                                    IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
                                    integrator.setPrompt("Scan a QRCode");
                                    integrator.setOrientationLocked(true);
                                    integrator.setCameraId(0);  // Use a specific camera of the device
                                    integrator.setBeepEnabled(true);
                                    integrator.setCaptureActivity(CaptureActivityPortrait.class);
//                                    integrator.setBarcodeImageEnabled(true);
                                    integrator.initiateScan();

                                } else if (utteranceId.equalsIgnoreCase("ID2")) {
                                    finish();
                                } else if (utteranceId.equalsIgnoreCase("ID3")) {
                                    createCameraSource();
                                    startCameraSource();
                                }


                            }
                        });
                    }
                });

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        System.out.println("Scanned qr_code");
        if (scanResult != null && scanResult.getContents() != null) {
            String re = scanResult.getContents();
            Log.d("code", re);
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            Bundle bundle1 = new Bundle();
            bundle1.putString("id", re);
            bundle1.putString("eid", mSubjectName);
            intent.putExtras(bundle1);
            startActivity(intent);
        } else {
            finish();
            Toast.makeText(getApplicationContext(), "OOPS!! you didnt capture anything", Toast.LENGTH_SHORT).show();
        }
        // else continue with any other code you need in the method

    }


    @Override
    public void onClick(View arg0) {
        //  takepicture();
    }

    public void takepicture(final int x, final int y, final int width, final int height) {
//    public void takepicture(){

        System.out.println("x = " + x + "\ny = " + y + "\nwidth =  " + width + "\nheight = " + height);

        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                mCameraSource.stop();
                Log.d(TAG, "onPictureTaken - jpeg");


                mBitmapImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                System.out.println(mBitmapImage.getHeight() + " " + mBitmapImage.getWidth());
//                Log.d(TAG, (x+width) + " " + (y+height));
                System.out.println((2 * y + height) + " " + (2 * x + width));
                int right = x + width;
                int bottom = y + height;
                if (x + right > mBitmapImage.getWidth()) {
                    right = mBitmapImage.getWidth() - x;
                    FaceOutOfBound = true;
                }
                if (y + bottom > mBitmapImage.getHeight()) {
                    bottom = mBitmapImage.getHeight() - y;
                    FaceOutOfBound = true;
                }

                croppedBitmap = Bitmap.createBitmap(mBitmapImage, x, y, right, bottom);


                createDirectoryAndSaveFile(croppedBitmap, "crop.jpeg");
                createDirectoryAndSaveFile(mBitmapImage, "full.jpeg");
                process();
                //*****************************************************************
                mKairosMode = MODE_IDENTIFY;
                new KairosDemon().execute(mKairosMode);
                HashMap<String, String> myHashAlarm2 = new HashMap<>();
                myHashAlarm2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID8");
                t1.speak("Picture taken. Identifying. Please wait", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);
                //cameraHelperCallback();

            }
        });
    }

    //**********************************************
    //saving cropped image
    //***********************************************
    private void createDirectoryAndSaveFile(Bitmap imageToSave, String fileName) {


//        Toast.makeText(MainActivity.this,"Image saving",Toast.LENGTH_SHORT).show();
        File direct = new File(Environment.getExternalStorageDirectory() + "/storage/emulated/0/atg");

        if (!direct.exists()) {
            File wallpaperDirectory = new File("/storage/emulated/0/atg");
            wallpaperDirectory.mkdirs();
        }

        File file = new File(new File("/storage/emulated/0/atg/"), fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Toast.makeText(MainActivity.this,"Image saved",Toast.LENGTH_SHORT).show();
    }

    //********************************************************


//*******************************************************

    // this will be called by demon
    public void identify() {
        Log.d(TAG, "Uploading image to identify...");
        try {
            //Log.d(TAG,"upload is:"+mGalleryName+","+encoded);
            this.mIdentificationResult = mKairos.identify(encoded, mGalleryName);
            Log.d(TAG, "Result is:" + this.mIdentificationResult);
            G.trace("IDentification result: " + mIdentificationResult);
            G.trace("Response code: " + mKairos.getResponseCode());

        } catch (Exception e) { // this will not be called; Kairos eats up the exception !
            e.printStackTrace();
            // Note: Toast in the worker thread will crash the app
            //Toast.makeText(this, "Error: could not upload image", Toast.LENGTH_LONG).show();
        }
    }

    private class KairosDemon extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            G.trace("In Pre-execute..");
        }

        @Override
        protected String doInBackground(String... args) {
            G.trace("---AsyncTask:Identifying---");
            //G.trace(mKairos.getIPAddress());
            if (args[0].equals(MODE_IDENTIFY))
                identify();
           /* else
                enroll();*/
            return args[0];
        }

        @Override
        protected void onPostExecute(String command) {
            G.trace("In Post-execute: " + command);
            int respCode = mKairos.getResponseCode();
            G.trace("HTTP status: " + respCode);
            if (respCode != Kairos.HTTP_OK) {
                HashMap<String, String> myHashAlarm2 = new HashMap<>();
                myHashAlarm2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                t1.speak("Sorry. There was a network error. May be, you should try again.", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);
                return;
            }
            if (command.equals(MODE_IDENTIFY))
                postIdentify();
            else
                postEnroll();
        }

        public void postIdentify() {
            String resp = (String) mIdentificationResult.get(0);
            G.trace("In Post-identity:" + resp);
            if (resp.startsWith("Error") || resp.startsWith("Identity")) {
                String errorCode = (String) mIdentificationResult.get(1);
                G.trace("Error" + errorCode);
                processError(errorCode);
                return;
            } else {
                mSubjectName = resp;
                Toast.makeText(MainActivity.this, "Welcome, " + mSubjectName, Toast.LENGTH_SHORT).show();
                HashMap<String, String> myHashAlarm = new HashMap<>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID1");
                t1.speak("Hello, " + mSubjectName + ".", TextToSpeech.QUEUE_FLUSH, myHashAlarm);
            }

        }


        public void postEnroll() {
            String resp = (String) mRegistrationResult.get(0);
            G.trace(resp);
            if (resp.startsWith("Error")) {
                String errorCode = (String) mIdentificationResult.get(1);
                G.trace(errorCode);
                processError(errorCode);
                return;
            }
            Toast.makeText(MainActivity.this, mSubjectName + " registered", Toast.LENGTH_SHORT).show();
            t1.speak("Congratulations ! You are successfully registered.", TextToSpeech.QUEUE_FLUSH, null);
        }

        // TODO: convert all these dangerous strings to static constants !
        public void processError(String errorCode) {
            if (errorCode.equals("Network")) {
                Toast.makeText(MainActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                HashMap<String, String> myHashAlarm2 = new HashMap<>();
                myHashAlarm2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                t1.speak("I think there is a network error. Try again after fixing it.", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);

            }
            if (errorCode.equals("Image")) {
                Toast.makeText(MainActivity.this, "Picture Error", Toast.LENGTH_SHORT).show();
                HashMap<String, String> myHashAlarm2 = new HashMap<>();
                myHashAlarm2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                t1.speak("Sorry. There is some problem in processing your picture. Can you please try again ?", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);

            }
            if (errorCode.equals("SpeechFailed")) {
                Toast.makeText(MainActivity.this, "Sorry for the inconvenience", Toast.LENGTH_SHORT).show();
                HashMap<String, String> myHashAlarm = new HashMap<>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                t1.speak("Please try gain.", TextToSpeech.QUEUE_FLUSH, myHashAlarm);

            }
            if (errorCode.equals("FAILED")) {
                Toast.makeText(MainActivity.this, "Welcome, Guest!", Toast.LENGTH_SHORT).show();
                HashMap<String, String> myHashAlarm = new HashMap<>();
                myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID2");
                t1.speak("It seems you have not register yourself, please register and try again.", TextToSpeech.QUEUE_FLUSH, myHashAlarm);
            }
            if (errorCode.equals("No face")) {

                HashMap<String, String> myHashAlarm2 = new HashMap<>();
                myHashAlarm2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID3");
                if(FaceOutOfBound==true) {
                    Toast.makeText(MainActivity.this, "Out of Frame", Toast.LENGTH_SHORT).show();
                    t1.speak("Your face might be out of frame. Please try again.", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);
                }
                else {
                    Toast.makeText(MainActivity.this, "No face detected", Toast.LENGTH_SHORT).show();
                    t1.speak("I did not find any face in the picture. Some times, this happens if there is not enough light.Please try again", TextToSpeech.QUEUE_FLUSH, myHashAlarm2);
                }
            }
        }
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //****************************************************
    public void cameraHelperCallback() {
        //mCameraHelper.saveImage(getTempFileName()); // optional
        process();
        // saveImage(getPictureFileName()); // reduced, b&w image
        //upload the image to Kairos and identify/ register
    }

    public void process() {
        resize();
        //  rotate();
        toGrayscale();
        imageView.setImageBitmap(mBitmapImage);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        mBitmapImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);

        createDirectoryAndSaveFile(croppedBitmap, "processed.jpeg");

        byte[] byteArray = byteArrayOutputStream.toByteArray();
        encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        //  Log.d(TAG, "Base64:"+encoded);
        // TODO: keep only the face rectangle
    }

    private void resize() {
//        int w = mBitmapImage.getWidth();
//        int h = mBitmapImage.getHeight();
        int width = mImageWidth;
        int w = croppedBitmap.getWidth();
        int h = croppedBitmap.getHeight();
        int height = Math.round(((float) width) / ((float) w) * h);
        if (width > w || height > h || height < 10)
            Log.d(TAG, "Picture is too small; no resizing done");
        else
//            mBitmapImage = Bitmap.createScaledBitmap(mBitmapImage, width, height, false);
            croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, width, height, false);
    }

    private void rotate() {
        int width = mBitmapImage.getWidth();
        int height = mBitmapImage.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(mRotationAngle);
        mBitmapImage = Bitmap.createBitmap(mBitmapImage, 0, 0, width, height, matrix, true);
    }

    private void toGrayscale() {
//        int width = mBitmapImage.getWidth();
//        int height = mBitmapImage.getHeight();
        int width = croppedBitmap.getWidth();
        int height = croppedBitmap.getHeight();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
//        c.drawBitmap(mBitmapImage, 0, 0, paint);
//        mBitmapImage = bmpGrayscale;
        c.drawBitmap(croppedBitmap, 0, 0, paint);
        croppedBitmap = bmpGrayscale;
    }

    private void createCameraSource() {

        Context context = getApplicationContext();

        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();

    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        if (t1 != null) {
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
        mPreview.stop();
    }

    @Override
    public void onStop() {
        if (t1 != null) {
            t1.stop();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }

    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);

            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
            int[] points = new int[4];

            points = mFaceGraphic.coordinates();
//            System.out.println("printing coordinates.");
//            System.out.println(points[0] + " " + points[1] + " " + points[2] + " "  + points[3]);
            System.out.println("face coordinates");
            System.out.println(face.getPosition().x + " " + face.getPosition().y + " " + face.getWidth() + " " + face.getHeight());

            int width1 = 0;
            Size size = null;
            size = mCameraSource.getPreviewSize();
            if (size != null) {
                width1 = size.getWidth();
                int face1 = (int) face.getWidth();
                if (face1 > width1 / 2 && points[0] > 0 && points[1] > 0)
//                    takepicture();
//                    takepicture((int)face.getPosition().x , (int)face.getPosition().y, (int)face.getWidth(), (int)face.getHeight() );
                    takepicture(points[0], points[1], points[2], points[3]);
//                takepicture();
            }
        }

        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }

}
