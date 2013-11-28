package fr.itinerennes.bundler.gtfs.keolis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.serialization.GtfsReader;
import org.onebusaway.gtfs.services.GtfsDao;

import fr.itinerennes.bundler.gtfs.keolis.model.RouteExt;

/**
 * @author Jeremie Huchet
 */
public class KeolisExtensionsReader extends GtfsReader {

    private final GtfsDao gtfsDao;
    
    private final List<Route> accessibleRoutes;

    public KeolisExtensionsReader(final KeolisGtfsDaoImpl targetDao) throws IOException {
        super();
        this.gtfsDao = targetDao;
        this.accessibleRoutes = new ArrayList<Route>();
        targetDao.setAllAccessibleRoutes(accessibleRoutes);
        setEntityStore(targetDao);

        getEntityClasses().add(RouteExt.class);
        setEntitySchemaFactory(new KeolisExtensionsEntitySchemaFactory());
        addEntityHandler(new KeolisExtensionsEntityHandler());
    }

    private class KeolisExtensionsEntityHandler implements EntityHandler {

        @Override
        public void handleEntity(final Object bean) {
            if (bean instanceof RouteExt) {
                final RouteExt routeExt = (RouteExt) bean;
                if (routeExt.isAccessible()) {
                    final String agencyId = getAgencyForEntity(Route.class, routeExt.getId());
                    accessibleRoutes.add(gtfsDao.getRouteForId(new AgencyAndId(agencyId, routeExt.getId())));
                }
            }
        }

    }
}
