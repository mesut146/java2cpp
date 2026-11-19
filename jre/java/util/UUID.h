#pragma once

#include <cstdio>
#include <string>
#include "jre/java/lang/String.h"
namespace java{ namespace util {
class UUID : public java::lang::Object {
    std::string value_;
public:
    explicit UUID(std::string v) : value_(v) {}
    java::lang::String* toString() override { return new java::lang::String(new std::string(value_)); }
    static UUID* randomUUID() {
        char buf[64];
        std::snprintf(buf, sizeof(buf), "%08x-%04x-4%03x-%04x-%012llx",
                      static_cast<unsigned>(std::rand()),
                      static_cast<unsigned>(std::rand() & 0xffff),
                      static_cast<unsigned>(std::rand() & 0x0fff),
                      static_cast<unsigned>((std::rand() & 0x3fff) | 0x8000),
                      (static_cast<unsigned long long>(std::rand()) << 32 | static_cast<unsigned>(std::rand()))
                              & 0xFFFFFFFFFFFFULL);
        return new UUID(std::string(buf));
    }
};
}}
