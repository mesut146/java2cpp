#pragma once

#include <iostream>
#include "jre/java/io/PrintStream.h"

namespace java{
namespace lang{

class System {
public:
    inline static java::io::PrintStream* out = new java::io::PrintStream(std::cout);
    inline static java::io::PrintStream* err = new java::io::PrintStream(std::cerr);
};

}//namespace lang
}//namespace java
