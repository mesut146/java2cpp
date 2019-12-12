#include "car.h"
#include "java_array.h"
#include <iostream>
#include <array>

using namespace std;
using namespace ns1;

void ns1::car::print(){
    std::cout<<year<<std::endl;
}

void print(ns1::car::inner* obj){
    std::cout<<obj->price<<std::endl;
}

void car_test(car* c){
    c->year=1995;
    c=new car(1453);
}

void arr_test(java_array<int> arr){
    //arr[0]=55;
    arr=java_array<int>({66,99});
}

void arr_prim(){
    //int[][] arr=new int[][]{{1,2},{3,4}}
    int** arr=new int[2][3]{{1,2,3},{4,5,6}};

    int l1=sizeof(arr);
    int l2=sizeof(arr[0]);
    cout<<"size1="<<l1<<endl;//data size*size
    cout<<"size2="<<l2<<endl;//data size
    //cout<<"size3="<<arr.size()<<endl;
    for(int i=0;i<2;i++){
        for(int j=0;j<3;j++){
            cout<<"i="<<i<<" "<<arr[i][j]<<endl;
        }
        
    }
}

void arr_single(int x){
    x=555;
}

void arr(){
    java_array<int> single=java_array<int>({10,20,30});
    //int[][] arr=new int[][]{{1,2,3},{4,5,6}}
    java_array<java_array<int>> multi={{1,2,3},{4,5,6}};
    //multi[0]=java_array<int>({1,2,3});
    //multi[1]=java_array<int>({4,5,6});
    //multi2[0]={9,8,7};
    //multi2[1]={77,88,99};

    single={11,22,33};
    arr_single(multi[0][0]);
    //arr_test(single);
    for(int i=0;i<single.length;i++){
        //cout<<single[i]<<endl;
    }

    /*for(int i=0;i<multi2.length;i++){
            for(int j=0;j<multi2[0].length;j++){
                cout<<multi2[i][j]<<endl;
            }
        }*/

    /*for(int i=0;i<multi2.length;i++){
        for(int j=0;j<multi2[0].length;j++){
        for(int k=0;k<multi2[0][0].length;k++)
            cout<<multi2[i][j][k]<<endl;
        }
    }*/
}

/*
(1) java local var could be normal var but becomes ref/ptr(&var) when pass and return
(2) or just pointer everywhere except prims
*/
int main(void){
    /*car* mycar=car(2019);
    mycar-print();
    car_test(&mycar);
    mycar.print();*/
    //car::inner* in=new ns1::car::inner(500);
    //print(in);
    
    //arr();
    arr_prim();
}
