#pragma once


namespace java {
    namespace io {

/*interface*/
        class DataInput {
        public:
            virtual int readUnsignedShort() = 0;

            virtual void readFully(std::vector<char> *arr, int off, int len) = 0;
        };//class DataInput

    }//namespace java
}//namespace io
