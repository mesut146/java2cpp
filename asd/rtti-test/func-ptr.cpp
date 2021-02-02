#include <iostream>
#include <string>
#include <unordered_map>
#include <rttr/registration>
using namespace rttr;

class person{
public:
  int age;
  person(int age):age(age){}
  
  void print(){
    std::cout << age << "\n";
  }
  static void what(){
    std::cout << "person" << "\n";
  }
};


void asd(){
  std::cout << "asd" << "\n";
}

RTTR_REGISTRATION
{
    registration::class_<person>("person")
         .constructor<int>()
         .property("age", &person::age)
         .method("print", &person::print);
}

int main(){
  person obj(25);
  
  method meth = type::get(obj).get_method("print");
  meth.invoke(obj);
  
  return 0;
}
