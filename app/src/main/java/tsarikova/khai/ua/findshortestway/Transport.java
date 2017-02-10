package tsarikova.khai.ua.findshortestway;

/**
 * Created by ira on 04.02.2017.
 */

public enum Transport {

    DRIVING, WALKING, BIKE, TRANSIT;

    public static CharSequence[] names() {
        Transport[] transports = values();
        CharSequence[] names = new CharSequence[transports.length];

        for (int i = 0; i < transports.length; i++) {
            names[i] = transports[i].name();
        }
        return names;
    }
}
