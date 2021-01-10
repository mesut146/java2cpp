#include "array/array_single.h"
#include "array/array_multi.h"
#include "java/lang/Object.h"

#include <functional>
#include <iostream>

template <typename T>
T instance_of(Object* obj){
    return is_base_of<T>(obj);
}

template <typename tryCode, typename finallyCode>
void* valued_finally(const tryCode& tcode, const finallyCode& fincode){

    void* try_ptr=nullptr;   
    void* fin_ptr=nullptr;
    
    try{
        try_ptr=(void*)tcode();
    }catch(...){
        try{
            fin_ptr=(void*)fincode();
        }catch(...){
            std::cout<<"fatal error in try finally code"<<endl;
            std::terminate();
        }
        throw;
    } 
    fin_ptr=(void*)fincode();
    
    //finally has return statement then return that
    if(fin_ptr){
        return fin_ptr;
    }
    //otherwise return try return statement
    return try_ptr;
}

template <typename tryCode, typename finallyCode>
void void_finally(const tryCode& tcode, const finallyCode& fincode){
    try{
        tcode();
    }catch(...){
        try{
            fincode();
        }catch(...){
            std::cout<<"fatal error in try finally code"<<endl;
            std::terminate();
        }
        throw;
    } 
    fincode();
}

