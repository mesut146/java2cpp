#include <string>
#include "Object.h"

using namespace java::lang;

Object::~Object() = default;

bool Object::equals(Object *other) {
    return this == other;
}

std::string *Object::toString() {
    return new std::string("@" + std::string(typeid(this).name()) + std::to_string(hashCode()));
}

int Object::hashCode() {
    return std::hash<long>{}(reinterpret_cast<long >(this));
}
