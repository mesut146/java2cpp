#pragma once

#include <string>

namespace java {
    namespace nio {

        class ByteOrder {
        public:
            static ByteOrder *LITTLE_ENDIAN_;
            static ByteOrder *BIG_ENDIAN_;
            std::string name;

            explicit ByteOrder(std::string&&);

            std::string* toString();
        }; //class ByteOrder

    } //namespace java
} //namespace nio
