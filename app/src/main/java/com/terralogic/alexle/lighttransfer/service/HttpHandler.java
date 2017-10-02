package com.terralogic.alexle.lighttransfer.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by alex.le on 26-Sep-17.
 */

public class HttpHandler {
    private String reqUrl;
    private HashMap<String, String> params = new LinkedHashMap<>();
    private HashMap<String, String> headers = new LinkedHashMap<>();

    public HttpHandler(String url) {
        reqUrl = url;
    }

    public String getRequestUrl() {
        return reqUrl;
    }

    public void setRequestUrl(String reqUrl) {
        this.reqUrl = reqUrl;
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * GET request to the server
     * @return Response body
     */
    public String get(){
        String response = null;
        HttpsURLConnection conn = null;
        try {
            String data = getDataString(params);
            String getUrl = new String().concat(reqUrl).concat("?").concat(data);

            URL url = new URL(getUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(120000);
            if (headers.size() != 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.connect();

            InputStream in = null;
            int statusCode = conn.getResponseCode();
            if (statusCode >= 400) {
                in = new BufferedInputStream(conn.getErrorStream());
            } else {
                in = new BufferedInputStream(conn.getInputStream());
            }
            response = convertStreamToString(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }
    /**
     * POST request to the server
     * @return Response body
     */
    public String post(){
        String response = null;
        HttpURLConnection conn = null;
        try {
            String data = getDataString(params);

            URL url = new URL(reqUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            if (headers.size() != 0) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    conn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            conn.connect();

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
            writer.write(data);
            writer.flush();

            InputStream in = null;
            int statusCode = conn.getResponseCode();
            if (statusCode >= 400) {
                in = new BufferedInputStream(conn.getErrorStream());
            } else {
                in = new BufferedInputStream(conn.getInputStream());
            }
            response = convertStreamToString(in);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        return response;
    }


    public static String getMessage(String response) {
        if (response != null) {
            try {
                JSONObject json = new JSONObject(response);
                return json.getString("message");
            } catch (JSONException ex) {
                Log.e("HttpHandler", "JSON mapping error!");
            }
        }
        return "Connect failed!";
    }

    /**
     * Check is the request is successful
     * @param response Response body returned by the server
     */
    public static boolean isSuccessful(String response) {
        if (response == null) {
            return false;
        }
        String message = getMessage(response);
        if (message.equals("Login successful")
                || message.equals("Successful")
                || message.equals("Logout successful")
                || message.equals("Update successful !!!")
                || message.equals("Add device successfully !!!")) {
            return true;
        }
        return false;
    }

    /**
     * Convert params HashMap to data string which is used for sending request
     */
    private String getDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }

    /**
     * Read data from input stream
     */
    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
