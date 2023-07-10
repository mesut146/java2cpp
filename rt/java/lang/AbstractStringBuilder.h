#pragma once

namespace java {
    namespace lang {

        class AbstractStringBuilder {
            //fields
        public:
            static const int MAX_ARRAY_SIZE=1000;
            int count;
            std::vector<wchar_t> *value;

            //methods
        public:
            AbstractStringBuilder();

            explicit AbstractStringBuilder(int);

            //virtual AbstractStringBuilder *append(Object *);

            virtual AbstractStringBuilder *append(std::string *);

            //virtual AbstractStringBuilder *append(StringBuffer *);


            virtual AbstractStringBuilder *append(std::vector<wchar_t> *);

            virtual AbstractStringBuilder *append(bool);

            virtual AbstractStringBuilder *append(wchar_t);

            virtual AbstractStringBuilder *append(int);

            virtual AbstractStringBuilder *append(long);

            virtual AbstractStringBuilder *append(float);

            virtual AbstractStringBuilder *append(double);

            //virtual AbstractStringBuilder *append(CharSequence *, int, int);

            virtual AbstractStringBuilder *append(std::vector<wchar_t> *, int, int);

            //virtual AbstractStringBuilder *appendCodePoint(int);

            virtual AbstractStringBuilder *delete_renamed(int, int);

            //virtual AbstractStringBuilder *deleteCharAt(int);

            virtual int indexOf(std::string *);

            virtual int indexOf(std::string *, int);

            //virtual AbstractStringBuilder *insert(int, Object *);

            //virtual AbstractStringBuilder *insert(int, std::string *);

            //virtual AbstractStringBuilder *insert(int, std::vector<wchar_t> *);

            //virtual AbstractStringBuilder *insert(int, CharSequence *);

            //virtual AbstractStringBuilder *insert(int, bool);

            //virtual AbstractStringBuilder *insert(int, wchar_t);

            //virtual AbstractStringBuilder *insert(int, int);

            //virtual AbstractStringBuilder *insert(int, long);

            //virtual AbstractStringBuilder *insert(int, float);

            //virtual AbstractStringBuilder *insert(int, double);

            //virtual AbstractStringBuilder *insert(int, std::vector<wchar_t> *, int, int);

            //virtual AbstractStringBuilder *insert(int, CharSequence *, int, int);

            virtual int lastIndexOf(std::string * s);

            virtual int lastIndexOf(std::string *s, int off);

            //virtual AbstractStringBuilder *replace(int, int, std::string *);

            //virtual AbstractStringBuilder *reverse();

            virtual std::string *toString() = 0;

            //virtual AbstractStringBuilder *append(AbstractStringBuilder *);

            virtual int capacity();

            virtual wchar_t charAt(int);

            //virtual int codePointAt(int);

            //virtual int codePointBefore(int);

            //virtual int codePointCount(int, int);

            // virtual void ensureCapacity(int);

            // virtual void getChars(int, int, std::vector<wchar_t> *, int);

            virtual int length();

            // virtual int offsetByCodePoints(int, int);

            //virtual void setCharAt(int, wchar_t);

            virtual void setLength(int newLength);

            // virtual CharSequence *subSequence(int, int);

            virtual std::string *substring(int);

            virtual std::string *substring(int, int);

            // virtual void trimToSize();

            //AbstractStringBuilder *appendNull();

             void ensureCapacityInternal(int);

             void expandCapacity(int);

            //std::vector<wchar_t> *getValue();

            // int hugeCapacity(int);

            //int newCapacity(int);

            // void reverseAllValidSurrogatePairs();

        }; //class AbstractStringBuilder

    } //namespace java
} //namespace lang
