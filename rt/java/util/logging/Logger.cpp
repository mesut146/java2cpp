#include <iostream>
#include "Logger.h"
#include "LogRecord.h"

using namespace java::util::logging;


Logger *& Logger::global() {
    auto static a= new Logger(new std::string("global"));
    return a;
}

int & Logger::offValue() {
    auto static a = Level::OFF()->intValue();
    return a;
}

std::string *& Logger::GLOBAL_LOGGER_NAME() {
    auto static a = new std::string("global");
    return a;
}


Logger *Logger::getAnonymousLogger() {
    return getAnonymousLogger(nullptr);
}

Logger *Logger::getAnonymousLogger(std::string *resourceBundleName) {
    auto *result = new Logger(nullptr, resourceBundleName, new std::string("log_caller"), false);
    result->anonymous = true;
    /*Logger *root = manager.getLogger("");
    result->doSetParent(root);*/
    return result;
}

Logger::Logger(std::string *name) {
    this->name = name;
    isSystemLogger = true;
    levelValue = Level::INFO()->intValue();
}

void Logger::log(Level *level, std::string *msg) {
    if (level->intValue() < levelValue || levelValue == offValue()) {
        return;
    }
    auto *lr = new LogRecord(level, msg);
    doLog(lr);
}

void Logger::doLog(java::util::logging::LogRecord *record) {
    doLog(record, nullptr);
}

void Logger::doLog(java::util::logging::LogRecord *lr, std::string *rbname) {
    lr->setLoggerName(name);
    if (rbname != nullptr) {
        //lr->setResourceBundleName(rbname);
        //lr->setResourceBundle(findResourceBundle(rbname, false));
    }
    log(lr);
}

void Logger::log(LogRecord *record) {
    if (record->getLevel()->intValue() < levelValue || levelValue == offValue()) {
        return;
    }
    /*Filter theFilter = filter;
    if (theFilter != null && !theFilter.isLoggable(record)) {
        return;
    }*/

    // Post the LogRecord to all our Handlers, and then to
    // our parents' handlers, all the way up the tree.

    Logger *logger = this;
    std::cout << "LOG." << *record->level->name << " = " << record->message << "\n";
    /*while (logger != null) {
        Handler[]
        loggerHandlers = isSystemLogger
                         ? logger.accessCheckedHandlers()
                         : logger.getHandlers();
        for (Handler handler : loggerHandlers) {
            handler.publish(record);
        }

        bool useParentHdls = isSystemLogger
                             ? logger->useParentHandlers
                             : logger->getUseParentHandlers();

        if (!useParentHdls) {
            break;
        }

        logger = isSystemLogger ? logger->parent : logger->getParent();
    }*/
}

Logger::Logger(std::string *name, std::string *resourceBundleName, std::string *caller, bool isSystemLogger) {
    this->name = name;
    this->isSystemLogger = isSystemLogger;
}
