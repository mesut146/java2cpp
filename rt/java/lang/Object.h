#pragma once

#include <string>

namespace java {
    namespace lang {

        class Object {
            //methods
        public:
            virtual ~Object();

            virtual std::string *toString();

            //Object();

            virtual bool equals(Object *);

            virtual int hashCode();

            //virtual Object *clone();

            //virtual void finalize();

            //void notify();

            //void notifyAll();

            //void wait();

            //void wait(long);

            //void wait(long, int);

        }; //class Object

    } //namespace java
} //namespace lang
