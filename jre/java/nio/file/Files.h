#pragma once

#include <filesystem>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include "jre/java/lang/String.h"
#include "jre/java/nio/file/Path.h"
#include "jre/java/util/List.h"
#include "jre/java/util/ArrayList.h"

namespace java{
namespace nio{
namespace file{

class OpenOption;
class LinkOption;
namespace attribute { template <typename T> class FileAttribute; }

class Files {
public:
    static bool exists(Path* path, std::vector<LinkOption*>*) {
        return path && std::filesystem::exists(path->native());
    }
    static Path* createDirectories(Path* path,
            std::vector<attribute::FileAttribute<java::lang::Object*>*>*) {
        if (path) std::filesystem::create_directories(path->native());
        return path;
    }
    static Path* writeString(Path* path, java::lang::String* text,
            std::vector<OpenOption*>*) {
        if (!path) return path;
        std::ofstream out(path->native(), std::ios::binary);
        if (text) out.write(text->utf8()->data(), static_cast<std::streamsize>(text->utf8()->size()));
        return path;
    }
    static java::lang::String* readString(Path* path) {
        if (!path) return new java::lang::String(new std::string());
        std::ifstream in(path->native(), std::ios::binary);
        std::ostringstream ss;
        ss << in.rdbuf();
        return new java::lang::String(new std::string(ss.str()));
    }
    static long size(Path* path) {
        if (!path) return 0;
        std::error_code ec;
        return static_cast<long>(std::filesystem::file_size(path->native(), ec));
    }
    static java::util::List<java::lang::String*>* readAllLines(Path* path) {
        auto* lines = new java::util::ArrayList<java::lang::String*>();
        if (!path) return lines;
        std::ifstream in(path->native());
        std::string line;
        while (std::getline(in, line)) {
            if (!line.empty() && line.back() == '\r') line.pop_back();
            lines->add(new java::lang::String(new std::string(line)));
        }
        return lines;
    }
    static bool deleteIfExists(Path* path) {
        if (!path) return false;
        std::error_code ec;
        return std::filesystem::remove(path->native(), ec);
    }
};

}//namespace file
}//namespace nio
}//namespace java
