package com.mesut.j2cpp;

import java.util.ArrayList;
import java.util.List;

public class Config {

    public static List<String> ignored = new ArrayList<>();

    public static boolean use_auto = true;
    public static boolean use_boxing = true;
    public static boolean full = true;

    public static boolean ns_type_nested = true;
    public static boolean ns_indent = false;
    public static boolean ns_nested_indent = false;
    //create folder named 'include' in destination and put all headers in it
    public static boolean separateInclude = false;

    //create a header that forwards all class declarations
    public static boolean common_forwards = true;
    //make all .cpp include that header
    public static boolean include_common_forwards = true;

    //make headers for classpath classes
    public static boolean writeLibHeader = true;

    //move field initializers into constructors
    public static boolean fields_in_constructors = true;
    //init static fields by Construct On First Use Idiom
    public static boolean static_field_cofui = true;
    public static String static_init_name = "si";

    //put base class name in front of method,e.g Base::member()
    public static boolean qualifyBaseMethod = false;

    //force all types inherit from java::lang::Object
    public static boolean baseClassObject = false;
    public static boolean printDestructor = false;

    public static boolean ptr_parameter = true;
    public static boolean ptr_method = true;
    public static boolean ptr_field = true;
    public static boolean ptr_typeArg = true;
    public static boolean ptr_varDecl = true;
    public static boolean ptr_cast = true;
    //take outer ref as constructor arg otherwise create setter
    public static boolean outer_ref_cons_arg = false;
    //make all fields public
    public static boolean fields_public = true;
    //make all methods public
    public static boolean methods_public = true;

    public static String mainClass = null;

    //use arr[idx] instead of vector.at(idx)
    public static boolean array_access_bracket = false;

    //shrink types based on namespaces
    public static boolean normalizeTypes = true;
    //print parameter names in header
    public static boolean printParamNames = true;
    public static String parentName = "_parent_ref";
    public static String refSetterName = "setRef";
    public static String type_int = "int";
    public static String type_long = "long";
    public static String type_byte = "char";
    public static String type_char = "wchar_t";
    public static String type_float = "float";
    public static String type_double = "double";
    public static String type_short = "char16_t";
    public static String type_boolean = "bool";

    static {
        ignored.add("java.lang.Throwable");
    }

}
