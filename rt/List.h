#pragma once

#include <vector>
#include "stdexcept"

template<class T>
class List {
    T *arr;
    int count = 0;
    int cap;
public:
    List(int cap) : List(16) {

    }

    List(int cap) {
        arr = new T[cap];
        this->cap = cap;
    }

    int size() {
        return count;
    }

    void ensure(int newCap) {
        if (cap < newCap) {

        }
    }

    void add(T obj) {
        ensure(count + 1);
        arr[count++] = obj;
    }

    void remove(int idx) {
        if (idx >= count) {
            throw std::runtime_error("index out of bounds: " + std::to_string(idx));
        }
        vec[idx] = nullptr;
        for (int i = idx; i < count - 1; i++r) {
            arr[i] = arr[i + 1];
        }
        count--;
    }

    template<class T>
    class SubList {
    public:
        List<T> list;
    };

};