#pragma once

#include "java/io/IOException.h"

namespace java
{
    namespace io
    {

        class UTFDataFormatException : public IOException
        {
        public:
            explicit UTFDataFormatException(std::string *msg) ;
        }; //class UTFDataFormatException

    } //namespace java
} //namespace io
