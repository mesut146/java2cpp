package base;

public class InnerTest2 {

    int x = 123456;
    Inner obj;
    Inner.Inner2 obj2;
    
    void print(){
      //obj2.innerRef();
    }
    
    /*void anonyTest(){
       new InnerTest(){
         void print(){
           int val = x;//ref.x or super.x , both works?
         }
       };
    }
    
    void anonyOther(){
       new MyType(){
         void anony(){
           int val = x;//ref.x
         }
       };
    
    }*/

    class Inner {
        
        void innerMeth() {
            print();//ref.print
            x = 555;//ref.x
            obj.innerMeth();//ref.obj.innerMeth
        }
        
        /*void anonyRef(){
          new MyType(){
            void asd(){
              int val = x;//ref.ref.x
            }
          };
        }*/
        
        class Inner2{
          void innerRef(){
            //int val_inner2 = x;//ref.ref.x
          }
        }
    }
}

class MyType{
  void anony(){}
}
