#include "car.h"
#include "java_array_multi.h"
#include "java_array.h"
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

    cout << (*single)[3] << endl;
    //java_array<java_array_single<int>> multi2({{1, 2}, {3, 4, 5}});
    java_array<java_array<java_array_single<int>>> multi2({{{1, 2, 11}, {3, 4}}, {{1, 2}, {3, 4}}});
    //multi2.init(new int[3]{2, 1, 3}, 3);

    cout << "len=" << multi2.length << endl;
    cout << "len1=" << multi2[0].length << endl;
    cout << "len2=" << multi2[0][0].length << endl;

    for (int i = 0; i < multi2.length; i++)
    {
        //cout << "lenx=" << multi2[i].length << endl;
        for (int j = 0; j < multi2[0].length; j++)
        {
            //cout << "elem=" << multi2[i][j] << endl;
        }
    }
}

void arr0()
{
    java_array_single<int> *single = new java_array_single<int>({10, 20, 30});
    java_array_multi<int> *multi = new java_array_multi<int>(new int[2]{2, 3}, 2);

    /*multi2[0]=java_array<int>(3);
    multi2[1]=java_array<int>(3);*/
    //arr_test(single);
    cout << "single len=" << single->length << endl;
    for (int i = 0; i < single->length; i++)
    {
        //cout << (*single)[i].length << endl;
    }

    /*for (int i = 0; i < 2; i++)
    {
        //cout<<multi[i][j]<<endl;
        for (int j = 0; j < 3; j++)
        {
            cout << multi[i][j] << endl;
        }
    }*/
    cout << "multi len=" << multi->length << endl;
    java_array_multi<int> *m1 = &multi[1];
    java_array_single<int> *s1 = (java_array_single<int> *)m1;
    cout << m1->length << endl;
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
    //arr0();
    //arr_prim();
}