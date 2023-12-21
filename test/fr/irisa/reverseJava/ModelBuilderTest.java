package fr.irisa.reverseJava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ModelBuilderTest {
    ModelBuilder mb;

    final String ModelBuilderCStyle = """
            @startuml
            skinparam classAttributeIconSize 0
            hide circle
            class ModelBuilder
            ModelBuilder : +String UmlPrelude
            ModelBuilder *--> "knownClasses *" ClassData
            ModelBuilder *--> "config 1" Configuration
            ModelBuilder : +ModelBuilder(String ...)
            ModelBuilder : #void saveText(String, String)
            ModelBuilder : +ClassData addClass(int, int, Class)
            ModelBuilder : +ModelBuilder setOption(String, String)
            ModelBuilder : +ModelBuilder setOptionsForAll(String ...)
            ModelBuilder : +String getDiagramSpec()
            ModelBuilder : +void addExtraInterface(String, String)
            ModelBuilder : +void addFromObjects(int, int, Collection)
            ModelBuilder : +void addJar(int, int, String)
            ModelBuilder : +void generateUMLasTXT(String)
            @enduml
                        """;
    final String ModelBuilderUML = """
            @startuml
            skinparam classAttributeIconSize 0
            hide circle
            class ModelBuilder
            ModelBuilder : +UmlPrelude: String
            ModelBuilder *--> "knownClasses *" ClassData
            ModelBuilder *--> "config 1" Configuration
            ModelBuilder : +ModelBuilder(String ...)
            ModelBuilder : #saveText(String, String)
            ModelBuilder : +addClass(int, int, Class): ClassData
            ModelBuilder : +addExtraInterface(String, String)
            ModelBuilder : +addFromObjects(int, int, Collection)
            ModelBuilder : +addJar(int, int, String)
            ModelBuilder : +generateUMLasTXT(String)
            ModelBuilder : +getDiagramSpec(): String
            ModelBuilder : +setOption(String, String): ModelBuilder
            ModelBuilder : +setOptionsForAll(String ...): ModelBuilder
            @enduml
                                    """;

    @Before
    public void setup() {
        mb = new ModelBuilder("hide circle");
        mb.setOptionsForAll("SHOWPROTECTED");
    }

    @Test
    public void testSetOptionsForAll() {
        mb.setOptionsForAll("C-STYLE-SIGNATURES");
        assertTrue(mb.config.isCStyleSig(mb.getClass()));
    }

    @Test
    public void testCStyleSignatures() {
        mb.setOption("C-STYLE-SIGNATURES", mb.getClass().getSimpleName());
        mb.addClass(1, 1, mb.getClass());
        assertEquals(ModelBuilderCStyle, mb.getDiagramSpec());
    }

    @Test
    public void testAddSingleClass() {
        mb.addClass(1, 1, mb.getClass());
        assertEquals(ModelBuilderUML, mb.getDiagramSpec());
    }

    @Test
    public void testAddFromObjects() {
        mb.addFromObjects(1, 1, List.of(mb, mb));
        assertEquals(ModelBuilderUML, mb.getDiagramSpec());
    }

    @Test
    public void testNoFluent() {
        mb.setOption("NOFLUENT", "setOption,setOptionsForAll");
        mb.addClass(1, 1, mb.getClass());
        String result = ModelBuilderUML.replace(": ModelBuilder", "");
        assertEquals(result, mb.getDiagramSpec());
    }

    @Test
    public void testHide() {
        mb.setOption("HIDE", "UmlPrelude");
        mb.addClass(1, 1, mb.getClass());
        String result = ModelBuilderUML.replace("ModelBuilder : +UmlPrelude: String\n", "");
        assertEquals(result, mb.getDiagramSpec());

    }
}
