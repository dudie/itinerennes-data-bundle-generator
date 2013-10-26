package fr.itinerennes.bundler.tasks;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.services.GtfsDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import fr.itinerennes.bundler.gtfs.GtfsAdvancedDao;
import fr.itinerennes.bundler.tasks.framework.AbstractTask;
import fr.itinerennes.bundler.tasks.framework.PreExec;

/**
 * @author Jeremie Huchet
 */
@Component
public class FeedInfoTask extends AbstractTask {

    @Value("${program.args.output}")
    private String output;

    @Autowired
    private Gson gson;

    @Autowired
    private GtfsRelationalDao gtfs;

    @Autowired
    private GtfsAdvancedDao xGtfs;

    @PreExec
    public void assertOneFeed() {
        if (gtfs.getAllFeedInfos().size() > 1) {
            throw new IllegalStateException("Multiple Feed informations found");
        }
    }

    @PreExec
    public void assertOneAgency() {
        if (gtfs.getAllFeedInfos().size() > 1) {
            throw new IllegalStateException("Multiple agencies found");
        }
    }

    @Override
    protected void execute() {
        final FeedInfo gFeedInfo = gtfs.getAllFeedInfos().iterator().next();
        final Agency gAgency = gtfs.getAllAgencies().iterator().next();

        final TimeZone tz = xGtfs.getTimeZone(gAgency.getId());

        final fr.dudie.onebusaway.model.FeedInfo infos = new fr.dudie.onebusaway.model.FeedInfo();
        infos.setPublisherName(gFeedInfo.getPublisherName());
        infos.setPublisherUrl(gFeedInfo.getPublisherUrl());
        infos.setLang(new Locale(gFeedInfo.getLang()));
        infos.setStart(gFeedInfo.getStartDate().getAsCalendar(tz).getTime());
        infos.setEnd(gFeedInfo.getEndDate().getAsCalendar(tz).getTime());
        infos.setVersion(gFeedInfo.getVersion());

        final fr.dudie.onebusaway.model.Agency agency = new fr.dudie.onebusaway.model.Agency();
        agency.setId(gAgency.getId());
        agency.setLang(new Locale(gAgency.getLang()));
        agency.setName(gAgency.getName());
        agency.setPhone(gAgency.getPhone());
        agency.setUrl(gAgency.getUrl());
        agency.setTimezone(tz);

        try {
            write(infos, new File(output), "infos.json");
            write(agency, new File(output, "agency"), String.format("%s.json", agency.getId()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void write(Object o, File outputDir, String filename) throws IOException {
        outputDir.mkdirs();
        FileUtils.writeStringToFile(new File(outputDir, filename), gson.toJson(o), "UTF-8");
    }
}
