#pragma once

#include <string>

namespace java {
    namespace util {
        namespace logging {

            class Level {
            public:
                static Level *FINE;
                static Level *&FINER();
                static Level *FINEST;
                static Level *&INFO();
                static Level *&OFF();
                static Level *ALL;
                static Level *CONFIG;
                static Level *SEVERE;
                static Level *WARNING;

                std::string *name;
                std::string *resourceBundleName;
                static std::string *&defaultBundle();
                int value;

                int intValue();

                Level(std::string *name, int value, std::string *resourceBundleName);

                Level(std::string *name, int value);
            };//class Level

        }//namespace java
    }//namespace util
}//namespace logging
