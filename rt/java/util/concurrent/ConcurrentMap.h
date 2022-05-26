#pragma once

namespace java
{
    namespace util
    {
        namespace concurrent
        {

            /*interface*/

            template <typename K, typename V>
            class ConcurrentMap : public java::util::Map<K, V>
            {
                //methods
            public:
                virtual V getOrDefault(java::lang::Object *, V) = 0;

                virtual V putIfAbsent(K, V) = 0;

                virtual bool remove(java::lang::Object *, java::lang::Object *) = 0;

                virtual V replace(K, V) = 0;

                virtual bool replace(K, V, V) = 0;

            }; //class ConcurrentMap

        } //namespace java
    }     //namespace util
} //namespace concurrent
