#pragma once

#include <algorithm>
#include <string>
#include <vector>
#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"
#include "jre/java/util/List.h"
#include "jre/java/util/ArrayList.h"

namespace java{
namespace util{

class Arrays {
public:
    template <typename T>
    static List<T>* asList(std::vector<T>* values) {
        List<T>* list = new ArrayList<T>();
        if (values) for (T value : *values) list->add(value);
        return list;
    }

    template <typename T>
    static std::vector<T>* copyOf(std::vector<T>* values, int length) {
        auto* result = new std::vector<T>();
        if (values) for (int i = 0; i < length; ++i) {
            result->push_back(i < static_cast<int>(values->size()) ? values->at(i) : T());
        }
        return result;
    }

    template <typename T>
    static void fill(std::vector<T>* values, T value) {
        if (values) std::fill(values->begin(), values->end(), value);
    }

    template <typename T>
    static bool equals(std::vector<T>* a, std::vector<T>* b) {
        if (a == b) return true;
        if (!a || !b || a->size() != b->size()) return false;
        for (size_t i = 0; i < a->size(); ++i) {
            T x = a->at(i), y = b->at(i);
            if (x == y) continue;
            if (x && x->equals(y)) continue;
            return false;
        }
        return true;
    }
};

}//namespace util
}//namespace java
