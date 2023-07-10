#pragma once

#include <vector>
#include <string>
#include "java/lang/AbstractStringBuilder.h"

namespace java {
    namespace lang {

        class StringBuffer : public AbstractStringBuilder {
            //fields
        public:
            std::vector<wchar_t> *toStringCache;

            //methods
        public:
            StringBuffer();

            StringBuffer(int);

            StringBuffer(std::string *);

            virtual ~StringBuffer();

            //StringBuffer *append(Object *);

            StringBuffer *append(std::string *);

            StringBuffer *append(StringBuffer *);

            //StringBuffer *append(AbstractStringBuilder *);


            StringBuffer *append(std::vector<wchar_t> *);

            StringBuffer *append(bool);

            StringBuffer *append(wchar_t);

            StringBuffer *append(int);

            StringBuffer *append(long);

            StringBuffer *append(float);

            StringBuffer *append(double);

            StringBuffer *append(std::vector<wchar_t> *, int, int);

            //StringBuffer *appendCodePoint(int);

            //int capacity();

            wchar_t charAt(int);

           // int codePointAt(int);

            //int codePointBefore(int);

           // int codePointCount(int, int);

            StringBuffer *delete_renamed(int, int);

            //StringBuffer *deleteCharAt(int);

            //void ensureCapacity(int);

            //void getChars(int, int, std::vector<wchar_t> *, int);

            int indexOf(std::string *);

            int indexOf(std::string *, int);

            //StringBuffer *insert(int, Object *);

            StringBuffer *insert(int, std::string *);

            StringBuffer *insert(int, std::vector<wchar_t> *);

            //StringBuffer *insert(int, CharSequence *);

            StringBuffer *insert(int, bool);

            StringBuffer *insert(int, wchar_t);

            StringBuffer *insert(int, int);

            StringBuffer *insert(int, long);

            StringBuffer *insert(int, float);

            StringBuffer *insert(int, double);

            StringBuffer *insert(int, std::vector<wchar_t> *, int, int);

            //StringBuffer *insert(int, CharSequence *, int, int);

            //int lastIndexOf(std::string *);

            //int lastIndexOf(std::string *, int);

            int length();

            //int offsetByCodePoints(int, int);

            StringBuffer *replace(int, int, std::string *);

            //StringBuffer *reverse();

            //void setCharAt(int, wchar_t);

            //void setLength(int);

            //CharSequence *subSequence(int, int);

            std::string *substring(int);

            std::string *substring(int, int);

            std::string *toString();

            //void trimToSize();

        }; //class StringBuffer

    } //namespace java
} //namespace lang
