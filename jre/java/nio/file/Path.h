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
    Path* getParent() { return new Path(path_.parent_path().string()); }
    Path* resolve(Path* other) {
        return new Path(other ? (path_ / other->native()).string() : path_.string());
    }
    Path* resolve(java::lang::String* other) {
        return new Path(other ? (path_ / std::filesystem::path(*other->utf8())).string() : path_.string());
    }
    java::lang::String* toString() override {
        return new java::lang::String(new std::string(path_.string()));
    }
};

}//namespace file
}//namespace nio
}//namespace java
