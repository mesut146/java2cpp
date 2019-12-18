#pragma once

#include <iostream>
#include <vector>
#include <string>
#include <memory>

using namespace std;

template <typename T>
class java_array_multi;

template <typename T>
class java_array_single : public java_array_multi<T>
{
public:
    //int length;
    T *elems;
    int x = 146;

    java_array_single()
    {
        java_array_multi<T>::length = 0;
        elems = nullptr;
    }

    java_array_single(int size)
    {
        if (size < 0)
        {
            throw std::runtime_error("negative array size");
        }
        else if (size == 0)
        {
            java_array_multi<T>::length = 0;
            elems = nullptr;
        }
        else
        {
            java_array_multi<T>::length = size;
            elems = new T[java_array_multi<T>::length];
        }
    }

    java_array_single(std::initializer_list<T> list)
    {
        java_array_multi<T>::length = list.size();
        elems = new T[java_array_multi<T>::length];
        std::copy(list.begin(), list.end(), elems);
    }

    T &operator[](int index) const
    {
        if (index < java_array_multi<T>::length && index >= 0)
        {
            cout << "access single " << index << endl;
            return elems[index];
        }
        throw std::runtime_error(format("array index out of bounds exception: index=%d size=%d", index, java_array_multi<T>::length));
    }

    java_array_single<T> operator=(std::initializer_list<T> rhs)
    {
        java_array_multi<T>::length = rhs.size();
        elems = new T[java_array_multi<T>::length];
        std::copy(rhs.begin(), rhs.end(), elems);
        return this;
    }

    template <typename... Args>
    static std::string format(const std::string &format, Args... args)
    {
        size_t size = snprintf(nullptr, 0, format.c_str(), args...) + 1; // Extra space for '\0'
        std::unique_ptr<char[]> buf(new char[size]);
        snprintf(buf.get(), size, format.c_str(), args...);
        return std::string(buf.get(), buf.get() + size - 1); // We don't want the '\0' inside
    }
};
