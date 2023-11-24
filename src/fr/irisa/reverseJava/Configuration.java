package fr.irisa.reverseJava;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Configuration {
    /**
     * Name of the option to turn return type to "void" for a set of Members
     */
    public static final String NOFLUENT = "NOFLUENT";
    /**
     * Name of the option to hide a set of Members
     */
    public static final String HIDE = "HIDE";
    /**
     * Name of the option to hide Members whose name match the regexp
     */
    public static final String HIDEREGEXP = "HIDEREGEXP";
    /**
     * Name of the option to only show the content of the listed packages
     */
    public static final String ONLYSHOW = "ONLYSHOW";  
    
    public static final String ALL = "ALL";  


    private Set<String> showPrivate = new HashSet<>();
    private Set<String> showProtected = new HashSet<>();
    private Set<String> showPackage = new HashSet<>();
    private Set<String> onlyShowPackages = new HashSet<>();
    private Set<String> hide = new HashSet<>();
    private Set<Pattern> hideRegexp = new HashSet<>();
    private Set<String> noFluent = new HashSet<>();
    private Set<String> flat = new HashSet<>();

    private boolean matches(Set<Pattern> patterns, String value){
        for (Pattern p : patterns) {
            if (p.matcher(value).matches()) return true;
        }
        return false;
    } 

    private boolean contains(Set<String> set, String value){
        if (set.contains(ALL)) return true;
        return set.contains(value);
    } 

    boolean isFlat(Class<?> c) {
        return flat.contains(c.getName());
    }

    boolean noFluent(Method m) {
        return noFluent.contains(m.getName());
    }

    boolean isVisible(Member m) {
        String name = m.getName();
        if (hide.contains(name))
            return false;
        if (matches(hideRegexp, name))
            return false;
        int modifier = m.getModifiers();
        if (Modifier.isPublic(modifier))
            return true;
        if (Modifier.isProtected(modifier))
            return contains(showProtected,name);
        if (Modifier.isPrivate(modifier))
            return contains(showPrivate,name);
        // if none of the above, means it is package visibility
        return contains(showPackage,name);
    }

   boolean isVisible(Class<?> c) {
        String name = c.getName();
        if (hide.contains(name))
            return false;
        if (matches(hideRegexp, name))
            return false;
        int modifier = c.getModifiers();
        if (Modifier.isProtected(modifier) && !contains(showProtected,name))
            return false;
        if (Modifier.isPrivate(modifier) && !contains(showPrivate,name))
            return false;
        if (onlyShowPackages.isEmpty()) return true;
        String pkgname = c.getPackageName();
        for (String p : onlyShowPackages) {
            if (pkgname.startsWith(p)) return true;
        }
        return false;
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

    public void setOption(String option, String value) {
        switch (option) {
            case "FLAT":
                flat.addAll(Arrays.asList(value.split(",")));
                break;
            case "SHOWPRIVATE":
                showPrivate.addAll(Arrays.asList(value.split(",")));
                break;
            case "SHOWPROTECTED":
                showProtected.addAll(Arrays.asList(value.split(",")));
                break;
            case "SHOWPACKAGE":
                showPackage.addAll(Arrays.asList(value.split(",")));
                break;
            case ONLYSHOW:
                onlyShowPackages.addAll(Arrays.asList(value.split(",")));
                break;
            case HIDE:
                hide.addAll(Arrays.asList(value.split(",")));
                break;
            case HIDEREGEXP:
                hideRegexp.add(Pattern.compile(value));
                break;
            case NOFLUENT:
                noFluent.addAll(Arrays.asList(value.split(",")));
                break;
            default:
        }
    }
}
