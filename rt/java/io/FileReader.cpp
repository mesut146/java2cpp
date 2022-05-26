#include "FileInputStream.h"
#include "FileReader.h"

using namespace java::io;

FileReader::FileReader(std::string *path) : InputStreamReader(new FileInputStream(path)) {}
