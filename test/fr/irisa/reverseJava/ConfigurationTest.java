package fr.irisa.reverseJava;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {
    Configuration config;

    @Before
    public void setup() {
        config = new Configuration();
    }

    @Test
    public void testIsVisible() {

    }

    @Test
    public void testSetOption() {
        config.setOption("FLAT",config.getClass().getSimpleName());
        assertTrue(config.isFlat(config.getClass()));
        config.setOption("C-STYLE-SIGNATURES",config.getClass().getSimpleName());
        assertTrue(config.isCStyleSig(config.getClass()));
    }

    @Test
    public void testSetAllOption() {
        config.setOption("FLAT",Configuration.ALL);
        assertTrue(config.isFlat(config.getClass()));
    }
}
