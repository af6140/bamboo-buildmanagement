package com.entertainment.bamboo.plugins.buildmanagement.tasks.utils;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.Eval;

import java.util.Map;

/**
 * Created by dwang on 9/28/16.
 */
public class GroovyUtil {

    public static boolean eval(Map<String, String> variables, String expr) {
        GroovyShell groovyShell=getGroovyShell(variables);
        return eval(groovyShell,expr);
    }

    public static boolean eval(GroovyShell groovyShell, String expr) {
        Object obj = groovyShell.evaluate(expr);
        boolean bool = (Boolean)obj;
        return bool;
    }

    public static  GroovyShell getGroovyShell(Map<String, String> variables) {
        Binding binding =new Binding(variables);
        GroovyShell groovyShell=new GroovyShell(binding);
        return groovyShell;
    }
}
