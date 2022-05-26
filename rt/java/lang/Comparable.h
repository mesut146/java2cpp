#pragma once


namespace java {
    namespace lang {

/*interface*/

        template<typename T>
        class Comparable {
//methods
        public:
            virtual int compareTo(T) = 0;


        };//class Comparable

    }//namespace java
}//namespace lang
