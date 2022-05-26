#pragma once

#include <string>
#include <vector>
#include "java/lang/Object.h"
#include "java/lang/AbstractStringBuilder.h"

namespace java {
    namespace lang {

        class StringBuilder : public AbstractStringBuilder {

            //methods
        public:
            StringBuilder();

            explicit StringBuilder(int);

            explicit StringBuilder(std::string *);

            //StringBuilder(CharSequence *);

            //StringBuilder *append(Object *);

            StringBuilder *append(std::string *) override;

            //StringBuilder *append(StringBuffer *);

            //StringBuilder *append(CharSequence *);

            StringBuilder *append(std::vector<wchar_t> *) override;

            StringBuilder *append(bool) override;

            StringBuilder *append(wchar_t) override;

            StringBuilder *append(int) override;

            StringBuilder *append(long) override;

            StringBuilder *append(float) override;

            StringBuilder *append(double) override;

          //  StringBuilder *append(CharSequence *, int, int);

            StringBuilder *append(std::vector<wchar_t> *, int, int) override;

          //  StringBuilder *appendCodePoint(int);

            //StringBuilder *delete_renamed(int, int);

           // StringBuilder *deleteCharAt(int);

            //int indexOf(std::string *);

            //int indexOf(std::string *, int);

            //StringBuilder *insert(int, Object *);

            //StringBuilder *insert(int, std::string *);

            //StringBuilder *insert(int, std::vector<wchar_t> *);

            //StringBuilder *insert(int, CharSequence *);

            //StringBuilder *insert(int, bool);

            //StringBuilder *insert(int, wchar_t);

            //StringBuilder *insert(int, int);

            //StringBuilder *insert(int, long);

            //StringBuilder *insert(int, float);

            //StringBuilder *insert(int, double);

            //StringBuilder *insert(int, std::vector<wchar_t> *, int, int);

            //StringBuilder *insert(int, CharSequence *, int, int);

            //int lastIndexOf(std::string *);

            //int lastIndexOf(std::string *, int);

            //void readObject(java::io::ObjectInputStream* );

            //StringBuilder *replace(int, int, std::string *);

           // StringBuilder *reverse();

            std::string *toString() override;

            //void writeObject(java::io::ObjectOutputStream* );

        }; //class StringBuilder

    } //namespace java
} //namespace lang
