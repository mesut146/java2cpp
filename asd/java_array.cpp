#include "java_array.h"

template <typename T> array<T>::array(T arr[], int s) {
    elems = new T[s];
    length = s;
    for(int i = 0; i < length; i++){
        elems[i] = arr[i];
    }
}
template <typename T> T operator[] (int index){
    if(index>=0&&index<length){
        return elems[index];
    }else{
        throw 0;
    }
}
