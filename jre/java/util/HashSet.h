#pragma once

#include <vector>
#include "jre/java/util/Set.h"

namespace java{
namespace util{

template <typename E>
class HashSet : public Set<E> {
    std::vector<E> values_;
    bool same(java::lang::Object* a, java::lang::Object* b) {
        if (!a || !b) return a == b;
        return a->equals(b);
    }
public:
    HashSet() {}
    bool add(E value) override {
        for (E existing : values_) if (same(existing, value)) return false;
        values_.push_back(value);
        return true;
    }
    bool remove(java::lang::Object* value) override {
        for (auto it = values_.begin(); it != values_.end(); ++it) {
            if (same(*it, value)) { values_.erase(it); return true; }
        }
        return false;
    }
    bool contains(java::lang::Object* value) override {
        for (E existing : values_) if (same(existing, value)) return true;
        return false;
    }
    int size() override { return static_cast<int>(values_.size()); }
    bool isEmpty() override { return values_.empty(); }
    void clear() override { values_.clear(); }
};

}//namespace util
}//namespace java
