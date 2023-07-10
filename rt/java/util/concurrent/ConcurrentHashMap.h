#pragma once

namespace java {
    namespace util {
        namespace concurrent {

            template<typename K, typename V>
            class ConcurrentHashMap {

            public:

                ConcurrentHashMap();

                ConcurrentHashMap(int);

                ConcurrentHashMap(int, float);

                ConcurrentHashMap(int, float, int);

                void clear();

                static int compareComparables(java::lang::Class<java::lang::Object *> *, java::lang::Object *,
                                              java::lang::Object *);

                bool contains(java::lang::Object *);

                bool containsKey(java::lang::Object *);

                bool containsValue(java::lang::Object *);

                //java::util::Enumeration<V> *elements();

                //std::unordered_set<java::util::Entry<K, V> *> *entrySet();

                bool equals(java::lang::Object *);

                void fullAddCount(long, bool);

                V get(java::lang::Object *);

                V getOrDefault(java::lang::Object *, V);

                int hashCode();

                bool isEmpty();

                //KeySetView<K, V> *keySet();

                //KeySetView<K, V> *keySet(V);

                //std::vector<K> *keys();

                //long mappingCount();

                //V merge(K, V, java::util::function::BiFunction<java::lang::Object *, java::lang::Object *, java::lang::Object *> *);

                /*template <typename K>
                static KeySetView<K, java::lang::Boolean *> *newKeySet();*/

                /*template <typename K>
                static KeySetView<K, java::lang::Boolean *> *newKeySet(int);*/

                V put(K, V);

                //void putAll(java::util::Map<java::lang::Object *, java::lang::Object *> *);

                V putIfAbsent(K, V);

                //V putVal(K, V, bool);

                //void readObject(java::io::ObjectInputStream *);

                std::vector <V> *values();

                //void writeObject(java::io::ObjectOutputStream *);

            }; //class ConcurrentHashMap

        } //namespace java
    }     //namespace util
} //namespace concurrent
