#include "car.h"
#include "java_array.h"
#include <iostream>

using namespace std;

ns1::car::car(int y){
    year=y;
}

void ns1::car::print(){
    std::cout<<year<<std::endl;
}

ns1::car::inner::inner(int p){
    price=p;
}

void print(ns1::car::inner* obj){
    std::cout<<obj->price<<std::endl;
}

void array(){
    java_array<int>* arr=new java_array<int>(new int[3]{1,2,3},3);
    int len=sizeof(arr);
    int len0=sizeof(int);
    cout<<"size="<<arr->length<<endl;
    for(int i=0;i<15;i++){
        //cout<<arr[i]<<endl;
    }
    
}

int main(void){
    /*ns1::car* mycar=new ns1::car(2019);
    mycar->print();
    ns1::car::inner* in=new ns1::car::inner(500);
    print(in);*/
    
    array();
}
