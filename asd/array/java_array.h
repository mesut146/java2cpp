#include <iostream>
#include <vector>
#include <string>
#include <memory>
#include <typeinfo>
#include <typeindex>

using namespace std;

template <typename T, typename B = T>
class java_array
{
public:
    int length;
    T *elems;
    java_array *map;

    java_array()
    {
        length = 0;
        elems = nullptr;
    }

    java_array(int size)
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
            map = new java_array[length];
        }
    }

    java_array(std::initializer_list<T> list)
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

    java_array(int *size, int n)
    {
        cout << "init n=" << n << " size=" << size[0] << endl;
        length = size[0];
        elems = new T[length];
        if (n == 1)
        {
            //already initialized
            //elems = new T[length];
            return;
        }
        else if (n == 2)
        {
            //elems = new java_array<>[length];
            for (int i = 0; i < length; i++)
            {
                cout << "type=" << typeid(T).name() << endl;
                //cout<<"elem="<<elems[i]<<endl;
                //elems[i]=java_array<B>(size,1);
            }
        }
    }

    bool isArray()
    {
        return string(typeid(T).name()).find("java_array") != string::npos;
    }

    bool isSingleArray()
    {
        return string(typeid(T).name()).find("java_array_single") != string::npos;
    }

    void init(int *sizes, int n)
    {
        cout << "n=" << n << endl;
        cout << "t is=" << typeid(T).name() << endl;
        //sizes++;
        length = sizes[0];
        elems = new T[length];
        if (n > 2)
        {
            //T obj(3);

            //cout << "obj=" << obj.length << endl;
            //obj.init(sizes + 1, n - 1);
            //map[0]=java_array
            for (int i = 0; i < length; i++)
            {
                elems[i] = T(sizes[1]);
                elems[i].init(sizes + 1, n - 1);
            }
        }
        else
        {
            cout << "t is prim" << endl;
            for (int i = 0; i < length; i++)
            {
                elems[i] = T(sizes[1]);
            }
        }
    }

    T &operator[](int index) const
    {
        if (index < length && index >= 0)
        {
            return elems[index];
        }
        throw std::runtime_error(format("array index out of bounds exception: index=%d size=%d", index, length));
    }

    java_array<T> operator=(std::initializer_list<T> rhs)
    {
        length = rhs.size();
        elems = new T[length];
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