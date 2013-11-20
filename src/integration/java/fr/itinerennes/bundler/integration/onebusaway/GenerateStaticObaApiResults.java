package fr.itinerennes.bundler.integration.onebusaway;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsDao;

import com.google.gson.Gson;

import fr.dudie.onebusaway.client.JsonOneBusAwayClient;
import fr.dudie.onebusaway.client.JsonOneBusAwayClient.ClientListener;
import fr.dudie.onebusaway.gson.OneBusAwayGsonFactory;
import fr.dudie.onebusaway.model.StopSchedule;
import fr.dudie.onebusaway.model.TripSchedule;
import fr.itinerennes.bundler.gtfs.GtfsException;
import fr.itinerennes.bundler.gtfs.GtfsUtils;

public class GenerateStaticObaApiResults {

    private static JsonOneBusAwayClient oba;

    private static Gson gson;

    public static void main(String[] args) throws IOException, GtfsException {
        final String url = args[0];
        final String key = args[1];
        final String gtfsFile = args[2];
        final String out = args[3].replaceAll("/$", "");

        final Map<String, String> agencyMapping = new HashMap<String, String>();
        agencyMapping.put("1", "2");
        final GtfsDao gtfs = GtfsUtils.load(new File(gtfsFile), agencyMapping);

        final CloneClientListener listener = new CloneClientListener();
        oba = new JsonOneBusAwayClient(new DefaultHttpClient(), url, key);
        oba.addListener(listener);
        gson = OneBusAwayGsonFactory.newInstance(true);

        final Calendar end = Calendar.getInstance();
        end.set(2013, 11, 22, 0, 0);

        final Calendar start = Calendar.getInstance();
        start.set(2013, 10, 4, 0, 0);

        final Calendar current = Calendar.getInstance();
        current.setTime(start.getTime());

        while (current.before(end) || current.equals(end)) {
            System.out.println(current.getTime());
            for (final Stop stop : gtfs.getAllStops()) {
                final String stopId = stop.getId().toString();
                final String dateDir = String.format("%04d/%02d/%02d", current.get(Calendar.YEAR), current.get(Calendar.MONTH) + 1,
                        current.get(Calendar.DAY_OF_MONTH));
                final String methodDir = String.format("%s/schedule-for-stop", out);

                final File outDir = new File(String.format("%s/%s", methodDir, dateDir));
                outDir.mkdirs();
                final File f = new File(outDir, String.format("%s.json", stopId));

                listener.setOutput(new File(String.format("%s/schedule-for-stop.original/%s/%s.json", out, dateDir, stopId)));

                final StopSchedule ss = oba.getScheduleForStop(stopId, current.getTime());
                final String json = gson.toJson(ss);

                final Writer w = new PrintWriter(f);
                w.write(json);
                w.close();
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        }

        final File outDir = new File(String.format("%s/trip-details", out));
        outDir.mkdirs();
        for (final Trip trip : gtfs.getAllTrips()) {
            final String tripId = trip.getId().toString();
            final File f = new File(outDir, String.format("%s.json", tripId));

            listener.setOutput(new File(String.format("%s/trip-details.original/%s.json", out, tripId)));

            final TripSchedule ts = oba.getTripDetails(tripId);
            final String json = gson.toJson(ts);

            final Writer w = new PrintWriter(f);
            w.write(json);
            w.close();
        }
    }

    private static class CloneClientListener implements ClientListener {

        private File output;

        public void setOutput(File output) {
            this.output = output;
        }

        @Override
        public void onRequest(final String url) {
            try {
                FileUtils.copyURLToFile(new URL(url), output);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
