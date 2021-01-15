package base;


public class SuperTest{

  void print(){};

  static class inner extends SuperTest{
    void print(){};
  
  
    void print2(){
      print();
      super.print();
      this.print2();
    }
  }
}
