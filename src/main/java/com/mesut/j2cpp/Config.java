package com.mesut.j2cpp;

public class Config {
    public static boolean include_auto = false;
    //create folder named 'include' in destination and put all headers in it
    public static boolean separateInclude = false;
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
    //move inner classes to uppermost parent level(still in same file)
    public static boolean move_inners = true;
    //move inner classes to separate file(implementation still in same file)
    public static boolean move_inners_out = true;

    public static boolean use_vector = true;

    //put use namespace stmt if class has types that are longer than limit
    public static int shrinkNsLimit = 3;

    public static String parentName = "_this_parent";
}
