#pragma once


using namespace com::java::lang;

class MyEnum:Enum{

    public: static MyEnum* m1=new MyEnum(new String("m111"));
    public: static MyEnum* m2=new MyEnum();
    String* str;
    int x=5;

    MyEnum(String* s);
    MyEnum();
    void print();

};//class MyEnum
