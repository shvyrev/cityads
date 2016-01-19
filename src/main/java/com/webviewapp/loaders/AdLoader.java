package com.webviewapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by s.shvyrev on 19.01.16.
 */
public class AdLoader extends AsyncTaskLoader<String> {
    private static final String SIM_ID = "id";
    private static final String DEFAULT_IMSI = "";
    private String id;

    public static final String url = "http://www.505.rs/adviator/index.php";
    public static final String charset = "UTF-8";
    public static final int READ_TIMEOUT = 10000;
    public static final int CONNECT_TIMEOUT = 10000;

    public static final String OK_STATUS = "OK";

    public AdLoader(Context context, Bundle args) {
        super(context);
        if(args != null)
            id = args.getString(SIM_ID);
        if(TextUtils.isEmpty(id))
            id = DEFAULT_IMSI;
    }

    @Override
    public String loadInBackground() {
        HashMap<String, String> params = new HashMap<>();
        params.put("id", id);

        String requestParams = createRequestParams(params);
        HttpURLConnection connection = null;

        try {
            connection = createHttpPostConnection(url, charset);
        } catch (IOException e) {
            e.printStackTrace();
        }

        DataOutputStream outStream = null;
        try {
            outStream = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outStream.writeBytes(requestParams.toString());
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        InputStream inStream = null;
        try {
            inStream = new BufferedInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

        String inputLine;
        StringBuffer result = new StringBuffer();

        try {
            while((inputLine = reader.readLine()) != null){
                result.append(inputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    private HttpURLConnection createHttpPostConnection(String url, String charset) throws IOException {
        URL link = new URL(url);
        HttpURLConnection result = (HttpURLConnection) link.openConnection();
        result.setDoOutput(true);
        result.setRequestMethod("POST");
        result.setRequestProperty("Accept-Charset", charset);
        result.setReadTimeout(READ_TIMEOUT);
        result.setConnectTimeout(CONNECT_TIMEOUT);
        return result;
    }

    private String createRequestParams(HashMap<String, String> params) {
        StringBuffer result = new StringBuffer();
        for (String key :
                params.keySet()) {
            if(result.length() != 0)
                result.append("&");
            try {
                result.append(key)
                        .append("=")
                        .append(URLEncoder.encode(params.get(key), charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }

    @Override
    public void deliverResult(String data) {
        JSONObject json = null;
        String status = "";
        String message = "";
        String url = "";

        try {
            json = new JSONObject(data);
            status = json.getString("status");
            message = json.getString("message");
            url = json.getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(TextUtils.equals(status, OK_STATUS))
            super.deliverResult(url);
        else
            super.deliverResult(message);
    }
}
