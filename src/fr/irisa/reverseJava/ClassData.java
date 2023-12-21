package fr.irisa.reverseJava;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ClassData {

    String plantUML = null;
    private Class<?> classe = null;
    protected List<Field> fields = null;
    protected List<Constructor<?>> constructors = null;
    protected List<Method> methods = null;
    private ClassData superdata = null;
    String name = null;
    private boolean isExplored = false;
    private Configuration config;

    public ClassData(Class<?> base, Configuration config) {
        classe = base;
        this.config = config;
        name = base.getSimpleName();
    }

    public ClassData(String uml) {
        plantUML = uml;
    }

    public boolean isExplored() {
        return isExplored;
    }

    public void setExplored(boolean isExplored) {
        this.isExplored = isExplored;
    }

    public void setSuperData(ClassData superdata) {
        this.superdata = superdata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    List<Field> getFields() {
        if (fields == null) { // Not yet initialized
            fields = new LinkedList<>();
            for (Field f : classe.getDeclaredFields()) {
                if (config.isVisible(f)) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }

    List<Constructor<?>> getConstructors() {
        if (constructors == null) {
            constructors = new LinkedList<>();
            for (Constructor<?> c : classe.getDeclaredConstructors()) {
                if (config.isVisible(c))
                    constructors.add(c);
            }
        }
        return constructors;
    }

    List<Method> getMethods() {
        if (methods == null) {
            methods = new LinkedList<>();
            for (Method f : classe.getDeclaredMethods()) {
                if (config.isVisible(f))
                    methods.add(f);
            }
        }
        return methods;
    }

    /**
     * 
     */
    private void appendFields(StringBuilder sb, String target) {
        for (Field f : getFields()) {
            Class<?> type = f.getType();
            boolean isArray = type.isArray();
            String typeName = type.getSimpleName();
            if (type.isPrimitive() || typeName.equals("String") || isArray) {
                sb.append(target).append(" : ");
                appendSymbol(sb, f.getModifiers());
                if (config.isCStyleSig(classe)) {
                    sb.append(getTypeDesc(type));
                    sb.append(" ").append(f.getName()).append('\n');
                } else {
                    sb.append(f.getName()).append(": ");
                    sb.append(getTypeDesc(type)).append('\n');
                }
            } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
                String baseType = getGenericParameter(f).getSimpleName();
                sb.append(target).append(" *--> \"").append(f.getName());
                sb.append(" *\" ").append(baseType);
                if (List.class.isAssignableFrom(type))
                    sb.append(" : {ordered}");
                sb.append('\n');
            } else {
                sb.append(target).append(" *--> \"").append(f.getName());
                sb.append(" 1\" ").append(typeName).append('\n');
            }
        }
        if (config.isFlat(classe) && superdata != null) {
            superdata.appendFields(sb, target);
        }
    }

    private String getTypeDesc(Class<?> rtype) {
        if (rtype.isArray()) {
            Class<?> at = rtype.componentType();
            return at.getSimpleName() + "[]";
        }
        return rtype.getSimpleName();
    }

    /**
     * 
     */
    private String getSignature(String target, Method m) {
        StringBuilder sb = new StringBuilder();
        sb.append(target).append(" : ");
        appendSymbol(sb, m.getModifiers());
        if (config.isCStyleSig(classe)) { // append type at the beginning
            String typeName = config.noFluent(m) ? "void" : getTypeDesc(m.getReturnType());
            sb.append(typeName).append(' ');
        }
        sb.append(m.getName()).append('(');
        boolean notFirst = false;
        for (Class<?> p : m.getParameterTypes()) {
            if (notFirst)
                sb.append(", ");
            sb.append(p.getSimpleName().replace("[]", " ..."));
            notFirst = true;
        }
        sb.append(")");
        if (!config.isCStyleSig(classe)) { // append type at the end
            String typeName = config.noFluent(m) ? "void" : getTypeDesc(m.getReturnType());
            if (typeName != "void")
                sb.append(": ").append(typeName);
        }
        return sb.toString();
    }

    private void addSignatures(String target, SortedSet<String> seenMethods) {
        for (Method m : getMethods()) {
            seenMethods.add(getSignature(target, m));
        }
        if (config.isFlat(classe) && superdata != null) {
            superdata.addSignatures(target, seenMethods);
        }

    }

    /**
    * 
    */
    private void appendConstructors(StringBuilder sb, String target) {
        for (Constructor<?> m : getConstructors()) {
            sb.append(target).append(" : ");
            appendSymbol(sb, m.getModifiers());
            sb.append(target).append('(');
            boolean notFirst = false;
            for (Class<?> p : m.getParameterTypes()) {
                if (notFirst)
                    sb.append(", ");
                sb.append(p.getSimpleName().replace("[]", " ..."));
                notFirst = true;
            }
            sb.append(")\n");
        }
    }

    /**
     * 
     */
    private void appendMethods(StringBuilder sb, String target) {
        SortedSet<String> seenMethods = new TreeSet<String>();
        addSignatures(target, seenMethods);
        for (String m : seenMethods) {
            sb.append(m).append('\n');
        }
    }

    public Class<?> getGenericParameter(Field f) {
        Type type = f.getGenericType();
        if (ParameterizedType.class.isInstance(type)) {
            Type[] types = ((ParameterizedType) type).getActualTypeArguments();
            int pos = Map.class.isAssignableFrom(f.getType()) ? 1 : 0;
            try {
                return (Class<?>) types[pos];
            } catch (java.lang.ClassCastException e) {
                return Object.class;
            }
        }
        return Object.class;
    }

    private void appendSymbol(StringBuilder sb, int modifier) {
        if (Modifier.isAbstract(modifier))
            sb.append("{abstract} ");
        if (Modifier.isStatic(modifier))
            sb.append("{static} ");
        if (Modifier.isPublic(modifier))
            sb.append("+");
        else if (Modifier.isProtected(modifier))
            sb.append("#");
        else if (Modifier.isPrivate(modifier))
            sb.append("-");
        else
            sb.append("~");
    }

    private void appendImplementedInterfaces(StringBuilder sb, String target) {
        Class<?>[] interfaces = classe.getInterfaces();
        final String link = " <|.. ";
        for (Class<?> s : interfaces) {
            sb.append(s.getSimpleName()).append(link).append(target).append('\n');
        }
        if (config.isFlat(classe) && superdata != null) {
            superdata.appendImplementedInterfaces(sb, target);
        }
    }

    void appendIn(StringBuilder sb) {
        if (plantUML == null) {
            if (classe.isInterface()) {
                sb.append("interface ");
            } else if (classe.isEnum()) {
                sb.append("enum ");
            } else {
                if (Modifier.isAbstract(classe.getModifiers())) {
                    if (config.isFlat(classe))
                        return; // does not include abstact classes
                    sb.append("abstract ");
                }
                sb.append("class ");
            }
            sb.append(name);
            if (Modifier.isFinal(classe.getModifiers()))
                sb.append(" << final >>");
            sb.append("\n");
            appendImplementedInterfaces(sb, name);
            Class<?> sup = classe.getSuperclass();
            if (!config.isFlat(classe) && sup != null && sup.getName() != "java.lang.Object") {
                final String link = " <|-- ";
                sb.append(sup.getSimpleName()).append(link).append(name).append('\n');
            }
            appendFields(sb, name);
            appendConstructors(sb, name);
            appendMethods(sb, name);
        } else {
            sb.append(plantUML);
        }
    }

}
