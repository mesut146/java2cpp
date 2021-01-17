package base;

public class AnonyTest {

    int x = 123456;
    AnonyTest obj;
    
    void print(){
      new AnonyTest(){
        void print(){
          //todo
          int y=x;
          obj=null;
        }
      };
    }

}
