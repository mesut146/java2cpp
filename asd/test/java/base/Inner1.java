package base;

public class Inner1 {

    int x = 123456;
    Inner obj;
    static int x_static = 22;
    
    void print(){
      obj = new Inner();
      obj.innerMeth();
    }
    
    static void print_static(){
    }
    

    class Inner {
        void innerMeth() {
            print();//ref.print
            x = 555;//ref.x
            obj.innerMeth();//ref.obj.innerMeth
            local();
        }
        void local(){}
       
    }
}

