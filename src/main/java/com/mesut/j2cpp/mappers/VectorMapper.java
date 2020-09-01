package com.mesut.j2cpp.mappers;

public class VectorMapper {

    //obj.name() -> expr
    //transform java lists to c++ vector
    String getMethod(String obj, String name) {
        if (name.equals("add")) {
            return "push_back";
        }
        if (name.equals("get")) {
            return "at";
        }
        if (name.equals("remove")) {
            return "erase";
        }

        return "";
    }
}
