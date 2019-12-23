#include "car.h"
#include "java_array_single.h"
#include <iostream>
#include <array>
#include <typeinfo>

using namespace std;
using namespace ns1;

void ns1::car::print()
{
    std::cout << year << std::endl;
}

void print(ns1::car::inner *obj)
{
    std::cout << obj->price << std::endl;
}

void car_test(car *c)
{
    c->year = 1995;
    c = new car(1453);
}

void arr_test(java_array<int> arr)
{
    //arr[0]=55;
    arr = java_array<int>({66, 99});
}

void arr_single(int x)
{
    x = 555;
}

void arr()
{
    java_array_single<int> *single = new java_array_single<int>({6, 74, 44});

    //cout << (*single)[3] << endl;
    java_array<java_array_single<int>> multi2(new int[2]{3, 1}, 2);
    //java_array<java_array<java_array_single<int>>> multi2({{{1, 2, 11}, {3, 4}}, {{1, 2}, {3, 4}}});
    //multi2.init(new int[3]{2, 1, 3}, 3);

    cout << "len=" << multi2.length << endl;
    cout << "len1=" << multi2[0].length << endl;
    //cout << "len2=" << multi2[0][0].length << endl;

    for (int i = 0; i < multi2.length; i++)
    {
        //cout << "lenx=" << multi2[i].length << endl;
        for (int j = 0; j < multi2[0].length; j++)
        {
            cout << "elem=" << multi2[i][j] << endl;
        }
    }
}

template <typename type> int prim(){

    
    return 0;
}

/*
(1) java local var could be normal var but becomes ref/ptr(&var) when pass and return
(2) or just pointer everywhere except prims
*/
int main(void)
{
    /*car* mycar=car(2019);
    mycar-print();
    car_test(&mycar);
    mycar.print();*/
    //car::inner* in=new ns1::car::inner(500);
    //print(in);
    prim<void>();
    
    //arr();
    //arr0();
    //arr_prim();
}
