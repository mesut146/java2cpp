#pragma once


namespace java {
    namespace util {

        template<typename E>
        class EnumSet {

        public:
            EnumSet(java::lang::Class<E> *, std::vector<java::lang::Enum<java::lang::Object *> *> *);

            virtual void addAll() = 0;


            static EnumSet<E> *allOf(java::lang::Class<E> *);

            EnumSet<E> *clone();

            virtual void complement() = 0;


            static EnumSet<E> *of(E);


            static EnumSet<E> *of(E, E);


            static EnumSet<E> *of(E, std::vector<E> *);


            static EnumSet<E> *of(E, E, E);


            static EnumSet<E> *of(E, E, E, E);


            static EnumSet<E> *of(E, E, E, E, E);


            static EnumSet<E> *range(E, E);


        };//class EnumSet

    }//namespace java
}//namespace util
