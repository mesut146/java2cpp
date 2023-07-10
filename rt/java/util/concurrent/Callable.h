#pragma once


namespace java {
    namespace util {
        namespace concurrent {

/*interface*/

            template<typename V>
            class Callable {
                virtual V call() = 0;
            };//class Callable

        }//namespace java
    }//namespace util
}//namespace concurrent
