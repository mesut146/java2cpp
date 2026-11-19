#pragma once

#include <string>
#include <vector>
#include "jre/java/lang/String.h"
#include "jre/java/nio/file/Path.h"

namespace java{
namespace nio{
namespace file{

class Paths {
public:
    static Path* get(java::lang::String* first, std::vector<java::lang::String*>* more) {
        std::string value = first ? *first->utf8() : std::string();
        if (more) for (java::lang::String* part : *more) {
            if (!part) continue;
            if (!value.empty() && value.back() != '/') value += '/';
            value += *part->utf8();
        }
        return new Path(value);
    }
};

}//namespace file
}//namespace nio
}//namespace java
