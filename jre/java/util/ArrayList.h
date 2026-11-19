#pragma once

#include <vector>
#include "jre/java/util/List.h"

namespace java{
namespace util{

template <typename E>
class ArrayList : public List<E> {
    std::vector<E> values_;
public:
    ArrayList() {}

    bool add(E value) override { values_.push_back(value); return true; }
    void add(int index, E value) override {
        if (index < 0) index = 0;
        if (index > static_cast<int>(values_.size())) index = static_cast<int>(values_.size());
        values_.insert(values_.begin() + index, value);
    }
    int size() override { return static_cast<int>(values_.size()); }
    bool isEmpty() override { return values_.empty(); }
    E get(int index) override {
        if (index < 0 || index >= static_cast<int>(values_.size())) return E();
        return values_[index];
    }
    E set(int index, E value) override {
        if (index < 0 || index >= static_cast<int>(values_.size())) return E();
        E old = values_[index];
        values_[index] = value;
        return old;
    }
    E remove(int index) override {
        if (index < 0 || index >= static_cast<int>(values_.size())) return E();
        E old = values_[index];
        values_.erase(values_.begin() + index);
        return old;
    }
    bool remove(java::lang::Object* value) override {
        for (auto it = values_.begin(); it != values_.end(); ++it) {
            if ((*it == nullptr && value == nullptr)
                    || (*it && (*it)->equals(value))) {
                values_.erase(it);
                return true;
            }
        }
        return false;
    }
    int indexOf(java::lang::Object* value) override {
        for (int i = 0; i < static_cast<int>(values_.size()); ++i) {
            E item = values_[i];
            if ((item == nullptr && value == nullptr) || (item && item->equals(value))) return i;
        }
        return -1;
    }
    int lastIndexOf(java::lang::Object* value) override {
        for (int i = static_cast<int>(values_.size()) - 1; i >= 0; --i) {
            E item = values_[i];
            if ((item == nullptr && value == nullptr) || (item && item->equals(value))) return i;
        }
        return -1;
    }
    bool contains(java::lang::Object* value) override { return indexOf(value) >= 0; }
    std::vector<E>* toArray(std::vector<E>* target) override {
        std::vector<E>* result = target ? target : new std::vector<E>();
        *result = values_;
        return result;
    }
    void clear() override { values_.clear(); }
};

}//namespace util
}//namespace java
