#include <iostream>

template <class T>
class lin_array
{
public:
    int length;
    T *elems;
    int* dims;
    int dim;

    lin_array()
    {
        length = 0;
        elems = nullptr;
        dims={0};
    }

    lin_array(int size)
    {
        if (size < 0)
        {
            throw "negative array size";
        }
        else if (size == 0)
        {
            length = 0;
            elems = nullptr;
            dims={0};
        }
        else
        {
            length = size;
            elems = new T[length];
            dims={length};
        }
        dim=1;
    }

    lin_array(std::initializer_list<T> list)
    {
        length = list.size();
        elems = new T[length];
        std::copy(list.begin(), list.end(), elems);
    }

    lin_array(T list[], int size)
    {
        if (size < 0)
        {
            throw "negative array size";
        }
        length = size;
        elems = new T[length];
        dims={size};
        dim=1;
        for (int i = 0; i < length; i++)
        {
            elems[i] = list[i];
        }
    }

    int getLength(int d){
        return dims[d];
    }

    void initDims(int size[], int n)
    {
        dim=n;
        dims=size;
        length=1;
        for(int i:dims){
            length*=i;
        }
        /*2,3,4
        {{{0,1,2,3},{4,5,6,7},{8,9,10,11}},{{12,13,14,15},{16,17,18,19},{20,21,22,23}}
        */
    }

    T &operator[](int index) const
    {
        if (index < length && index >= 0)
        {
            return elems[index];
        }
        throw "array index out of bounds exception";
    }

    lin_array<T> operator=(std::initializer_list<T> rhs)
    {
        length = rhs.size();
        elems = new T[length];
        std::copy(rhs.begin(), rhs.end(), elems);
        return *this;
    }
};
