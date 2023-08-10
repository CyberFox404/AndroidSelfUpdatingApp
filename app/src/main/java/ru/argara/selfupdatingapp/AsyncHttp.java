package ru.argara.selfupdatingapp;
import android.location.GnssAntennaInfo;
import android.util.Log;

import com.loopj.android.http.*;

import cz.msebera.android.httpclient.Header;

public class AsyncHttp {
//public static final int F_INET_CHECK = -125;

public static int ID = 0;


    private static Listener listener;

    public static int getID() {
        int _id = ID;
        ID += 1;
        return _id;
    }
    public int checkInet() {
        return myget("https://ya.ru/");
        //return F_INET_CHECK;
        //return getID();
    }
    //private static Listener listener;

    public interface Listener {
        public void ponSuccess(int statusCode, Header[] headers, byte[] response, int ID);
        public void ponFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e, int ID);


    }


    public void setListener(Listener listener){
        AsyncHttp.listener = listener;
    }





        private static final String BASE_URL = "https://api.twitter.com/1/";

        private static final AsyncHttpClient client = new AsyncHttpClient();

    public int myget(String url){

        int iID = getID();

client.get(url, null, new AsyncHttpResponseHandler() {

    @Override
    public void onStart() {
        // called before request is started
        //Some debugging code here
    }

    @Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
        listener.ponSuccess(statusCode, headers, responseBody, iID);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
        listener.ponFailure(statusCode, headers, responseBody, error, iID);
    }

    @Override
    public void onRetry(int retryNo) {
        //Some debugging code here-------

    }

    }
);

return  iID;
}
        public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.get(url, params, responseHandler);
        }

    public void get(String url) {
        client.get(url, null, null);
    }

        public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
            client.post(url, params, responseHandler);
        }

        private String getAbsoluteUrl(String relativeUrl) {
            return BASE_URL + relativeUrl;
        }



}
