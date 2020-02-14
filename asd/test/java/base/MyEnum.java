package base;

enum MyEnum{
    m1("m111"),m2;
    String str;
    int x=5;
    //Object System;

    MyEnum(String s){
        str=s;
    }
    
    MyEnum(){
        str="emp";
    }

    void print(){
        System.out.println(str+","+x);//System::out->println()
        //System.arraycopy();//System::arraycopy()
    }
}
