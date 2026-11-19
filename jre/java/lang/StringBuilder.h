#pragma once

#include <string>
#include "jre/java/lang/String.h"

namespace java{
namespace lang{

class StringBuilder : public Object {
    std::string value_;
public:
    StringBuilder() {}
    explicit StringBuilder(String* s) { if (s) value_ = *s->utf8(); }

    StringBuilder* append(String* s) { if (s) value_ += *s->utf8(); return this; }
    StringBuilder* append(const std::string& s) { value_ += s; return this; }
    StringBuilder* append(int v) { value_ += std::to_string(v); return this; }
    StringBuilder* append(long v) { value_ += std::to_string(v); return this; }
    StringBuilder* append(bool v) { value_ += v ? "true" : "false"; return this; }
    StringBuilder* append(Object* o) { return append(o ? o->toString() : new String(new std::string("null"))); }
    StringBuilder* append(wchar_t c) { value_.push_back(static_cast<char>(c)); return this; }

    int length() { return static_cast<int>(value_.size()); }
    String* toString() override { return new String(new std::string(value_)); }
};

}//namespace lang
}//namespace java
