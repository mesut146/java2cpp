#include <iostream>

int func(){
  try{
    if(1 ==1){
      throw 5; 
    }
    return 255;
  }
  catch(int val){
    std::cout << "catched=" << val << "\n";
    return -1;
  }
  return 0;
}

int main(){
  int val = func();
  std::cout << "val=" << val << "\n";
  return 0;
}
