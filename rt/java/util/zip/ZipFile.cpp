#include "ZipFile.h"
#include <stdexcept>

using namespace java::util::zip;

ZipFile::ZipFile(java::io::File *file) {
    name = file->getPath();
}

std::vector<ZipEntry *> *ZipFile::entries() {
    throw std::runtime_error("ZipFile::entries()");
}

java::io::InputStream *ZipFile::getInputStream(ZipEntry *ze) {
    throw std::runtime_error("ZipFile::getInputStream()");
}

void ZipFile::close() {}

ZipEntry *ZipFile::getEntry(std::string *name) {
    throw std::runtime_error("ZipFile::getEntry()");
}