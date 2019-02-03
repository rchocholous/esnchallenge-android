package org.esncz.esnchallenge.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chochy
 * Date: 2019-01-18
 */
public class RequestBuilder<T> {
    private int method;
    private String url;
    private Class<T> clazz;
    private VolleyCallback<T> callback;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Request.Priority priority;
    private String tag;

    public RequestBuilder(int method, String url, Class<T> clazz, VolleyCallback<T> callback) {
        this.method = method;
        this.url = url;
        this.clazz = clazz;
        this.callback = callback;
        this.priority = Request.Priority.NORMAL;
    }

    public RequestBuilder<T> withHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    public RequestBuilder<T> withHeaders(String key, String value) {
        if(this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
        return this;
    }

    public RequestBuilder<T> withParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public RequestBuilder<T> withParams(String key, String value) {
        if(this.params == null) {
            this.params = new HashMap<>();
        }
        this.params.put(key, value);
        return this;
    }


    public RequestBuilder<T> withPriority(Request.Priority priority) {
        this.priority = priority;
        return this;
    }

    public RequestBuilder<T> withTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Request<T> build() {
        GsonRequest<T> request = new GsonRequest<T>(
                this.method,
                this.url,
                this.clazz,
                new Response.Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        try {
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage;

                        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                            errorMessage = "Could not connect to server.";
                        } else if (error instanceof AuthFailureError) {
                            errorMessage = "Invalid credentials.";
                        } else if (error instanceof ServerError) {
                            errorMessage = "Server error.";
                        } else {
                            errorMessage = "Network error.";
                        }

                        try {
                            callback.onError(errorMessage);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                if(headers != null) {
                    return headers;
                }
                return super.getHeaders();
            }
            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                if(params != null) {
                    return params;
                }
                return super.getParams();
            }

        };
        request.setPriority(priority);
        if(tag != null) {
            request.setTag(tag);
        }

        return request;
    }

}
