package fr.itinerennes.bundler.gtfs.dao;

import java.util.List;

import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.services.GtfsDao;

public interface KeolisGtfsDao extends GtfsDao {

    List<Route> getAllAccessibleRoutes();
}
