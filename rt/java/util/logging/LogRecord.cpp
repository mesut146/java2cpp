#include "LogRecord.h"

void java::util::logging::LogRecord::setLoggerName(std::string *loggerName) {
    this->loggerName = loggerName;
}

java::util::logging::Level *java::util::logging::LogRecord::getLevel() {
    return level;
}

java::util::logging::LogRecord::LogRecord(java::util::logging::Level *level, std::__cxx11::basic_string<char> *msg) {
    this->level = level;
    this->message = msg;
}
