#pragma once

#include "java/io/OutputStream.h"
#include "java/io/InputStream.h"

namespace java {
    namespace lang {

        class System {
            //fields
        public:
            static java::io::PrintStream *err();
            //static java::io::InputStream *in();
            static java::io::PrintWriter *out();

            static int identityHashCode(void *ptr);

            static long nanoTime() {
                return 0;
            }
        }; //class System

    } //namespace java
} //namespace lang
