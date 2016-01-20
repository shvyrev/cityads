package com.webviewapp.loaders;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.webviewapp.ErrorHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
    private static final String TAG = AdLoader.class.getSimpleName();
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
        HttpURLConnection connection = null;
        int statusCode = 0;

        try {
            String requestParams = createRequestParams(params);

            connection = createHttpPostConnection(url, charset);
            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
            outStream.writeBytes(requestParams.toString());
            outStream.flush();
            outStream.close();

            InputStream inStream = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));

            String inputLine;
            StringBuffer result = new StringBuffer();
            while((inputLine = reader.readLine()) != null){
                result.append(inputLine);
            }
            inStream.close();


            return result.toString();
        } catch (IOException e) {

            try {
                statusCode = connection.getResponseCode();

                ByteArrayOutputStream outStream = new ByteArrayOutputStream();

                InputStream es = connection.getErrorStream();
                int ret;
                byte[] buf = new byte[4096];

                while ((ret = es.read(buf)) > 0) {
                    outStream.write(buf, 0, ret);
                }
                es.close();

                ErrorHandler.onHttpError(statusCode, new String(outStream.toByteArray()));

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            ErrorHandler.onLoadingError(e.getStackTrace());

            return null;
        }
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

    private String createRequestParams(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuffer result = new StringBuffer();
        for (String key :
                params.keySet()) {
            if(result.length() != 0)
                result.append("&");
                result.append(key)
                        .append("=")
                        .append(URLEncoder.encode(params.get(key), charset));
        }
        return result.toString();
    }

    @Override
    public void deliverResult(String data) {
        if(data != null && !TextUtils.isEmpty(data)){
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
                ErrorHandler.onJsonError(e.getStackTrace());
            }

            if(TextUtils.equals(status, OK_STATUS))
                super.deliverResult(url);
            else
                super.deliverResult(message);
        }
    }
}
