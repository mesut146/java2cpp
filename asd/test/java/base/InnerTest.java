package base;

public class InnerTest {

    int x = 123456;
    Inner obj;
    
    void print(){
      
    }

    class Inner {
        
        void innerMeth() {
            print();
            x = 555;
            obj.innerMeth();
        }
    }
}
