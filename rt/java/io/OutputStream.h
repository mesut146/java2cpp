#pragma once

#include <vector>

namespace java {
    namespace io {

        class OutputStream {
//methods
        public:
            //OutputStream();

            virtual void close();

            virtual void write(int) = 0;

            virtual void write(std::vector<char> *, int, int);

            virtual void flush();

            virtual void write(std::vector<char> *);


        };//class OutputStream

    }//namespace java
}//namespace io
