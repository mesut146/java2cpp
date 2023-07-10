#pragma once

#include <vector>
#include <string>
#include "java/lang/Object.h"

namespace java {
    namespace util {

        class BitSet : java::lang::Object {
        public:
            static int ADDRESS_BITS_PER_WORD;
            static int BITS_PER_WORD;
            static int BIT_INDEX_MASK;
            static unsigned long WORD_MASK;
            bool sizeIsSticky = false;
            std::vector<long> *words;
            int wordsInUse = 0;

        public:
            BitSet();

            explicit BitSet(int);

            //BitSet(std::vector<long> *);

            ~BitSet() override;

            //void and_renamed(BitSet *);

            void andNot(BitSet *);

            int cardinality() const;

            void checkInvariants() const;

            static void checkRange(int, int);

            void clear();

            void clear(int);

            void clear(int, int);

            BitSet *clone();

            void ensureCapacity(int);

            //bool equals(java::lang::Object *);

            void expandTo(int);

           // void flip(int);

            //void flip(int, int);

            bool get(int);

            BitSet *get(int, int);

            int hashCode() override;

            void initWords(int);

            //bool intersects(BitSet *);

            bool isEmpty() const;

            int length() const;

            int nextClearBit(int);

            int nextSetBit(int) const;

            void or_renamed(BitSet *);

            //int previousClearBit(int);

            //int previousSetBit(int);

            //void readObject(java::io::ObjectInputStream *);

            void recalculateWordsInUse();

            void set(int);

            void set(int, bool);

            void set(int, int);

            void set(int, int, bool);

            int size();

            //std::vector<char> *toByteArray();

            //std::vector<long> *toLongArray();

            std::string *toString() override;

            void trimToSize();

            //static BitSet *valueOf(std::vector<long> *);

            //static BitSet *valueOf(java::nio::LongBuffer *);

            //static BitSet *valueOf(java::nio::ByteBuffer *);

            static int wordIndex(int);

            //void writeObject(java::io::ObjectOutputStream *);

            //void xor_renamed(BitSet *);

        }; //class BitSet

    } //namespace java
} //namespace util
