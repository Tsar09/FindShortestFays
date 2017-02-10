package tsarikova.khai.ua.findshortestway;

import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;


/**
 * Created by ira on 31.01.2017.
 */

public class JSONParser {

    private static final String TAG = "JSONParser";

    public String getJSONFromUrl(String url) {
        JSONObject jObj = null;
        String json = "";
        BufferedReader reader = null;
        InputStream is = null;

        // Making HTTP request
        try {
            URL newURL = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) newURL.openConnection();
            is = new BufferedInputStream(urlConnection.getInputStream());

            reader = new BufferedReader(new InputStreamReader(
                    is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        } finally {
            close(reader);
            close(is);
        }
        Log.d(TAG, json);
        return json;
    }

    private void close(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                Log.e(TAG, "Cannot close BufferedReader " + ex);
            }
        }
    }

    private void close(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
                Log.e(TAG, "Cannot close InputStream " + ex);
            }
        }
    }
}
