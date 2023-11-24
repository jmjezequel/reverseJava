package fr.irisa.reverseJava;

import org.junit.Before;
import org.junit.Test;

public class UmlViewerTest {
    UmlViewer mb;

    @Before
    public void setup() {
        mb = new UmlViewer("hide circle");
        mb.setOptionsForAll("SHOWPROTECTED");
    }

    @Test
    public void testGenerateUMLasPNG() {
        mb.addClass(2, 2, mb.getClass());
        mb.generateUMLasPNG("test/testUmlViewer.png");
    }

    @Test
    public void testViewPNG() {
        testGenerateUMLasPNG();
        mb.viewPNG("test/testUmlViewer.png");
    }
}
