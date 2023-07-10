#pragma once

#include "Level.h"
#include <string>

namespace java {
    namespace util {
        namespace logging {
            class LogRecord {
            public:
                Level *level;
                std::string *loggerName;
                std::string *message;
                std::string *resourceBundleName;

                LogRecord(java::util::logging::Level *level, std::__cxx11::basic_string<char> *msg);

                void setLoggerName(std::string *name);

                Level *getLevel();
            };

        }
    }
}