package fr.itinerennes.bundler.config;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Jérémie Huchet
 */
public final class PropertyAnnotationsTest {

    @Property
    private String firstname;

    @Property("lastname")
    private String userLastName;

    @Property(file = "alternate.properties")
    private String altFirstname;

    @Property(value = "altLastname", file = "alternate.properties")
    private String altUserLastName;

    @Before
    public void autowireProperties() {

        PropertyAnnotations.process("default.properties", this);
    }

    @Test
    public void checkValues() {

        assertEquals("value_firstname", firstname);
        assertEquals("value_lastname", userLastName);
        assertEquals("value_altFirstname", altFirstname);
        assertEquals("value_altLastname", altUserLastName);
    }
}
