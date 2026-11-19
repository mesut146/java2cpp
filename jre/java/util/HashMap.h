#pragma once

#include <utility>
#include <vector>
#include "jre/java/util/Map.h"

namespace java{
namespace util{

template <typename K, typename V>
class HashMap : public Map<K, V> {
    std::vector<std::pair<K, V>> entries_;

    bool same(java::lang::Object* a, java::lang::Object* b) {
        if (a == nullptr || b == nullptr) return a == b;
        return a->equals(b);
    }

public:
    HashMap() {}

    V put(K key, V value) override {
        for (auto& entry : entries_) {
            if (same(entry.first, key)) {
                V old = entry.second;
                entry.second = value;
                return old;
            }
        }
        entries_.emplace_back(key, value);
        return V();
    }

    V get(java::lang::Object* key) override {
        for (auto& entry : entries_) {
            if (same(entry.first, key)) return entry.second;
        }
        return V();
    }

    V getOrDefault(java::lang::Object* key, V fallback) override {
        for (auto& entry : entries_) {
            if (same(entry.first, key)) return entry.second;
        }
        return fallback;
    }

    V putIfAbsent(K key, V value) override {
        for (auto& entry : entries_) {
            if (same(entry.first, key)) return entry.second;
        }
        entries_.emplace_back(key, value);
        return V();
    }

    V remove(java::lang::Object* key) override {
        for (auto it = entries_.begin(); it != entries_.end(); ++it) {
            if (same(it->first, key)) {
                V old = it->second;
                entries_.erase(it);
                return old;
            }
        }
        return V();
    }

    bool remove(java::lang::Object* key, java::lang::Object* value) override {
        for (auto it = entries_.begin(); it != entries_.end(); ++it) {
            if (same(it->first, key) && same(it->second, value)) {
                entries_.erase(it);
                return true;
            }
        }
        return false;
    }

    bool containsKey(java::lang::Object* key) override {
        for (auto& entry : entries_) if (same(entry.first, key)) return true;
        return false;
    }

    bool containsValue(java::lang::Object* value) override {
        for (auto& entry : entries_) if (same(entry.second, value)) return true;
        return false;
    }

    int size() override { return static_cast<int>(entries_.size()); }
    bool isEmpty() override { return entries_.empty(); }
    void clear() override { entries_.clear(); }
};

}//namespace util
}//namespace java
