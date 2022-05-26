#pragma once

#include "java/lang/Throwable.h"

namespace java {
    namespace lang {

        class Exception : public Throwable {
            //methods
        public:
            Exception();

            explicit Exception(std::string *s);

            explicit Exception(std::string &&s);

            explicit Exception(const String &s);

            explicit Exception(Throwable *ex);

            Exception(std::string *s, Throwable *ex);

        }; //class Exception

    } //namespace java
} //namespace lang
