#pragma once


namespace java {
    namespace nio {

        class Buffer {
//fields
        public:
            static int SPLITERATOR_CHARACTERISTICS;
            long address;
            int capacity_renamed;
            int limit_renamed;
            int mark_renamed;
            int position_renamed;

//methods
        public:
            Buffer(int mark, int pos, int lim, int cap);

            //virtual java::lang::Object *array() = 0;

            virtual int arrayOffset() = 0;

            virtual bool hasArray() = 0;

            virtual bool isDirect() = 0;

            int capacity();

            static void checkBounds(int, int, int);

            int checkIndex(int);

            int checkIndex(int, int);

            Buffer *clear();

            void discardMark();

            Buffer *flip();

            bool hasRemaining();

            virtual bool isReadOnly() = 0;

            int limit();

            Buffer *limit(int);

            Buffer *mark();

            int markValue();

            int nextGetIndex();

            int nextGetIndex(int);

            int nextPutIndex();

            int nextPutIndex(int nb);

            int position();

            Buffer *position(int);

            int remaining();

            Buffer *reset();

            Buffer *rewind();

            void truncate();


        };//class Buffer

    }//namespace java
}//namespace nio
