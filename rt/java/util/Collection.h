#pragma once

#include "java/lang/Iterable.h"

namespace java
{
    namespace util
    {

        /*interface*/

        template <typename E>
        class Collection : public java::lang::Iterable<E>
        {
            //methods
        public:
            virtual bool add(E) = 0;

            virtual bool addAll(Collection<java::lang::Object *> *) = 0;

            virtual void clear() = 0;

            virtual bool contains(java::lang::Object *) = 0;

            virtual bool containsAll(Collection<java::lang::Object *> *) = 0;

            virtual bool equals(java::lang::Object *) = 0;

            virtual int hashCode() = 0;

            virtual bool isEmpty() = 0;

            virtual Iterator<E> *iterator() = 0;

            virtual bool remove(java::lang::Object *) = 0;

            virtual bool removeAll(Collection<java::lang::Object *> *) = 0;

            virtual bool retainAll(Collection<java::lang::Object *> *) = 0;

            virtual int size() = 0;

            virtual Spliterator<E> *spliterator() = 0;

            virtual std::vector<java::lang::Object *> *toArray() = 0;

            template <typename T>
            virtual std::vector<T> *toArray(std::vector<T> *) = 0;

        }; //class Collection

    } //namespace java
} //namespace util
