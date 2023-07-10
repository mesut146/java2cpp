#pragma once

#include <map>
#include <string>
#include "coll.h"

template<class T>
int hash0(T t) {
    return t->hashCode();
}

/*template<>
int hash0<int>(int t) {
    return t;
}*/

template<class K, class V>
class TreeMap {
    typedef std::map<K, V, maps::comp<K>> type;

public:


    int hashCode() {
        int hash = 0;
        for (auto it:map) {
            hash = hash * 31 + hash0<K>(it.first) + hash0<V>(it.second);
        }
        return hash;
    }

    bool equals(TreeMap<K, V> *other) {
        if (size() != other->size()) {
            return false;
        }
        auto it1 = map.begin();
        auto it2 = other->map.begin();
        while (it1 != map.end()) {
            if (it1 != it2) return false;
            it1++;
            it2++;
        }
        return true;
    }

    bool containsKey(K &key) {
        auto it = map.find(key);
        return it != map.end();
    }

    std::vector<V> *values() {
        auto v = new std::vector<V>();
        for (auto e:map) {
            v->push_back(e.second);
        }
        return v;
    }

    void put(const K &key, V &val) {
        auto it = map.find(key);
        if (it == map.end()) {
            map.insert({key, val});
        } else {
            it->second = val;
        }
    }

    V get0(K key) {
        for (auto e:map) {
            if (e.first == key) return e.second;
            if (e.first->compareTo(key) == 0) {
                return e.second;
            }
        }
        return (V) nullptr;
    }

    V get1(K key) {
        auto it = map.find(key);
        if (it == map.end()) {
            return (V) nullptr;
        }
        return it->second;
    }

    V get(K key) {
        if (key == nullptr) {
            return nullptr;
        }
        return get0(key);
    }

    V getStr(K key) {
        return get1(key);
    }

    int size() {
        return map.size();
    }

    bool empty() {
        return map.empty();
    }

    void erase(K &key) {
        map.erase(key);
    }

    std::vector<K> *keySet() {
        auto v = new std::vector<K>();
        for (auto it:map) {
            v->push_back(it.first);
        }
        return v;
    }

    typename type::iterator pollFirstEntry() {
        auto it = map.begin();
        map.erase(it);
        return it;
    }

    type map;
};