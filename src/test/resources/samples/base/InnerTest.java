package base;

public class InnerTest {

    int x = 123456;
    Inner obj;
    Inner.Inner2 obj2;
    static int x_static = 22;
    
    void print(){
      obj2.innerRef();
      obj = new Inner();
    }
    
    static void print_static(){
    }
    
    void anonyTest(){
       new InnerTest(){
         void print(){
           int val = x;//ref.x or super.x , both works?
           anonyMethod();
           InnerTest.print_static();
         }
         void anonyMethod(){}
       };
    }
    
    void anonyOther(){
       new MyType(){
         void anony(){
           int val = x;//ref.x
           print();
           InnerTest.print_static();
         }
       };
    
    }

    class Inner {
        
        void innerMeth() {
            print();//ref.print
            x = 555;//ref.x
            obj.innerMeth();//ref.obj.innerMeth
        }
        
        void anonyRef(){
          new MyType(){
            void asd(){
              int val = x;//ref.ref.x
            }
          };
        }
        
        class Inner2{
          void innerRef(){
            int val_inner2 = x;//ref->ref->x
            print();//ref->ref->print()
            print_static();//InnerTest::print_static()
            x_static = 55;//InnerTest::x_static
          }
        }
    }
}

class MyType{
  void anony(){}
}
