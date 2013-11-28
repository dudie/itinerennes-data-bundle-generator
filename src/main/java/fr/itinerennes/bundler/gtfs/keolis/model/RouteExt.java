package fr.itinerennes.bundler.gtfs.keolis.model;

public class RouteExt {

    private String id;

    private boolean accessible;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the accessible
     */
    public boolean isAccessible() {
        return accessible;
    }

    /**
     * @param accessible
     *            the accessible to set
     */
    public void setAccessible(boolean accessible) {
        this.accessible = accessible;
    }

}
