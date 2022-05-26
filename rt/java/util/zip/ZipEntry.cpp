#include "ZipEntry.h"

using namespace java::util::zip;

std::string *ZipEntry::getName() {
    return name;
}

bool ZipEntry::isDirectory() {
    return false;
}

long ZipEntry::getTime() {
    return 0;
}