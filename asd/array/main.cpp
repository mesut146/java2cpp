#include "array_single.h"
#include <iostream>
#include <array>
#include <typeinfo>

using namespace std;


/*template <class T>
void print_multi(array_multi<T> *arr){
    for(int i=0;i<arr->length;i++){
      cout<< i <<"="<<arr->get(i)<<endl;
    }
}*/

void single()
{
    array_single<int> *dim1 = new array_single<int>({6, 74, 44});
    array_single<array_single<int>> *dim2=new array_single<array_single<int>>(5,array_single<int>(10));
    //array_single<array_single<array_single<int>>> dim3({{{1, 2, 11}, {3, 4}}, {{1, 2}, {3, 4}}});


    cout << "len=" << dim2->get(0).length << endl;
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

}

void multi2(){


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
    //multi2();
    single();

}
