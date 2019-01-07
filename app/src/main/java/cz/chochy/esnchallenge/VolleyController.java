package cz.chochy.esnchallenge;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * @author chochy
 * Date: 2019-01-07
 */
public class VolleyController {
    private static VolleyController instance;
    private RequestQueue queue;
    private static Context mCtx;

    public static synchronized VolleyController getInstance(Context context) {
        if(instance == null) {
            instance = new VolleyController(context);
        }
        return instance;
    }

    private VolleyController() {
    }

    private VolleyController(Context context) {
        mCtx = context;
        queue = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (queue == null) {
            // getApplicationContext() is key. It should not be activity context,
            // or else RequestQueue won't last for the lifetime of your app
            queue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return queue;
    }

    public  void addToRequestQueue(Request req) {
        getRequestQueue().add(req);
    }
}
