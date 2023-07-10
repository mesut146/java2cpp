#pragma once

#include "java/lang/Exception.h"

namespace java {
    namespace io {

        class IOException : public java::lang::Exception {
            //methods
        public:
            IOException();

            explicit IOException(std::string *);

        }; //class IOException

    } //namespace java
} //namespace io
