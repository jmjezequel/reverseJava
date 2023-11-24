package fr.irisa.diverse.plantUml;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class ClassData {
    /**
     * Name of the option to turn return type to "void" for a set of Members
     */
    private static final String NOFLUENT = "NOFLUENT";
    /**
     * Name of the option to hide a set of Members
     */
    private static final String HIDE = "HIDE";
    /**
     * Name of the option to hide Members whose name match the regexp
     */
    private static final String HIDEREGEXP = "HIDEREGEXP";
    String plantUML = null;
    private Class<?> classe = null;
    protected List<Field> fields = null;
    protected List<Method> methods = null;
    private ClassData superdata = null;
    String name = null;
    boolean flat = false;
    private boolean isExplored = false;
    private boolean showPrivate = false;
    private boolean showProtected = false;
    private boolean showPackage = false;
    Set<String> noFluent = new HashSet<>();
    Set<String> hide = new HashSet<>();
    Pattern hideRegexp = null;

    public boolean isExplored() {
        return isExplored;
    }

    public void setExplored(boolean isExplored) {
        this.isExplored = isExplored;
    }

    public ClassData(Class<?> base) {
        classe = base;
        name = base.getSimpleName();
    }

    public ClassData(String uml) {
        plantUML = uml;
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
                if (isVisible(f)) {
                    fields.add(f);
                }
            }
        }
        return fields;
    }

    List<Method> getMethods() {
        if (methods == null) {
            methods = new LinkedList<>();
            for (Method f : classe.getDeclaredMethods()) {
                if (isVisible(f))
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
                if (isArray) {
                    Class<?> at = type.componentType();
                    sb.append(at.getSimpleName()).append("[]");
                } else {
                    sb.append(typeName);
                }
                sb.append(" ").append(f.getName()).append('\n');
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
        if (flat && superdata != null) {
            superdata.appendFields(sb, target);
        }
    }

    /**
     * 
     */
    private String getSignature(String target, Method m) {
        StringBuilder sb = new StringBuilder();
        Class<?> rtype = m.getReturnType();
        boolean isArray = rtype.isArray();
        String typeName = noFluent.contains(m.getName()) ? "void" : rtype.getSimpleName();
        sb.append(target).append(" : ");
        appendSymbol(sb, m.getModifiers());
        if (isArray) {
            Class<?> at = rtype.componentType();
            sb.append(at.getSimpleName()).append("[]");
        } else {
            sb.append(typeName);
        }
        sb.append(" ").append(m.getName()).append('(');
        boolean notFirst = false;
        for (Class<?> p : m.getParameterTypes()) {
            if (notFirst)
                sb.append(", ");
            sb.append(p.getSimpleName());
            notFirst = true;
        }
        sb.append(")");
        return sb.toString();
    }


    private void addSignatures(String target, SortedSet<String> seenMethods){
        for (Method m : getMethods()) {
            seenMethods.add(getSignature(target, m));
        }
        if (flat && superdata != null) {
            superdata.addSignatures(target, seenMethods);
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

    // boolean isVisibleAnnotation(AnnotatedElement f) {
    // if (!f.isAnnotationPresent(UmlShowWhen.class))
    // return true;
    // UmlShowWhen annotation = f.getAnnotation(UmlShowWhen.class);
    // String s = annotation.value();
    // boolean result = hide.contains(s);
    // if (hideRegexp != null) {
    // result = result || hideRegexp.matcher(s).matches();
    // }
    // // System.out.println(name+showConditions.toString() + ':' + s + '=' +
    // result);
    // return result;
    // }

    private boolean isVisible(Member m) {
        String name = m.getName();
        if (hide.contains(name))
            return false;
        if (hideRegexp != null && hideRegexp.matcher(name).matches())
            return false;
        int modifier = m.getModifiers();
        if (Modifier.isPublic(modifier))
            return true;
        if (Modifier.isProtected(modifier))
            return showProtected;
        if (Modifier.isPrivate(modifier))
            return showPrivate;
        // if none of the above, means it is package visibility
        return showPackage;
    }

    private void appendImplementedInterfaces(StringBuilder sb, String target) {
        Class<?>[] interfaces = classe.getInterfaces();
        final String link = " <|.. ";
        for (Class<?> s : interfaces) {
            sb.append(s.getSimpleName()).append(link).append(target).append('\n');
        }
        if (flat && superdata != null) {
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
                    if (flat)
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
            if (!flat && sup != null && sup.getName() != "java.lang.Object") {
                final String link = " <|-- ";
                sb.append(sup.getSimpleName()).append(link).append(name).append('\n');
            }
            appendFields(sb, name);
            appendMethods(sb, name);
        } else {
            sb.append(plantUML);
        }
    }

    public void setOption(String option, String value) {
        switch (option) {
            case "FLAT":
                flat = value == null || value == name;
                break;
            case "SHOWPRIVATE":
                showPrivate = value == null || value == name;
                break;
            case "SHOWPROTECTED":
                showProtected = value == null || value == name;
                break;
            case "SHOWPACKAGE":
                showPackage = value == null || value == name;
                break;
            case HIDE:
                hide.addAll(Arrays.asList(value.split(",")));
                break;
            case HIDEREGEXP:
                hideRegexp = Pattern.compile(value);
                break;
            case NOFLUENT:
                noFluent.addAll(Arrays.asList(value.split(",")));
                break;
            default:
        }
    }
}
