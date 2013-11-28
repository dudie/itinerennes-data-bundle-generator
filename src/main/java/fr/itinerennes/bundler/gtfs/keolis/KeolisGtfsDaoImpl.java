package fr.itinerennes.bundler.gtfs.keolis;

import java.util.Collections;
import java.util.List;

import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Route;

import fr.itinerennes.bundler.gtfs.dao.KeolisGtfsDao;

public class KeolisGtfsDaoImpl extends GtfsRelationalDaoImpl implements KeolisGtfsDao {

    private List<Route> accessibleRoutes;

    @Override
    public List<Route> getAllAccessibleRoutes() {
        return Collections.unmodifiableList(accessibleRoutes);
    }
    
    public void setAllAccessibleRoutes(final List<Route> accessibleRoutes) {
        this.accessibleRoutes = accessibleRoutes;
    }
}
