#pragma once

#include <filesystem>
#include <string>
#include <vector>
#include "jre/java/lang/Object.h"
#include "jre/java/lang/String.h"

namespace java{
namespace io{

class File : public java::lang::Object {
    std::filesystem::path path_;
public:
    explicit File(java::lang::String* path)
        : path_(path ? std::filesystem::path(*path->utf8()) : std::filesystem::path()) {}

    bool exists() { return std::filesystem::exists(path_); }
    bool isDirectory() { return std::filesystem::is_directory(path_); }
    java::lang::String* getName() {
        return new java::lang::String(new std::string(path_.filename().string()));
    }
    java::lang::String* getParent() {
        return new java::lang::String(new std::string(path_.parent_path().string()));
    }
    bool renameTo(File* target) {
        if (!target) return false;
        std::error_code ec;
        std::filesystem::rename(path_, target->path_, ec);
        return !ec;
    }
    bool delete_renamed() {
        std::error_code ec;
        return std::filesystem::remove(path_, ec);
    }
    std::vector<File*>* listFiles() {
        auto* result = new std::vector<File*>();
        std::error_code ec;
        for (const auto& entry : std::filesystem::directory_iterator(path_, ec)) {
            result->push_back(new File(new java::lang::String(
                    new std::string(entry.path().string()))));
        }
        return result;
    }
};

}//namespace io
}//namespace java
