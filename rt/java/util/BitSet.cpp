#include <cassert>
#include <java/lang/StringBuilder.h>
#include "BitSet.h"
#include "java/lang/IndexOutOfBoundsException.h"
#include "java/lang/Long.h"
#include "java/lang/NegativeArraySizeException.h"
#include "CppHelper.h"

using namespace java::lang;
using namespace java::util;

unsigned long BitSet::WORD_MASK = 0xffffffffffffffffL;
int BitSet::ADDRESS_BITS_PER_WORD = 6;
int BitSet::BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
int BitSet::BIT_INDEX_MASK = BITS_PER_WORD - 1;

BitSet::BitSet() {
    initWords(BITS_PER_WORD);
    sizeIsSticky = false;
}

BitSet::BitSet(int nbits) {
    // nbits can't be negative; size 0 is OK
    if (nbits < 0)
        throw NegativeArraySizeException(new std::string("nbits < 0: " + std::to_string(nbits)));

    initWords(nbits);
    sizeIsSticky = true;
}

void BitSet::initWords(int nbits) {
    words = new std::vector<long>(wordIndex(nbits - 1) + 1);
}

int BitSet::wordIndex(int bitIndex) {
    return bitIndex >> ADDRESS_BITS_PER_WORD;
}

void BitSet::recalculateWordsInUse() {
    // Traverse the bitset until a used word is found
    int i;
    for (i = wordsInUse - 1; i >= 0; i--)
        if ((*words)[i] != 0)
            break;

    wordsInUse = i + 1; // The new logical size
}

bool BitSet::get(int bitIndex) {
    if (bitIndex < 0)
        throw IndexOutOfBoundsException(new std::string("bitIndex < 0: " + std::to_string(bitIndex)));

    checkInvariants();

    int wordIndex0 = wordIndex(bitIndex);
    return (wordIndex0 < wordsInUse)
           && (((*words)[wordIndex0] & (1L << bitIndex)) != 0);
}

BitSet *BitSet::get(int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);

    checkInvariants();

    int len = length();

    // If no set bits in range return empty bitset
    if (len <= fromIndex || fromIndex == toIndex)
        return new BitSet(0);

    // An optimization
    if (toIndex > len)
        toIndex = len;

    auto *result = new BitSet(toIndex - fromIndex);
    int targetWords = wordIndex(toIndex - fromIndex - 1) + 1;
    int sourceIndex = wordIndex(fromIndex);
    bool wordAligned = ((fromIndex & BIT_INDEX_MASK) == 0);

    // Process all words but the last word
    for (int i = 0; i < targetWords - 1; i++, sourceIndex++)
        (*result->words)[i] = wordAligned ? (*words)[sourceIndex] :
                              ((*words)[sourceIndex] >> fromIndex) |
                              ((*words)[sourceIndex + 1] << -fromIndex);

    // Process the last word
    long lastWordMask = WORD_MASK >> -toIndex;
    (*result->words)[targetWords - 1] =
            ((toIndex - 1) & BIT_INDEX_MASK) < (fromIndex & BIT_INDEX_MASK)
            ? /* straddles source words */
            (((*words)[sourceIndex] >> fromIndex) |
             ((*words)[sourceIndex + 1] & lastWordMask) << -fromIndex)
            :
            (((*words)[sourceIndex] & lastWordMask) >> fromIndex);

    // Set wordsInUse correctly
    result->wordsInUse = targetWords;
    result->recalculateWordsInUse();
    result->checkInvariants();

    return result;
}

void BitSet::set(int bitIndex) {
    if (bitIndex < 0)
        throw IndexOutOfBoundsException(new std::string("bitIndex < 0: " + std::to_string(bitIndex)));

    int wordIndex0 = wordIndex(bitIndex);
    expandTo(wordIndex0);

    (*words)[wordIndex0] |= (1L << bitIndex); // Restores invariants

    checkInvariants();
}

int BitSet::length() const {
    if (wordsInUse == 0)
        return 0;

    return BITS_PER_WORD * (wordsInUse - 1) +
           (BITS_PER_WORD - Long::numberOfLeadingZeros((*words)[wordsInUse - 1]));
}

bool BitSet::isEmpty() const {
    return wordsInUse == 0;
}

template<class T>
std::vector<T> *clone(std::vector<T> *arr) {
    auto res = new std::vector<T>(arr->begin(), arr->end());
    return res;
}

BitSet *BitSet::clone() {
    if (!sizeIsSticky)
        trimToSize();

    try {
        auto *result = new BitSet(*this);
        result->words = new std::vector<long>(words->begin(), words->end());
        result->checkInvariants();
        return result;
    } catch (Exception &e) {
        throw e;
    }
}

void BitSet::checkInvariants() const {
    assert(wordsInUse == 0 || (*words)[wordsInUse - 1] != 0);
    assert(wordsInUse >= 0 && wordsInUse <= words->size());
    assert(wordsInUse == words->size() || (*words)[wordsInUse] == 0);
}

int BitSet::nextSetBit(int fromIndex) const {
    if (fromIndex < 0)
        throw IndexOutOfBoundsException(new std::string("fromIndex < 0: " + std::to_string(fromIndex)));

    checkInvariants();

    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
        return -1;

    auto word = (*words)[u] & (WORD_MASK << fromIndex);

    while (true) {
        if (word != 0)
            return (u * BITS_PER_WORD) + Long::numberOfTrailingZeros(word);
        if (++u == wordsInUse)
            return -1;
        word = (*words)[u];
    }
}

int BitSet::cardinality() const {
    int sum = 0;
    for (int i = 0; i < wordsInUse; i++)
        sum += Long::bitCount((*words)[i]);
    return sum;
}

void BitSet::clear() {
    while (wordsInUse > 0)
        (*words)[--wordsInUse] = 0;
}


void BitSet::clear(int bitIndex) {
    if (bitIndex < 0)
        throw IndexOutOfBoundsException(new std::string("bitIndex < 0: " + std::to_string(bitIndex)));

    int wordIndex0 = wordIndex(bitIndex);
    if (wordIndex0 >= wordsInUse)
        return;

    (*words)[wordIndex0] &= ~(1L << bitIndex);

    recalculateWordsInUse();
    checkInvariants();
}

void BitSet::clear(int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);

    if (fromIndex == toIndex)
        return;

    int startWordIndex = wordIndex(fromIndex);
    if (startWordIndex >= wordsInUse)
        return;

    int endWordIndex = wordIndex(toIndex - 1);
    if (endWordIndex >= wordsInUse) {
        toIndex = length();
        endWordIndex = wordsInUse - 1;
    }

    long firstWordMask = WORD_MASK << fromIndex;
    long lastWordMask = WORD_MASK >> -toIndex;
    if (startWordIndex == endWordIndex) {
        // Case 1: One word
        (*words)[startWordIndex] &= ~(firstWordMask & lastWordMask);
    } else {
        // Case 2: Multiple words
        // Handle first word
        (*words)[startWordIndex] &= ~firstWordMask;

        // Handle intermediate words, if any
        for (int i = startWordIndex + 1; i < endWordIndex; i++)
            (*words)[i] = 0;

        // Handle last word
        (*words)[endWordIndex] &= ~lastWordMask;
    }

    recalculateWordsInUse();
    checkInvariants();
}

BitSet::~BitSet() = default;

void BitSet::trimToSize() {
    if (wordsInUse != words->size()) {
        words = copyOfRange(words, 0, wordsInUse);
        checkInvariants();
    }
}

void BitSet::checkRange(int fromIndex, int toIndex) {
    if (fromIndex < 0)
        throw IndexOutOfBoundsException(new std::string("fromIndex < 0: " + std::to_string(fromIndex)));
    if (toIndex < 0)
        throw IndexOutOfBoundsException(new std::string("toIndex < 0: " + std::to_string(toIndex)));
    if (fromIndex > toIndex)
        throw IndexOutOfBoundsException(new std::string("fromIndex: " + std::to_string(fromIndex) +
                                                        " > toIndex: " + std::to_string(toIndex)));
}

int BitSet::size() {
    return words->size() * BITS_PER_WORD;
}

void BitSet::expandTo(int wordIndex) {
    int wordsRequired = wordIndex + 1;
    if (wordsInUse < wordsRequired) {
        ensureCapacity(wordsRequired);
        wordsInUse = wordsRequired;
    }
}

void BitSet::set(int bitIndex, bool value) {
    if (value)
        set(bitIndex);
    else
        clear(bitIndex);
}

void BitSet::set(int fromIndex, int toIndex) {
    checkRange(fromIndex, toIndex);

    if (fromIndex == toIndex)
        return;

    // Increase capacity if necessary
    int startWordIndex = wordIndex(fromIndex);
    int endWordIndex = wordIndex(toIndex - 1);
    expandTo(endWordIndex);

    long firstWordMask = WORD_MASK << fromIndex;
    long lastWordMask = WORD_MASK >> -toIndex;
    if (startWordIndex == endWordIndex) {
        // Case 1: One word
        (*words)[startWordIndex] |= (firstWordMask & lastWordMask);
    } else {
        // Case 2: Multiple words
        // Handle first word
        (*words)[startWordIndex] |= firstWordMask;

        // Handle intermediate words, if any
        for (int i = startWordIndex + 1; i < endWordIndex; i++)
            (*words)[i] = WORD_MASK;

        // Handle last word (restores invariants)
        (*words)[endWordIndex] |= lastWordMask;
    }

    checkInvariants();
}

void BitSet::set(int fromIndex, int toIndex, bool value) {
    if (value)
        set(fromIndex, toIndex);
    else
        clear(fromIndex, toIndex);
}

int BitSet::nextClearBit(int fromIndex) {
    // Neither spec nor implementation handle bitsets of maximal length.
    // See 4816253.
    if (fromIndex < 0)
        throw IndexOutOfBoundsException(new std::string("fromIndex < 0: " + std::to_string(fromIndex)));

    checkInvariants();

    int u = wordIndex(fromIndex);
    if (u >= wordsInUse)
        return fromIndex;

    long word = ~(*words)[u] & (WORD_MASK << fromIndex);

    while (true) {
        if (word != 0)
            return (u * BITS_PER_WORD) + Long::numberOfTrailingZeros(word);
        if (++u == wordsInUse)
            return wordsInUse * BITS_PER_WORD;
        word = ~(*words)[u];
    }
}

void BitSet::or_renamed(BitSet *set) {
    if (this == set)
        return;

    int wordsInCommon = std::min(wordsInUse, set->wordsInUse);

    if (wordsInUse < set->wordsInUse) {
        ensureCapacity(set->wordsInUse);
        wordsInUse = set->wordsInUse;
    }

    // Perform logical OR on words in common
    for (int i = 0; i < wordsInCommon; i++)
        (*words)[i] |= (*set->words)[i];

    // Copy any remaining words
    if (wordsInCommon < set->wordsInUse)
        arraycopy(set->words, wordsInCommon,
                  words, wordsInCommon,
                  wordsInUse - wordsInCommon);

    // recalculateWordsInUse() is unnecessary
    checkInvariants();
}

void BitSet::andNot(BitSet *set) {
    // Perform logical (a & !b) on words in common
    for (int i = std::min(wordsInUse, set->wordsInUse) - 1; i >= 0; i--)
        (*words)[i] &= ~(*set->words)[i];

    recalculateWordsInUse();
    checkInvariants();
}

void BitSet::ensureCapacity(int wordsRequired) {
    if (words->size() < wordsRequired) {
        // Allocate larger of doubled size or required size
        int request = std::max(2 * (int) words->size(), wordsRequired);
        words = copyOfRange(words, 0, request);
        sizeIsSticky = false;
    }
}

int BitSet::hashCode() {
    long h = 1234;
    for (int i = wordsInUse; --i >= 0;)
        h ^= (*words)[i] * (i + 1);

    return (int) ((h >> 32) ^ h);
}

std::string *BitSet::toString() {
    checkInvariants();

    int numBits = (wordsInUse > 128) ?
                  cardinality() : wordsInUse * BITS_PER_WORD;
    auto *b = new StringBuilder(6 * numBits + 2);
    b->append('{');

    int i = nextSetBit(0);
    if (i != -1) {
        b->append(i);
        for (i = nextSetBit(i + 1); i >= 0; i = nextSetBit(i + 1)) {
            int endOfRun = nextClearBit(i);
            do { b->append(", ")->append(i); }
            while (++i < endOfRun);
        }
    }

    b->append('}');
    return b->toString();
}