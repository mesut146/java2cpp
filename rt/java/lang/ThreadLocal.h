#pragma once

#include <thread>

namespace java {
    namespace lang {


        template<typename T>
        class ThreadLocal {
        public:
            int threadLocalHashCode;
            T val;

            virtual T initialValue() {
                return nullptr;
            }

            ThreadLocal() = default;


            /*void createMap(std::thread::id t, T firstValue) {
                auto a = map().find(t)->second;
                if (a == nullptr) {
                    a = new ThreadLocalMap(this, firstValue);
                    map().insert({t, a});
                }
            }*/

            T get() {
                if (val == nullptr) {
                    val = initialValue();
                }
                return val;
                /*auto id = std::this_thread::get_id();
                ThreadLocalMap *map = getMap(id);
                if (map != nullptr) {
                    ThreadLocalMap::Entry *e = map->getEntry(this);
                    if (e != nullptr)
                        return (T) e->value;
                }
                return setInitialValue();*/
            }

            /*ThreadLocalMap *getMap(std::thread::id id) {
                auto a = map().find(id)->second;
                return a;
            }*/

            //static int nextHashCode();

            void remove() {
                /*ThreadLocalMap *m = getMap(std::this_thread::get_id());
                if (m != nullptr)
                    m->remove(this);*/
            }

            /*void set(T value) {
                auto t = std::this_thread::get_id();
                ThreadLocalMap *map = getMap(t);
                if (map != nullptr)
                    map->set(this, value);
                else
                    createMap(t, value);
            }*/

            /*T setInitialValue() {
                T value = initialValue();
                auto t = std::this_thread::get_id();
                ThreadLocalMap *map = getMap(t);
                if (map != nullptr)
                    map->set(this, value);
                else
                    createMap(t, value);
                return value;
            }*/

        }; //class ThreadLocal

    } //namespace java
} //namespace lang
