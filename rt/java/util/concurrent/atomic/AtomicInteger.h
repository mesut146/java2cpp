#pragma once

#include <string>

namespace java {
    namespace util {
        namespace concurrent {
            namespace atomic {

                class AtomicInteger {
                public:
                    int value;

                public:
                    AtomicInteger();

                    AtomicInteger(int x);

                    int addAndGet(int);

                    bool compareAndSet(int, int);

                    int decrementAndGet();

                    double doubleValue();

                    float floatValue();

                    int get();


                    int getAndAdd(int);

                    int getAndDecrement();

                    int getAndIncrement();

                    int getAndSet(int);


                    int incrementAndGet();

                    int intValue();

                    void lazySet(int);

                    long longValue();

                    void set(int);

                    std::string *toString();


                    bool weakCompareAndSet(int, int);


                };//class AtomicInteger

            }//namespace java
        }//namespace util
    }//namespace concurrent
}//namespace atomic
