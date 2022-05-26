#include <java/lang/NullPointerException.h>
#include "Level.h"

using namespace java::util::logging;
using namespace java::lang;

Level *& Level::OFF() {
    auto static a = new Level(new std::string("OFF"), INT32_MAX, defaultBundle());
    return a;
}
Level *& Level::INFO() {
    auto static a = new Level(new std::string("INFO"), 800, defaultBundle());
    return a;
}
Level *& Level::FINER() {
    auto static a = new Level(new std::string("FINER"), 400, defaultBundle());
    return a;
}

//Level *Level::SEVERE = new Level(new std::string("SEVERE"), 1000, defaultBundle);
//Level *Level::WARNING = new Level(new std::string("WARNING"), 900, defaultBundle);
//Level *Level::CONFIG = new Level(new std::string("CONFIG"), 700, defaultBundle);
//Level *Level::FINE = new Level(new std::string("FINE"), 500, defaultBundle);
//Level *Level::FINEST = new Level(new std::string("FINEST"), 300, defaultBundle);
//Level *Level::ALL = new Level(new std::string("ALL"), INT32_MIN, defaultBundle);

std::string *&Level::defaultBundle() {
    auto static a = new std::string("sun.util.logging.resources.logging");
    return a;
}

int Level::intValue() {
    return value;
}

Level::Level(std::string *name, int value) : Level(name, value, nullptr) {
}

Level::Level(std::string *name, int value, std::string *resourceBundleName) {
    if (name == nullptr) {
        throw new NullPointerException();
    }
    this->name = name;
    this->value = value;
    this->resourceBundleName = resourceBundleName;
    //this.localizedLevelName = resourceBundleName == null ? name : null;
    //this.cachedLocale = null;
    //KnownLevel.add(this);
}