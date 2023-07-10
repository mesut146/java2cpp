#pragma once

#include "java/lang/Object.h"

namespace java {
    namespace util {

        /*interface*/

        template<typename T>
        class Comparator : public java::lang::Object {
        public:
            virtual int compare(T, T) = 0;
        }; //class Comparator

    } //namespace java
} //namespace util
