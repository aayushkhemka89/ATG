package ticketgenerate.com.example.verizon.automaticticket;

/**
 * Created by user on 9/2/2017.
 */

import android.util.Log;

public class G {
    public static String TAG="face";
    public static boolean debug = true;

    public static void trace (Object o) {
        if (!debug) return;
        if (o==null) o = "(null)";
        Log.i(TAG, o.toString());
        Log.d("gallery", o.toString());
    }

    public static void trace2 (Object o) {
        if (o==null) o = "(null)";
        Log.i(TAG, o.toString());
    }
}
