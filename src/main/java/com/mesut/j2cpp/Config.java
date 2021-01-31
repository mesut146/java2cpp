package com.mesut.j2cpp;

public class Config {
    public static boolean ns_type_nested = true;
    public static boolean ns_indent = false;
    public static boolean ns_nested_indent = false;
    public static boolean include_auto = false;
    //create folder named 'include' in destination and put all headers in it
    public static boolean separateInclude = false;

    public static boolean common_forwards = true;
    public static boolean include_common_forwards = true;

    //make all in one header
    public static boolean common_headers = true;
    public static boolean include_common_headers = true;

    //write headers for classpath classes
    public static boolean writeLibHeader = true;

    public static boolean common_lib_forwards = true;

    //force all types inherit from java::lang::Object
    public static boolean baseClassObject = false;
    public static boolean printDestructor = false;

    public static boolean ptr_parameter = true;
    public static boolean ptr_method = true;
    public static boolean ptr_field = true;
    public static boolean ptr_typeArg = true;
    public static boolean ptr_varDecl = true;
    public static boolean ptr_cast = true;
    //move inner classes to uppermost parent level(still in same file)
    public static boolean move_inners = true;
    //move inner classes to separate file(implementation still in same file,per class by header)
    public static boolean move_inners_out = true;
    //take outer ref as constructor arg otherwise create setter
    public static boolean outer_ref_cons_arg = false;
    //make all fields public
    public static boolean fields_public = true;
    //make all methods public
    public static boolean methods_public = true;

    public static boolean use_vector = true;
    public static boolean array_access_bracket = false;

    //put use namespace stmt if class has types that are longer than limit
    public static int shrinkNsLimit = 3;

    //shrink types based on namespaces
    public static boolean normalizeTypes = true;

    //print parameter names in header
    public static boolean printParamNames = false;

    public static String parentName = "_parent_ref";
    public static String refSetterName = "setRef";
}
