#pragma once

#include "Exception.h"

class StringIndexOutOfBoundsException : public java::lang::Exception {
public:
    StringIndexOutOfBoundsException();

    explicit StringIndexOutOfBoundsException(int i);
};

