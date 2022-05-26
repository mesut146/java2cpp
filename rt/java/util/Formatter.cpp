#include "Formatter.h"

using namespace java::util;

Formatter::Formatter() = default;


void Formatter::close() {
}

std::string *Formatter::toString() {
    return sb.toString();
}