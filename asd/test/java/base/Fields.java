package base;

class type{}

public class Fields{
  type obj = new type();
  static type st_obj = new type();
  
  Fields(int dummy){
    this(new type());
  }
  Fields(type p){
  }
  Fields(){}
  
}
