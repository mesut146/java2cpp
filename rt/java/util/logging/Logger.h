#pragma once

#include <string>
#include "Level.h"
#include "LogRecord.h"

namespace java {
    namespace util {
        namespace logging {

            class Logger {
//fields
            public:
                static Logger *&global();
                static std::string *&GLOBAL_LOGGER_NAME();
                static int& offValue();
                static std::string *SYSTEM_LOGGER_RB_NAME;
                //static LoggerBundle *NO_RESOURCE_BUNDLE;
                //static LoggerBundle *SYSTEM_BUNDLE;
                bool anonymous;
                //java::util::Locale *catalogLocale;
                std::string *catalogName;
                bool isSystemLogger;
                Level *levelObject;
                int levelValue;
                //LoggerBundle *loggerBundle;
                std::string *name;
                Logger *parent;
                //static java::lang::Object *treeLock;
                bool useParentHandlers;

//methods
            public:
                explicit Logger(std::string *name);

                Logger(std::string *name, std::string *resourceBundleName);

                Logger(std::string *name, std::string *resourceBundleName, std::string *caller, bool isSystemLogger);

                void checkPermission();

                void config(std::string *);


                //static Logger *demandLogger(std::string *, std::string *, java::lang::Class<java::lang::Object *> *);


                void doSetParent(Logger *);

                void entering(std::string *, std::string *);

                //void entering(std::string *, std::string *, java::lang::Object *);

                //void entering(std::string *, std::string *, std::vector<java::lang::Object *> *);

                void exiting(std::string *, std::string *);

                //void exiting(std::string *, std::string *, java::lang::Object *);


                void fine(std::string *);


                void finest(std::string *);


                static Logger *getAnonymousLogger();

                static Logger *getAnonymousLogger(std::string *);


                //LoggerBundle *getEffectiveLoggerBundle();


                static Logger *getGlobal();


                Level *getLevel();

                static Logger *getLogger(std::string *);

                static Logger *getLogger(std::string *, std::string *);

                std::string *getName();

                Logger *getParent();

                static Logger *getPlatformLogger(std::string *);


                std::string *getResourceBundleName();

                bool getUseParentHandlers();

                void info(std::string *);


                bool isLevelInitialized();

                bool isLoggable(Level *);


                void log(Level *, std::string *);


                //void log(Level *, std::string *, java::lang::Object *);

                //void log(Level *, std::string *, std::vector<java::lang::Object *> *);

                //void log(Level *, std::string *, java::lang::Throwable *);


                void logp(Level *, std::string *, std::string *, std::string *);


                //void logp(Level *, std::string *, std::string *, std::string *, java::lang::Object *);

                //void logp(Level *, std::string *, std::string *, std::string *, std::vector<java::lang::Object *> *);

                //void logp(Level *, std::string *, std::string *, std::string *, java::lang::Throwable *);


                void logrb(Level *, std::string *, std::string *, std::string *, std::string *);

                //void logrb(Level *, std::string *, std::string *, std::string *, std::string *, java::lang::Object *);

                /*void logrb(Level *, std::string *, std::string *, std::string *, std::string *,
                           std::vector<java::lang::Object *> *);*/

                /*void
                logrb(Level *, std::string *, std::string *, std::string *, std::string *, java::lang::Throwable *);*/


                //void setCallersClassLoaderRef(java::lang::Class<java::lang::Object *> *);


                void setLevel(Level *);

                void setParent(Logger *);


                void setUseParentHandlers(bool);

                //void setupResourceInfo(std::string *, java::lang::Class<java::lang::Object *> *);

                void severe(std::string *);

                //void throwing(std::string *, std::string *, java::lang::Throwable *);

                void updateEffectiveLevel();

                void warning(std::string *);


                void doLog(java::util::logging::LogRecord *record);

                void doLog(java::util::logging::LogRecord *record, std::string *rbname);

                void log(LogRecord *lr);
            };//class Logger

        }//namespace java
    }//namespace util
}//namespace logging
