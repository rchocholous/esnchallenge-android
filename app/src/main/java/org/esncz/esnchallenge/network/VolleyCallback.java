package org.esncz.esnchallenge.network;

import org.json.JSONException;

/**
 * @author chochy
 * Date: 2019-01-18
 */
public interface VolleyCallback<T> {
    void onSuccess(T result) throws JSONException;
    void onError(String result) throws Exception;
}
