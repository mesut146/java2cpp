package com.mesut.j2cpp;

public class Config {
    public static boolean include_auto = false;
    //create folder named 'include' in destination and put all headers in it
    public static boolean separateInclude = true;
    //force all types inherit from java::lang::Object
    public static boolean baseClassObject = false;
    public static boolean printDestructor = false;

    public static boolean ptr_parameter = true;
    public static boolean ptr_method = true;
    public static boolean ptr_field = true;
    public static boolean ptr_typeArg = true;
    public static boolean ptr_new = false;
    public static boolean ptr_varDecl = true;
    public static boolean ptr_cast = true;
}
