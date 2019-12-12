#include <iostream>

template <class T, class B = T>
class java_array
{
public:
    int length;
    T *elems;

    java_array()
    {
        length = 0;
        elems = nullptr;
    }

    java_array(int size)
    {
        if (size < 0)
        {
            throw "negative array size";
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
        }
    }

    java_array(std::initializer_list<T> list)
    {
        length = list.size();
        elems = new T[length];
        std::copy(list.begin(), list.end(), elems);
    }

    java_array(T list[], int size)
    {
        if (size < 0)
        {
            throw "negative array size";
        }
        length = size;
        elems = new T[length];
        //T elems[length];
        for (int i = 0; i < length; i++)
        {
            elems[i] = list[i];
        }
    }

    void initDims(int size[], int n,int xx)
    {
        //skip first
        //lvl becomes 0 init int otherwise call initDims recursively
        length = size[0];
        if (n == 1)
        {
            //already initialized
            elems = new T[length];
            return;
        }
        else if (n == 2)
        {
            elems = new T[length];
            //elems=new java_array<B>[length];
            for (int i = 0; i < length; i++)
            {
                elems[i] = java_array<B, B>(size[1]);
            }
            return;
        }
        /*else if (n == 3)
        {
            elems = new T[length];
            //elems = new java_array<java_array<B>>[length];
            for (int i = 0; i < length; i++)
            {
                elems[i] = java_array<java_array<B>>(size[1]);
                for (int j = 0; j < size[1]; j++)
                {
                    elems[i][j] = java_array<B>(size[2]);
                }
            }
        }*/
        else
        {
            elems=new T[length];
            int size2[n - 1];
            for (int i = 0; i < n - 1; i++)
            {
                size2[i] = size[i + 1];
            }
            for (int i = 0; i < length; i++)
            {
                elems[i].initDims(size2, n - 1,xx);
            }
        }
    }

    T &operator[](int index) const
    {
        if (index < length && index >= 0)
        {
            return elems[index];
        }
        throw "array index out of bounds exception";
    }

    java_array<T> operator=(std::initializer_list<T> rhs)
    {
        length = rhs.size();
        elems = new T[length];
        std::copy(rhs.begin(), rhs.end(), elems);
        return *this;
    }
};
