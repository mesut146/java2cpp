#pragma once

#include "java/util/Iterator.h"

namespace java {
    namespace lang {

        /*interface*/

        template<typename T>
        class Iterable {
        public:
            virtual java::util::Iterator<T> *iterator() = 0;
        }; //class Iterable

    } //namespace java
} //namespace lang
