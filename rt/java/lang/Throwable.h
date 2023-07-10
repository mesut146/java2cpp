#pragma once

#include <string>
#include <String.h>
#include <stdexcept>

namespace java {
    namespace lang {

        class Throwable : std::exception {
        public:
            std::string *message = nullptr;

            Throwable();

            explicit Throwable(std::string *s);

            explicit Throwable(std::string &&s);

            explicit Throwable(const String& s);

            explicit Throwable(Throwable *);

            Throwable(std::string *, Throwable *);

            //Throwable(std::string *, Throwable *, bool, bool);

            std::string *getMessage() const;

            void printStackTrace() const;

            const char *what() const noexcept override;

        }; //class Throwable

    } //namespace java
} //namespace lang
