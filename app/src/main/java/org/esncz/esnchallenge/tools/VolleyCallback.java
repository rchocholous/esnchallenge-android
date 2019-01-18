package org.esncz.esnchallenge.tools;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author chochy
 * Date: 2019-01-18
 */
public interface VolleyCallback<T> {
    void onSuccess(T result) throws JSONException;
    void onError(String result) throws Exception;
}
