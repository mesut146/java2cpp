package com.mesut.j2cpp;

public class Config {
    public static boolean include_auto = false;
    //create folder named 'include' in destination and put all headers in it
    public static boolean separateInclude = true;
    //force all types inherit from java::lang::Object
    public static boolean baseClassObject = false;
    public static boolean printDestructor = false;
}
