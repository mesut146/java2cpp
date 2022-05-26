#pragma once

#include "CppHelper.h"
#include <string>
#include "java/lang/StringBuilder.h"
#include "java/util/regex/Pattern.h"

namespace java {
    namespace util {

        class Formatter {
        public:
            static int MAX_FD_CHARS;
            //java::lang::Appendable *a;
            java::lang::StringBuilder sb;
            static std::string *formatSpecifier;
            static java::util::regex::Pattern *fsPattern;
            //Locale *l;
            //java::io::IOException *lastException;
            static double scaleUp;
            wchar_t zero = L'0';

        public:
            Formatter();

            template<typename ...args>
            void format(std::string *str, args ...a) {
                sb.append(CppHelper::format(str, a...));
            }

            //Formatter(java::lang::Appendable *);

            //Formatter(Locale *);

            //Formatter(std::string *);

            //Formatter(java::io::File *);

            //Formatter(java::io::PrintStream *);

            // Formatter(java::io::OutputStream *);

            //Formatter(Locale *, java::lang::Appendable *);

            //Formatter(java::lang::Appendable *, Locale *);

            // Formatter(std::string *, std::string *);

            //Formatter(java::io::File *, std::string *);

            //Formatter(java::io::OutputStream *, std::string *);


            //Formatter(std::string *, std::string *, Locale *);

            //Formatter(java::io::File *, std::string *, Locale *);

            //Formatter(java::io::OutputStream *, std::string *, Locale *);

            //static void checkText(std::string *, int, int);

            void close();

            //void ensureOpen();

            //void flush();

            //Formatter *format(std::string *, std::vector<java::lang::Object *> *);


            // java::io::IOException *ioException();


            //java::lang::Appendable *out();

            //std::vector<FormatString *> *parse(std::string *);


            std::string *toString();


        };//class Formatter

    }//namespace java
}//namespace util
