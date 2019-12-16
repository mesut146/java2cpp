#include "car.h"
#include "java_array.h"
#include "lin_array.h"
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

void arr_prim()
{
    //lin_array<lin_array<lin_array<int>>> arr;
    lin_array<lin_array<lin_array<int>>> arr;
    arr.init();
}

void arr_single(int x)
{
    x = 555;
}

void arr()
{
    java_array<int> single = java_array<int>({10, 20, 30});
    java_array<java_array<int, int>, int> multi(new int[2]{2,3},2));
    //java_array<java_array<java_array<int, int>, int>, int> multi(new int[3]{2, 3, 1}, 3);


    //multi[0]=java_array<int>({1,2,3});
    //multi[1]=java_array<int>({4,5,6});

    //cout<<java_array<int,int>::string_format("array index out of bounds exception: index=%d size=%d", 0, 10)<<endl;

    //arr_test(single);
    for (int i = 0; i < single.length + 1; i++)
    {
        //cout<<single[i]<<endl;
    }

    /*for (int i = 0; i < 2; i++)
    {
        //cout<<multi[i][j]<<endl;
        for (int j = 0; j < 3; j++)
        {
            cout << multi[i][j] << endl;
        }
    }*/
    cout << multi.length << endl;
    cout << multi[0].length << endl;
    //cout << multi[0][0].length << endl;
    for (int i = 0; i < 2; i++)
    {
        for (int j = 0; j < 3; j++)
            for (int k = 0; k < 1; k++)
            {
                //cout << multi[i][j].length << endl;
            }
    }
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
 
    arr();
    //arr_prim();
}
