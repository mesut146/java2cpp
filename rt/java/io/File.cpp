#include <string>
#include <vector>
#include "File.h"
#include "CppHelper.h"
#include <fstream>

using namespace java::io;

std::string *File::pathSeparator() {
    return new std::string(":");
}

std::string *File::separator() {
    return new std::string("/");
}

File::File(std::string *s) {
    this->path = s;
    this->prefixLength = 0;
}

std::string *File::getName() const {
    auto idx = path->rfind(separatorChar);
    if (idx < prefixLength) {
        return new std::string(path->substr(prefixLength));
    }
    return new std::string(path->substr(idx + 1));
}

std::string *File::getPath() const {
    return path;
}

int File::hashCode() const {
    return std::hash<std::string>{}(*path);
}

std::string *File::toString() const {
    return path;
}

bool File::equals(java::lang::Object *obj) const {
    if (obj != nullptr && instanceof<File>(obj)) {
        return compareTo((File *) obj) == 0;
    }
    return false;
}

int File::compareTo(File *other) const {
    auto s = other->path;
    if (*path == *s) return 0;
    return path->compare(*s);
}

bool File::exists() {
    std::ifstream f(*path);
    bool res = f.is_open();
    f.close();
    return res;
}

std::string *File::getParent() const {
    auto idx = path->rfind(separatorChar);
    if (idx < prefixLength) {
        if (prefixLength > 0 && (path->size() > prefixLength)) {
            return new std::string(path->substr(0, prefixLength));
        }
        return nullptr;
    }
    return new std::string(path->substr(0, idx + 1));
}

bool File::isDirectory() {
    return false;
}

bool File::isFile() {
    return false;
}

bool File::canRead() {
    std::ifstream f(*path);
    bool res = f.is_open();
    f.close();
    return res;
}

long File::length() {
    return 0;
}

File::File(File *parent, std::string *name) {
    path = new std::string(*parent->path + *separator() + *name);
}


std::vector<File *> *File::listFiles() {
    return nullptr;
}

long File::lastModified() {
    return 0;
}

File *File::getParentFile() const {
    return new File(getParent());
}

void File::mkdirs() {

}

void File::createNewFile() {

}
