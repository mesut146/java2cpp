#pragma once

#include <vector>
#include <string>
#include "java/io/Reader.h"
#include "InputStream.h"

namespace java {
    namespace io {

        class InputStreamReader : public Reader {
            InputStream *in;
        public:
            explicit InputStreamReader(InputStream *in);

            //InputStreamReader(InputStream *in, std::string *);

            void close() override;

            //std::string *getEncoding();

            int read() override;

            int read(std::vector<wchar_t> *, int, int) override;

            bool ready() override;


        };//class InputStreamReader

    }//namespace java
}//namespace io
