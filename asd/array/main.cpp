#include "array_single.h"
#include <iostream>
#include <array>
#include <typeinfo>
#include <vector>

using namespace std;

template <typename T>
void log(T arg){
  std::cout << arg << "\n";
}


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

void vect(){
  class cls;
  std::vector<cls*>* arr = new std::vector<cls*>(10);
  std::vector<std::vector<cls*>*> *a2 = new std::vector<std::vector<cls*>*>(5,new std::vector<cls*>(10));
  log(arr->size());
  log(arr->at(0));
  log(a2->size());
  log(a2->at(0)->size());
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
    vect();
    //single();

}
