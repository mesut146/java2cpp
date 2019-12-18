#pragma once

#include <iostream>
#include <vector>
#include <string>
#include <memory>
#include "java_array_single.h"

using namespace std;

//at least 2 dimensions
//T will be base type

template <typename T>
class java_array_multi
{
public:
    int length;
    //T *elems;//unnecessary
    java_array_multi<T> *map;
    java_array_single<T> *single;
    int dims;
    int *sizes;

    java_array_multi()
    {
        length = 0;
        //elems = nullptr;
    }

    java_array_multi(int size)
    {
        if (size < 0)
        {
            throw std::runtime_error("negative array size");
        }
        else if (size == 0)
        {
            length = 0;
            //elems = nullptr;
        }
        else
        {
            length = size;
            map = new java_array_multi<T>[length];
        }
    }

    /*java_array_multi(std::initializer_list<T> list)
    {
        length = list.size();
        elems = new T[length];
        std::copy(list.begin(), list.end(), elems);
    }*/

    /*(T* list, int size)
    {
        if (size < 0)
        {
            throw std::runtime_error("negative array size");
        }
        length = size;
        elems = new T[length];
        for (int i = 0; i < length; i++)
        {
            elems[i] = list[i];
        }
    }*/

    java_array_multi(int *size, int n)
    {
        //cout << "init n=" << n << " size=" << size << endl;
        length = size[0];
        dims = n;
        //map = new java_array_multi<T>[length];
        if (n == 1) //dep
        {
            return;
        }
        else if (n == 2)
        {

            single = new java_array_single<T>[length];
            for (int i = 0; i < length; i++)
            {
                single[i] = java_array_single<T>(size[1]);
                cout << "single arr created " << typeid(single[i]).name() << endl;
            }
        }
        else
        {
            //elems = new T[length];
            for (int i = 0; i < length; i++)
            {
                cout << "type=" << typeid(T).name() << endl;
                //cout<<"elem="<<elems[i]<<endl;
                //elems[i]=T(size,1);
            }
        }
    }

    void init(int* sizes,int n){

    }

    java_array_multi<T> &operator[](int index) const
    {
        if (index < length && index >= 0)
        {
            cout<<"access multi "<<index<<endl;
            if (dims == 2)
            {
                return single[index];
            }

            return map[index];
        }
        throw std::runtime_error(format("array index out of bounds exception: index=%d size=%d", index, length));
    }

    /*java_array_multi<T> operator=(std::initializer_list<T> rhs)
    {
        length = rhs.size();
        elems = new T[length];
        std::copy(rhs.begin(), rhs.end(), elems);
        return this;
    }*/

    template <typename... Args>
    static std::string format(const std::string &format, Args... args)
    {
        size_t size = snprintf(nullptr, 0, format.c_str(), args...) + 1; // Extra space for '\0'
        std::unique_ptr<char[]> buf(new char[size]);
        snprintf(buf.get(), size, format.c_str(), args...);
        return std::string(buf.get(), buf.get() + size - 1); // We don't want the '\0' inside
    }
};
