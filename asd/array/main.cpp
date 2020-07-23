#include "array_single.h"
#include <iostream>
#include <array>
#include <typeinfo>

using namespace std;


template <class T>
void print_multi(array_multi<T> *arr){
    for(int i=0;i<arr->length;i++){
      cout<< i <<"="<<arr->get(i)<<endl;
    }
}

void single()
{
    array_single<int> *dim1 = new array_single<int>({6, 74, 44});
    //array_single<array_single<int>> dim2(new int[2]{3, 1}, 2);
    //array_single<array_single<array_single<int>>> dim3({{{1, 2, 11}, {3, 4}}, {{1, 2}, {3, 4}}});


    cout << "len=" << dim1->length << endl;
    //cout << "len1=" << dim1[0]->length << endl;
    //cout << "len2=" << multi2[0][0].length << endl;

    /*for (int i = 0; i < dim1.length; i++)
    {
        //cout << "lenx=" << multi2[i].length << endl;
        for (int j = 0; j < dim1[0].length; j++)
        {
            cout << "elem=" << dim1[i][j] << endl;
        }
    }*/
}

void multi1(){
    array_multi<int> *dim1 = new array_multi<int>({6, 74, 44});

    cout << "len=" << dim1->length << endl;
    print_multi(dim1);
    //cout << "len1=" << dim1[0]->length << endl;
    //cout << "len2=" << multi2[0][0].length << endl;

    /*for (int i = 0; i < dim1.length; i++)
    {
        //cout << "lenx=" << multi2[i].length << endl;
        for (int j = 0; j < dim1[0].length; j++)
        {
            cout << "elem=" << dim1[i][j] << endl;
        }
    }*/
}

void multi2(){
    //array_multi<int> *dim1 = new array_multi<int>({6, 74, 44});
    //int[][] arr=new int[5][10];50 elems
    //int[] arr2=arr[0];
    //print(arr2.length);
    array_multi<array_multi<int>> *dim2=new array_multi<array_multi<int>>(2,{5,10});
    //array_single<array_single<array_single<int>>> dim3({{{1, 2, 11}, {3, 4}}, {{1, 2}, {3, 4}}});


    cout << "len=" << dim2->length << endl;
    //print_multi(dim2);
    //cout << "len1=" << dim1[0]->length << endl;
    //cout << "len2=" << multi2[0][0].length << endl;

    /*for (int i = 0; i < dim1.length; i++)
    {
        //cout << "lenx=" << multi2[i].length << endl;
        for (int j = 0; j < dim1[0].length; j++)
        {
            cout << "elem=" << dim1[i][j] << endl;
        }
    }*/
}

template <typename type>
int prim(){

    return 0;
}

/*
(1) java local var could be normal var but becomes ref/ptr(&var) when pass and return
(2) or just pointer everywhere except prims
*/
int main(void)
{
    multi2();

}
