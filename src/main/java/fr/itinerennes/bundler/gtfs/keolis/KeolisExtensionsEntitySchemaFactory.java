package fr.itinerennes.bundler.gtfs.keolis;

import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.DefaultFieldMapping;
import org.onebusaway.csv_entities.schema.EntitySchema;

import fr.itinerennes.bundler.gtfs.keolis.model.RouteExt;

/**
 * @author Jeremie Huchet
 */
public class KeolisExtensionsEntitySchemaFactory extends DefaultEntitySchemaFactory {

    private static final String ROUTES_EXT = "routes_extensions.txt";

    @Override
    public EntitySchema getSchema(final Class<?> entityClass) {
        final EntitySchema schema;
        if (RouteExt.class.equals(entityClass)) {
            schema = new EntitySchema(RouteExt.class, ROUTES_EXT, true);
            schema.addField(new DefaultFieldMapping(RouteExt.class, "route_id", "id", String.class, true));
            schema.addField(new DefaultFieldMapping(RouteExt.class, "route_accessible", "accessible", Boolean.class, true));

        } else {
            schema = super.getSchema(entityClass);
        }
        return schema;
    }

}
