package fr.irisa.reverseJava;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class ModelBuilderTest {
    ModelBuilder mb;

    final String ModelBuilderUML = """
            @startuml
            skinparam classAttributeIconSize 0
            hide circle
            class ModelBuilder
            ModelBuilder : +String UmlPrelude
            ModelBuilder *--> "knownClasses *" ClassData
            ModelBuilder *--> "config 1" Configuration
            ModelBuilder : #void saveText(String, String)
            ModelBuilder : +ClassData addClass(int, int, Class)
            ModelBuilder : +ModelBuilder setOption(String, String)
            ModelBuilder : +ModelBuilder setOptionsForAll(String[])
            ModelBuilder : +String getDiagramSpec()
            ModelBuilder : +void addExtraInterface(String, String)
            ModelBuilder : +void addFromObjects(int, int, Collection)
            ModelBuilder : +void addJar(int, int, String)
            ModelBuilder : +void generateUMLasTXT(String)
            @enduml
                        """;
    final String ModelBuilderNoFluent = """
            @startuml
            skinparam classAttributeIconSize 0
            hide circle
            class ModelBuilder
            ModelBuilder : +String UmlPrelude
            ModelBuilder *--> "knownClasses *" ClassData
            ModelBuilder *--> "config 1" Configuration
            ModelBuilder : #void saveText(String, String)
            ModelBuilder : +ClassData addClass(int, int, Class)
            ModelBuilder : +String getDiagramSpec()
            ModelBuilder : +void addExtraInterface(String, String)
            ModelBuilder : +void addFromObjects(int, int, Collection)
            ModelBuilder : +void addJar(int, int, String)
            ModelBuilder : +void generateUMLasTXT(String)
            ModelBuilder : +void setOption(String, String)
            ModelBuilder : +void setOptionsForAll(String[])
            @enduml
                        """;

    @Before
    public void setup() {
        mb = new ModelBuilder("hide circle");
        mb.setOptionsForAll("SHOWPROTECTED");
    }

    @Test
    public void testAddClass() {
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
        // String result = ModelBuilderUML.replace("+ModelBuilderUML", "+void");
        assertEquals(ModelBuilderNoFluent, mb.getDiagramSpec());
    }


    @Test
    public void testHide() {
        mb.setOption("HIDE", "UmlPrelude");
        mb.addClass(1, 1, mb.getClass());
        String result = ModelBuilderUML.replace("ModelBuilder : +String UmlPrelude\n", "");
        assertEquals(result, mb.getDiagramSpec());

    }
}
