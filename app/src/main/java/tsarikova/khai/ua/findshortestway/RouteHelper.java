package tsarikova.khai.ua.findshortestway;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Created by ira on 31.01.2017.
 */

public class RouteHelper {
    private static final String DIRECTIONS_API_KEY = "AIzaSyBvbmHgy9NoVfoDY5KtsOGM3GorkFuMp8c";

    private static final String LOG_TAG = "MainApp";

    private GoogleMap mMap;
    private Context context;
    private TextView details;
    private Route route = new Route();

    static String LANGUAGE_SPANISH = "ru";
    static String LANGUAGE_ENGLISH = "en";

    public RouteHelper(GoogleMap mMap, Context context, TextView details) {
        this.mMap = mMap;
        Log.i(LOG_TAG, mMap.toString());
        Log.i(LOG_TAG, this.mMap.toString());
        this.context = context;
        this.details = details;
    }

    public Route drawRoute(List<LatLng> points, List<Polyline> oldPolylines, Transport mode, String language) {
        String url = makeURL(points, mode);
        if (oldPolylines != null) {
            removePolylines(oldPolylines);
        }
        new connectAsyncTask(url).execute();
        route.setPoints(points);
        Log.i(LOG_TAG, "distance get " + route.getDistance());
        return route;
    }

    private String makeURL(List<LatLng> points, Transport mode) {
        StringBuilder urlString = new StringBuilder();
        if (mode == null) {
            mode = Transport.DRIVING;
        }
        Log.i(LOG_TAG, "Mode " + mode.name());
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(points.get(0).latitude);
        urlString.append(',');
        urlString.append(points.get(0).longitude);
        urlString.append("&destination=");
        urlString.append(points.get(0).latitude);
        urlString.append(',');
        urlString.append(points.get(0).longitude);

        urlString.append("&alternatives=false");
        urlString.append("&waypoints=");
        urlString.append("optimize:true|");

        for (int i = 1; i < points.size(); i++) {
            urlString.append('|');
            urlString.append(points.get(i).latitude);
            urlString.append(',');
            urlString.append(points.get(i).longitude);
        }

        urlString.append("&mode=" + mode.name().toLowerCase());
        // urlString.append("&key=" + DIRECTIONS_API_KEY);
        return urlString.toString();
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        private String url;

        connectAsyncTask(String urlPass) {
            url = urlPass;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            if (result != null) {
                drawPath(result);

                StringBuilder builder = new StringBuilder();
                builder.append("Distance: " + route.getDistance() + "km; ");
                builder.append("duration: " + route.getDuration() + "min");
                details.setText(builder.toString());
            }
        }
    }

    private void drawPath(String result) {
        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            Log.i(LOG_TAG, "JSON RESULT " + json.toString());
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            List<Polyline> polylines = new ArrayList<>();
            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                polylines.add(mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                        .width(4)
                        .color(Color.RED).geodesic(true)));
            }
            JSONArray arrayLegs = routeArray.getJSONObject(0).getJSONArray("legs");
            int duration = 0;
            double distance = 0;
            for (int i = 0; i < arrayLegs.length(); i++) {
                JSONObject distObject = arrayLegs.getJSONObject(i).getJSONObject("distance");
                distance += distObject.getDouble("value");
                Log.i(LOG_TAG, "distance step " + distance);

                JSONObject durationObject = arrayLegs.getJSONObject(i).getJSONObject("duration");
                duration += durationObject.getInt("value");
                Log.i(LOG_TAG, "duration step " + duration);
            }

            route.setPolylines(polylines);
            route.setDistance(decodeForKm(distance));
            route.setDuration(decodeForMinutes(duration));

            Log.i(LOG_TAG, "distance round " + decodeForKm(distance));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "cannot draw path " + e);
        }
    }

    private void removePolylines(List<Polyline> polylines) {
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines.clear();
    }

    private int decodeForKm(double meters) {
        return (int) Math.ceil(meters / 1000);
    }

    private int decodeForMinutes(int sec) {
        return (int) Math.ceil(sec / 60);
    }
}

