#include <iostream>

template <class T> class java_array{
    public:
        int length;
        T* elems;
        
        java_array(){
            length=0;
            elems=nullptr;
        }

        java_array(int size){
            if(size<0){
                throw "negative array size";
            }else if(size==0){
                length=0;
                elems=nullptr;
            }else{
               length=size;
               elems=new T[length]; 
            }
        }

        java_array(std::initializer_list<T> list){
            length=list.size();
            elems=new T[length];
            std::copy(list.begin(), list.end(), elems);
        }
        
        java_array(T list[],int size){
            if(size<0){
                throw "negative array size";
            }
            length=size;
            elems=new T[length];
            //T elems[length];
            for(int i=0;i<length;i++){
                elems[i]=list[i];
            }
        }
        
        T& operator[] (int index) const{
            if(index<length && index>=0){
                return elems[index];
            }
            throw "array index out of bounds exception";
        }

        java_array<T> operator= (std::initializer_list<T> rhs){
            length=rhs.size();
            elems=new T[length];
            std::copy(rhs.begin(), rhs.end(), elems);
            return *this;
        }
};
