#pragma once

#include <string>
#include "java/io/InputStreamReader.h"

namespace java {
    namespace io {

        class FileReader : public InputStreamReader {
        public:
            explicit FileReader(std::string *path);
        };//class FileReader

    }//namespace java
}//namespace io
