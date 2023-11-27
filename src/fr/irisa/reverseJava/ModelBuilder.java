package fr.irisa.reverseJava;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModelBuilder {
    public String UmlPrelude = """
            @startuml
            skinparam classAttributeIconSize 0
            """;
    public Map<String, ClassData> knownClasses = new LinkedHashMap<>();
    protected Configuration config = new Configuration();

    public ModelBuilder(String... globalOptions) {
        for (String option : globalOptions) {
            UmlPrelude += option + '\n';
        }
    }

    public ModelBuilder setOptionsForAll(String... optionNames) {
        for (String optName : optionNames) {
            config.setOption(optName, Configuration.ALL); // Apply everywhere
        }
        return this;
    }

    public ModelBuilder setOption(String name, String value) {
        config.setOption(name, value);
        return this;
    }

    public void addExtraInterface(String name, String content) {
        knownClasses.put(name, new ClassData(content));
    }

    private void followFields(int depth, int width, ClassData cd) {
        if (width == 0)
            return;
        for (Field f : cd.getFields()) {
            Class<?> type = f.getType();
            if (!(type.isPrimitive() || type.getSimpleName().equals("String")
                    || type.getSimpleName().equals("Object"))) {
                if (type.isArray()) {
                    Class<?> at = type.componentType();
                    if (!(at.isPrimitive() || at.getSimpleName().equals("String")))
                        addClass(depth, width - 1, type.componentType());
                } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                    addClass(depth, width - 1, cd.getGenericParameter(f));
                    // TODO maybe include going through content of collection?
                } else
                    addClass(depth, width - 1, type);
            }
        }
    }

    public void addJar(int depth, int width, String jarFileName) {
        List<Class<?>> classList = new LinkedList<>();
        try {
            JarFile jarFile = new JarFile(jarFileName);
            Enumeration<JarEntry> entries = jarFile.entries();

            URL[] urls = { new URL("jar:file:" + jarFileName + "!/") };
            URLClassLoader loader = URLClassLoader.newInstance(urls);

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class") && !name.contains("$")) {
                    String className = name.replaceAll("/", "\\.")
                            .replace(".class", "");
                    try {
                        Class<?> loadedClass = loader.loadClass(className);
                        System.out.println("Loaded Class: " + loadedClass.getName());
                        classList.add(loadedClass);
                    } catch (ClassNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        classList.forEach(c -> addClass(depth, width, c));
    }

    public ClassData addClass(int depth, int width, Class<?> base) {
        if (depth == 0 || base == null || base.getPackageName().startsWith("java.lang"))
            return null;
        if (!config.isVisible(base)) {
            return null;
        }
        String name = base.getSimpleName();
        ClassData data = knownClasses.get(name);
        if (data == null) {
            data = new ClassData(base, config);
            knownClasses.put(name, data);
        }
        if (!data.isExplored()) {
            data.setExplored(true);
            for (Class<?> i : base.getInterfaces()) {
                addClass(depth - 1, width, i);
            }
            ClassData superdata = addClass(depth - 1, width, base.getSuperclass()); // follow up
            if (superdata != null)
                data.setSuperData(superdata);
            followFields(depth, width - 1, data); // follow in width
        }
        return data;
    }

    public void addFromObjects(int depth, int width, Collection<?> objects) {
        for (Object o : objects) {
            addClass(depth, width, o.getClass());
        }
    }

    // public String configureUML() {
    // String result = "";
    // for (String option : options.keySet()) {
    // result += "!$" + option + " = " + options.get(option) + "\n";
    // }
    // return result;
    // }

    public String getDiagramSpec() {
        StringBuilder sb = new StringBuilder(UmlPrelude);
        for (ClassData cd : knownClasses.values()) {
            cd.appendIn(sb);
        }
        sb.append("@enduml\n");
        return sb.toString();
    }

    protected void saveText(String filename, String text) {
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), StandardCharsets.UTF_8));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateUMLasTXT(String filename) {
        String model = getDiagramSpec();
        saveText(filename, model);
    }

}
