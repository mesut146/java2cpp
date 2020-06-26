#pragma once

#include <iostream>
#include <vector>
#include <string>
#include <memory>
#include <typeinfo>
#include <typeindex>

using namespace std;

template <typename T>
class array_multi
{
public:
    int length;
    T *elems;
    array_multi *map;

    array_multi()
    {
        length = 0;
        elems = nullptr;
    }

    array_multi(int size)
    {
        if (size < 0)
        {
            throw std::runtime_error("negative array size");
        }
        else if (size == 0)
        {
            length = 0;
            elems = nullptr;
        }
        else
        {
            length = size;
            elems = new T[length];
            map = new array_multi[length];
        }
    }

    array_multi(std::initializer_list<T> list)
    {
        length = list.size();
        elems = new T[length];
        std::copy(list.begin(), list.end(), elems);
    }

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

    bool isArray()
    {
        return string(typeid(T).name()).find("array_multi") != string::npos;
    }

    bool isSingleArray()
    {
        return string(typeid(T).name()).find("array_multi_single") != string::npos;
    }

    /*array_multi(std::initializer_list<T> sizes, int n)
    {
    }*/

    array_multi(int *sizes, int n)
    {
        length = sizes[0];
        if (length < 0)
        {
            throw std::runtime_error("negative array size");
        }
        elems = new T[length];
        if (n > 2)
        {
            //T obj(3);

            //cout << "obj=" << obj.length << endl;
            //obj.init(sizes + 1, n - 1);
            //map[0]=array_multi
            for (int i = 0; i < length; i++)
            {
                elems[i] = T(sizes + 1, n - 1);
                //elems[i].init(sizes + 1, n - 1);
            }
        }
        else
        {
            cout << "t is single" << endl;
            for (int i = 0; i < length; i++)
            {
                elems[i] = T(sizes[1]);
            }
        }
    }

    T &operator[](int index) const
    {
        return get(index);
    }

    void checkIndex(int index){
        if(index < 0 || index >= length){
            throw std::runtime_error(format("array index out of bounds exception: index=%d size=%d", index, length));
        }
    }

    T get(int index){
        checkIndex(index);
        return elems[index];
    }

    void set(int index,T value){
        checkIndex(index);
        elems[index] = value;
    }

    array_multi<T> operator=(std::initializer_list<T> rhs)
    {
        length = rhs.size();
        elems = new T[length];
        std::copy(rhs.begin(), rhs.end(), elems);
        return this;
    }

    template <typename... Args>
    std::string format(const std::string &format, Args... args) const
    {
        size_t size = snprintf(nullptr, 0, format.c_str(), args...) + 1; // Extra space for '\0'
        std::unique_ptr<char[]> buf(new char[size]);
        snprintf(buf.get(), size, format.c_str(), args...);
        return std::string(buf.get(), buf.get() + size - 1); // We don't want the '\0' inside
    }
};
