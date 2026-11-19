#pragma once

#include <filesystem>
#include <string>
#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"

namespace java{
namespace nio{
namespace file{

class Path : public java::lang::Object {
    std::filesystem::path path_;
public:
    explicit Path(std::string value) : path_(value) {}
    const std::filesystem::path& native() const { return path_; }
    java::lang::String* toString() override {
        return new java::lang::String(new std::string(path_.string()));
    }
};

}//namespace file
}//namespace nio
}//namespace java
