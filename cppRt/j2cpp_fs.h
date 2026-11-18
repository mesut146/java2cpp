#pragma once

//java.io.File / java.nio.file.Files -> std::filesystem
#include <filesystem>
#include <fstream>
#include <iterator>
#include <string>
#include <vector>

namespace CppHelper {

inline std::filesystem::path *fsPath(std::string *s) {
    return new std::filesystem::path(s ? *s : std::string());
}
inline std::filesystem::path *fsPathPair(std::string *a, std::string *b) {
    std::filesystem::path p(a ? *a : std::string());
    if (b) p /= *b;
    return new std::filesystem::path(p);
}
inline std::string *pathString(const std::filesystem::path &p) {
    return new std::string(p.string());
}
inline std::string *fsPathString(std::filesystem::path *p) {
    return p ? pathString(*p) : new std::string();
}
inline std::string *fsFileName(std::filesystem::path *p) {
    return p ? pathString(p->filename()) : new std::string();
}
inline std::string *fsParent(std::filesystem::path *p) {
    return p ? pathString(p->parent_path()) : new std::string();
}
inline std::string *fsAbsolute(std::filesystem::path *p) {
    if (!p) return new std::string();
    std::error_code ec;
    return pathString(std::filesystem::absolute(*p, ec));
}
inline bool fsExists(std::filesystem::path *p) {
    return p && std::filesystem::exists(*p);
}
inline bool fsIsDirectory(std::filesystem::path *p) {
    return p && std::filesystem::is_directory(*p);
}
inline bool fsIsRegularFile(std::filesystem::path *p) {
    return p && std::filesystem::is_regular_file(*p);
}
inline bool fsCreateDir(std::filesystem::path *p) {
    if (!p) return false;
    std::error_code ec;
    return std::filesystem::create_directories(*p, ec);
}
inline bool fsCreateFile(std::filesystem::path *p) {
    if (!p) return false;
    std::ofstream f(*p);
    return f.good();
}
inline bool fsRemove(std::filesystem::path *p) {
    if (!p) return false;
    std::error_code ec;
    return std::filesystem::remove(*p, ec);
}
inline bool fsRename(std::filesystem::path *from, std::filesystem::path *to) {
    if (!from || !to) return false;
    std::error_code ec;
    std::filesystem::rename(*from, *to, ec);
    return !ec;
}
inline long fsFileSize(std::filesystem::path *p) {
    if (!p) return 0;
    std::error_code ec;
    return static_cast<long>(std::filesystem::file_size(*p, ec));
}
inline std::vector<std::filesystem::path *> *fsList(std::filesystem::path *p) {
    auto result = new std::vector<std::filesystem::path *>();
    if (!p) return result;
    std::error_code ec;
    for (const auto &entry : std::filesystem::directory_iterator(*p, ec)) {
        result->push_back(new std::filesystem::path(entry.path()));
    }
    return result;
}
inline std::string *fsReadString(std::filesystem::path *p) {
    if (!p) return new std::string();
    std::ifstream f(*p, std::ios::binary);
    return new std::string(std::istreambuf_iterator<char>(f), std::istreambuf_iterator<char>());
}
inline bool fsWriteString(std::filesystem::path *p, std::string *s) {
    if (!p) return false;
    std::ofstream f(*p, std::ios::binary);
    if (s) f.write(s->data(), static_cast<std::streamsize>(s->size()));
    return f.good();
}
inline std::vector<std::string *> *fsReadAllLines(std::filesystem::path *p) {
    auto result = new std::vector<std::string *>();
    if (!p) return result;
    std::ifstream f(*p);
    std::string line;
    while (std::getline(f, line)) {
        if (!line.empty() && line.back() == '\r') line.pop_back();
        result->push_back(new std::string(line));
    }
    return result;
}

} //namespace CppHelper
