#include <string>
#include <java/lang/Exception.h>
#include <java/lang/Throwable.h>
#include <iostream>


#include "java/lang/RuntimeException.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/IllegalStateException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"
#include "java/lang/NullPointerException.h"
#include "java/lang/UnsupportedOperationException.h"
#include "java/io/EOFException.h"
#include "java/io/UTFDataFormatException.h"

using namespace java::lang;
using namespace java::io;

RuntimeException::RuntimeException(std::string *s) : Exception(s) {

}

RuntimeException::RuntimeException(std::string &&s) : Exception(&s) {

}

RuntimeException::RuntimeException(Throwable *t) : Exception(t) {

}

RuntimeException::RuntimeException(std::string *msg, Throwable *e) : Exception(msg, e) {

}

RuntimeException::RuntimeException(const String &s) : Exception(s) {

}


RuntimeException::RuntimeException() = default;

//---------------------------
Exception::Exception(std::string *s) : Throwable(s) {

}

Exception::Exception(Throwable *e) : Throwable(e) {

}

Exception::Exception(std::string *msg, Throwable *e) : Throwable(msg, e) {

}

Exception::Exception(std::string &&s) : Throwable(&s) {

}

Exception::Exception(const String &s) : Throwable(s) {

}

Exception::Exception() = default;

//------------------------

IndexOutOfBoundsException::IndexOutOfBoundsException() = default;

IndexOutOfBoundsException::IndexOutOfBoundsException(std::string *) {

}
//------------------------

IllegalArgumentException::IllegalArgumentException() = default;

IllegalArgumentException::IllegalArgumentException(std::string *s) : RuntimeException(s) {

}

IllegalArgumentException::IllegalArgumentException(std::string &&msg) : RuntimeException(&msg) {

}

IllegalArgumentException::IllegalArgumentException(const String &s) : RuntimeException(s) {

}

//-----------------
IllegalStateException::IllegalStateException(std::string *s) : RuntimeException(s) {

}

IllegalStateException::IllegalStateException(std::string &&s) : RuntimeException(&s) {}

//----------------
Throwable::Throwable() = default;

Throwable::Throwable(std::string *s) {
    this->message = s;
}

Throwable::Throwable(std::string &&s) {
    this->message = &s;
}

Throwable::Throwable(const String &s) {
    this->message = s.ptr();
}

Throwable::Throwable(std::string *s, Throwable *) {
    this->message = s;
}

Throwable::Throwable(Throwable *ex) {
    this->message = ex->message;
}

std::string *Throwable::getMessage() const {
    return message;
}

void Throwable::printStackTrace() const {
    if (message == nullptr) {
        std::cerr << "errrr";
        return;
    }
    std::cerr << *message << "\n";
}

const char *Throwable::what() const noexcept {
    return message->data();
}


//-------

ArrayIndexOutOfBoundsException::ArrayIndexOutOfBoundsException(std::string *s) : RuntimeException(s) {

}

ArrayIndexOutOfBoundsException::ArrayIndexOutOfBoundsException(int index) : RuntimeException(
        "index=" + std::to_string(index)) {

}

ArrayIndexOutOfBoundsException::ArrayIndexOutOfBoundsException() = default;

//-------------
NullPointerException::NullPointerException() = default;

NullPointerException::NullPointerException(std::string *msg) : RuntimeException(msg) {

}

NullPointerException::NullPointerException(std::string &&msg) : RuntimeException(&msg) {

}

//----------------
EOFException::EOFException() = default;

//----------------------
UnsupportedOperationException::UnsupportedOperationException() = default;

UnsupportedOperationException::UnsupportedOperationException(std::string *msg) : RuntimeException(msg) {}

UnsupportedOperationException::UnsupportedOperationException(String &msg) : RuntimeException(msg) {

}

UnsupportedOperationException::UnsupportedOperationException(String &&msg) : RuntimeException(msg) {

}

//----------------
UTFDataFormatException::UTFDataFormatException(std::string *msg) : IOException(msg) {}

//------------------
IOException::IOException() = default;

IOException::IOException(std::string *msg) : java::lang::Exception(msg) {}