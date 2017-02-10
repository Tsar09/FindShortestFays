package tsarikova.khai.ua.findshortestway;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ira on 06.02.2017.
 */

public class Route {
    private List<Polyline> polylines;
    private List<LatLng> points;
    private int distance;
    private int duration;

    public List<Polyline> getPolylines() {
        if(polylines == null){
            polylines = new ArrayList<>();
        }
        return polylines;
    }

    public void setPolylines(List<Polyline> polylines) {
        this.polylines = polylines;
    }

    public List<LatLng> getPoints() {
        if(points == null){
            points = new ArrayList<>();
        }
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
