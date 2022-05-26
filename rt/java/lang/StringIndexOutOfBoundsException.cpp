#include "StringIndexOutOfBoundsException.h"


StringIndexOutOfBoundsException::StringIndexOutOfBoundsException(int i) : java::lang::Exception(
        "index out of bound: " + std::to_string(i)) {

}

StringIndexOutOfBoundsException::StringIndexOutOfBoundsException() = default;
