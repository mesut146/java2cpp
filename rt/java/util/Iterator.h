#pragma once

namespace java {
    namespace util {

        template<class T>
        struct It {
        public:
            T *m_ptr;

            It();

            T &operator*() const { return *m_ptr; }

            bool operator!=(const It<T> other) const { return *m_ptr != other.operator*(); }

            const It operator++(int) {
                It tmp = *this;
                ++(*this);
                return tmp;
            }
        };

        /*interface*/
        template<typename E>
        class Iterator {
        public:
            virtual bool hasNext() = 0;

            virtual E next() = 0;

            virtual void remove() = 0;

            It<E> begin();

            It<E> end();

        }; //class Iterator

    } //namespace java
} //namespace util
