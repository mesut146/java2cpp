#pragma once

#include <vector>
#include <cassert>
#include "java/lang/IllegalArgumentException.h"
#include "java/lang/ArrayIndexOutOfBoundsException.h"

using namespace java::lang;

template<class T>
void arraycopy(std::vector<T> src, int srcPos, std::vector<T> dst, int dstPos, int length) {
    int end = srcPos + length;
    while (srcPos < end) {
        dst[dstPos++] = src[srcPos++];
    }
}

template<class T>
class ComparableTimSort {
public:
    static const int MIN_MERGE = 32;
    static const int MIN_GALLOP = 7;
    int minGallop = MIN_GALLOP;
    static const int INITIAL_TMP_STORAGE_LENGTH = 256;
    std::vector<T> a;
    std::vector<T> tmp;

    int stackSize = 0;  // Number of pending runs on stack
    std::vector<int> runBase;
    std::vector<int> runLen;

    ComparableTimSort(std::vector<T> a) {
        this->a = a;

        // Allocate temp storage (which may be increased later if necessary)
        int len = a.size();
        auto newArray = std::vector<T>(len < 2 * INITIAL_TMP_STORAGE_LENGTH ?
                                       (unsigned int) len >> 1 : INITIAL_TMP_STORAGE_LENGTH);
        tmp = newArray;

        /*
         * Allocate runs-to-be-merged stack (which cannot be expanded).  The
         * stack length requirements are described in listsort.txt.  The C
         * version always uses the same stack length (85), but this was
         * measured to be too expensive when sorting "mid-sized" arrays (e.g.,
         * 100 elements) in Java.  Therefore, we use smaller (but sufficiently
         * large) stack lengths for smaller arrays.  The "magic numbers" in the
         * computation below must be changed if MIN_MERGE is decreased.  See
         * the MIN_MERGE declaration above for more information.
         */
        int stackLen = (len < 120 ? 5 :
                        len < 1542 ? 10 :
                        len < 119151 ? 24 : 40);
        runBase = std::vector<int>(stackLen);
        runLen = std::vector<int>(stackLen);
    }

    //void pushRun(int runBase, int runLen);

    //void mergeCollapse();

    /*static int countRunAndMakeAscending(std::vector<T> a, int lo, int hi);

    static int minRunLength(int n);

    static void rangeCheck(int arrayLen, int fromIndex, int toIndex);*/

    static void sort(std::vector<T> a) {
        sort(a, 0, a.size());
    }

    static void sort(std::vector<T> a, int lo, int hi) {
        rangeCheck(a.size(), lo, hi);
        int nRemaining = hi - lo;
        if (nRemaining < 2)
            return;  // Arrays of size 0 and 1 are always sorted

        // If array is small, do a "mini-TimSort" with no merges
        if (nRemaining < MIN_MERGE) {
            int initRunLen = countRunAndMakeAscending(a, lo, hi);
            binarySort(a, lo, hi, lo + initRunLen);
            return;
        }

        /**
         * March over the array once, left to right, finding natural runs,
         * extending short natural runs to minRun elements, and merging runs
         * to maintain stack invariant.
         */
        ComparableTimSort ts(a);
        int minRun = minRunLength(nRemaining);
        do {
            // Identify next run
            int runLen = countRunAndMakeAscending(a, lo, hi);

            // If run is short, extend to min(minRun, nRemaining)
            if (runLen < minRun) {
                int force = nRemaining <= minRun ? nRemaining : minRun;
                binarySort(a, lo, lo + force, lo + runLen);
                runLen = force;
            }

            // Push run onto pending-run stack, and maybe merge
            ts.pushRun(lo, runLen);
            ts.mergeCollapse();

            // Advance to find next run
            lo += runLen;
            nRemaining -= runLen;
        } while (nRemaining != 0);

        // Merge all remaining runs to complete sort
        assert(lo == hi);
        ts.mergeForceCollapse();
        assert(ts.stackSize == 1);
    }

    static int minRunLength(int n) {
        assert(n >= 0);
        int r = 0;      // Becomes 1 if any 1 bits are shifted off
        while (n >= MIN_MERGE) {
            r |= (n & 1);
            n >>= 1;
        }
        return n + r;
    }

    static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex)
            throw new IllegalArgumentException(
                    "fromIndex(" + std::to_string(fromIndex) + ") > toIndex(" + std::to_string(toIndex) + ")");
        if (fromIndex < 0)
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        if (toIndex > arrayLen)
            throw new ArrayIndexOutOfBoundsException(toIndex);
    }

    static int countRunAndMakeAscending(std::vector<T> a, int lo, int hi) {
        assert(lo < hi);
        int runHi = lo + 1;
        if (runHi == hi)
            return 1;

        // Find end of run, and reverse range if descending
        if (a[runHi++]->compareTo(a[lo]) < 0) { // Descending
            while (runHi < hi && (a[runHi])->compareTo(a[runHi - 1]) < 0)
                runHi++;
            reverseRange(a, lo, runHi);
        } else {                              // Ascending
            while (runHi < hi && (a[runHi])->compareTo(a[runHi - 1]) >= 0)
                runHi++;
        }
        return runHi - lo;
    }

    static void reverseRange(std::vector<T> a, int lo, int hi) {
        hi--;
        while (lo < hi) {
            auto t = a[lo];
            a[lo++] = a[hi];
            a[hi--] = t;
        }
    }

    static void binarySort(std::vector<T> a, int lo, int hi, int start) {
        assert (lo <= start && start <= hi);
        if (start == lo)
            start++;
        for (; start < hi; start++) {

            auto pivot = a[start];

            // Set left (and right) to the index where a[start] (pivot) belongs
            int left = lo;
            int right = start;
            assert (left <= right);
            /*
             * Invariants:
             *   pivot >= all in [lo, left).
             *   pivot <  all in [right, start).
             */
            while (left < right) {
                int mid = (unsigned int) (left + right) >> 1;
                if (pivot->compareTo(a[mid]) < 0)
                    right = mid;
                else
                    left = mid + 1;
            }
            assert (left == right);

            /*
             * The invariants still hold: pivot >= all in [lo, left) and
             * pivot < all in [left, start), so pivot belongs at left.  Note
             * that if there are elements equal to pivot, left points to the
             * first slot after them -- that's why this sort is stable.
             * Slide elements over to make room for pivot.
             */
            int n = start - left;  // The number of elements to move
            // Switch is just an optimization for arraycopy in default case
            switch (n) {
                case 2:
                    a[left + 2] = a[left + 1];
                case 1:
                    a[left + 1] = a[left];
                    break;
                default:
                    arraycopy(a, left, a, left + 1, n);
            }
            a[left] = pivot;
        }
    }

    void pushRun(int runBase, int runLen) {
        this->runBase[stackSize] = runBase;
        this->runLen[stackSize] = runLen;
        stackSize++;
    }

    void mergeCollapse() {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] <= runLen[n] + runLen[n + 1]) {
                if (runLen[n - 1] < runLen[n + 1])
                    n--;
                mergeAt(n);
            } else if (runLen[n] <= runLen[n + 1]) {
                mergeAt(n);
            } else {
                break; // Invariant is established
            }
        }
    }

    void mergeForceCollapse() {
        while (stackSize > 1) {
            int n = stackSize - 2;
            if (n > 0 && runLen[n - 1] < runLen[n + 1])
                n--;
            mergeAt(n);
        }
    }

    /**
     * Merges the two runs at stack indices i and i+1.  Run i must be
     * the penultimate or antepenultimate run on the stack.  In other words,
     * i must be equal to stackSize-2 or stackSize-3.
     *
     * @param i stack index of the first of the two runs to merge
     */

    void mergeAt(int i) {
        assert(stackSize >= 2);
        assert (i >= 0);
        assert(i == stackSize - 2 || i == stackSize - 3);

        int base1 = runBase[i];
        int len1 = runLen[i];
        int base2 = runBase[i + 1];
        int len2 = runLen[i + 1];
        assert (len1 > 0 && len2 > 0);
        assert (base1 + len1 == base2);

        /*
         * Record the length of the combined runs; if i is the 3rd-last
         * run now, also slide over the last run (which isn't involved
         * in this merge).  The current run (i+1) goes away in any case.
         */
        runLen[i] = len1 + len2;
        if (i == stackSize - 3) {
            runBase[i + 1] = runBase[i + 2];
            runLen[i + 1] = runLen[i + 2];
        }
        stackSize--;

        /*
         * Find where the first element of run2 goes in run1. Prior elements
         * in run1 can be ignored (because they're already in place).
         */
        int k = gallopRight(a[base2], a, base1, len1, 0);
        assert(k >= 0);
        base1 += k;
        len1 -= k;
        if (len1 == 0)
            return;

        /*
         * Find where the last element of run1 goes in run2. Subsequent elements
         * in run2 can be ignored (because they're already in place).
         */
        len2 = gallopLeft(a[base1 + len1 - 1], a,
                          base2, len2, len2 - 1);
        assert (len2 >= 0);
        if (len2 == 0)
            return;

        // Merge remaining runs, using tmp array with min(len1, len2) elements
        if (len1 <= len2)
            mergeLo(base1, len1, base2, len2);
        else
            mergeHi(base1, len1, base2, len2);
    }

    void mergeLo(int base1, int len1, int base2, int len2) {
        assert (len1 > 0 && len2 > 0 && base1 + len1 == base2);

        // Copy first run into temp array
        auto a = this->a; // For performance
        auto tmp = ensureCapacity(len1);
        arraycopy(a, base1, tmp, 0, len1);

        int cursor1 = 0;       // Indexes into tmp array
        int cursor2 = base2;   // Indexes int a
        int dest = base1;      // Indexes int a

        // Move first element of second run and deal with degenerate cases
        a[dest++] = a[cursor2++];
        if (--len2 == 0) {
            arraycopy(tmp, cursor1, a, dest, len1);
            return;
        }
        if (len1 == 1) {
            arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1]; // Last elt of run 1 to end of merge
            return;
        }

        int minGallop = this->minGallop;  // Use local variable for performance
        outer:
        while (true) {
            int count1 = 0; // Number of times in a row that first run won
            int count2 = 0; // Number of times in a row that second run won

            /*
             * Do the straightforward thing until (if ever) one run starts
             * winning consistently.
             */
            do {
                assert (len1 > 1 && len2 > 0);
                if ((a[cursor2])->compareTo(tmp[cursor1]) < 0) {
                    a[dest++] = a[cursor2++];
                    count2++;
                    count1 = 0;
                    if (--len2 == 0)
                        goto outer0;
                } else {
                    a[dest++] = tmp[cursor1++];
                    count1++;
                    count2 = 0;
                    if (--len1 == 1)
                        goto outer0;
                }
            } while ((count1 | count2) < minGallop);

            /*
             * One run is winning so consistently that galloping may be a
             * huge win. So try that, and continue galloping until (if ever)
             * neither run appears to be winning consistently anymore.
             */
            do {
                assert (len1 > 1 && len2 > 0);
                count1 = gallopRight(a[cursor2], tmp, cursor1, len1, 0);
                if (count1 != 0) {
                    arraycopy(tmp, cursor1, a, dest, count1);
                    dest += count1;
                    cursor1 += count1;
                    len1 -= count1;
                    if (len1 <= 1)  // len1 == 1 || len1 == 0
                        goto outer0;
                }
                a[dest++] = a[cursor2++];
                if (--len2 == 0)
                    goto outer0;

                count2 = gallopLeft(tmp[cursor1], a, cursor2, len2, 0);
                if (count2 != 0) {
                    arraycopy(a, cursor2, a, dest, count2);
                    dest += count2;
                    cursor2 += count2;
                    len2 -= count2;
                    if (len2 == 0)
                        goto outer0;
                }
                a[dest++] = tmp[cursor1++];
                if (--len1 == 1)
                    goto outer0;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);
            if (minGallop < 0)
                minGallop = 0;
            minGallop += 2;  // Penalize for leaving gallop mode
        }  // End of "outer" loop
        outer0:
        this->minGallop = minGallop < 1 ? 1 : minGallop;  // Write back to field

        if (len1 == 1) {
            assert (len2 > 0);
            arraycopy(a, cursor2, a, dest, len2);
            a[dest + len2] = tmp[cursor1]; //  Last elt of run 1 to end of merge
        } else if (len1 == 0) {
            throw IllegalArgumentException("Comparison method violates its general contract!");
        } else {
            assert (len2 == 0);
            assert (len1 > 1);
            arraycopy(tmp, cursor1, a, dest, len1);
        }
    }

    /**
     * Like mergeLo, except that this method should be called only if
     * len1 >= len2; mergeLo should be called if len1 <= len2.  (Either method
     * may be called if len1 == len2.)
     *
     * @param base1 index of first element in first run to be merged
     * @param len1  length of first run to be merged (must be > 0)
     * @param base2 index of first element in second run to be merged
     *        (must be aBase + aLen)
     * @param len2  length of second run to be merged (must be > 0)
     */

    void mergeHi(int base1, int len1, int base2, int len2) {
        assert(len1 > 0 && len2 > 0 && base1 + len1 == base2);

        // Copy second run into temp array
        auto a = this->a; // For performance
        auto tmp = ensureCapacity(len2);
        arraycopy(a, base2, tmp, 0, len2);

        int cursor1 = base1 + len1 - 1;  // Indexes into a
        int cursor2 = len2 - 1;          // Indexes into tmp array
        int dest = base2 + len2 - 1;     // Indexes into a

        // Move last element of first run and deal with degenerate cases
        a[dest--] = a[cursor1--];
        if (--len1 == 0) {
            arraycopy(tmp, 0, a, dest - (len2 - 1), len2);
            return;
        }
        if (len2 == 1) {
            dest -= len1;
            cursor1 -= len1;
            arraycopy(a, cursor1 + 1, a, dest + 1, len1);
            a[dest] = tmp[cursor2];
            return;
        }

        int minGallop = this->minGallop;  // Use local variable for performance
        outer:
        while (true) {
            int count1 = 0; // Number of times in a row that first run won
            int count2 = 0; // Number of times in a row that second run won

            /*
             * Do the straightforward thing until (if ever) one run
             * appears to win consistently.
             */
            do {
                assert (len1 > 0 && len2 > 1);
                if ((tmp[cursor2])->compareTo(a[cursor1]) < 0) {
                    a[dest--] = a[cursor1--];
                    count1++;
                    count2 = 0;
                    if (--len1 == 0)
                        goto outer0;
                } else {
                    a[dest--] = tmp[cursor2--];
                    count2++;
                    count1 = 0;
                    if (--len2 == 1)
                        goto outer0;
                }
            } while ((count1 | count2) < minGallop);

            /*
             * One run is winning so consistently that galloping may be a
             * huge win. So try that, and continue galloping until (if ever)
             * neither run appears to be winning consistently anymore.
             */
            do {
                assert (len1 > 0 && len2 > 1);
                count1 = len1 - gallopRight(tmp[cursor2], a, base1, len1, len1 - 1);
                if (count1 != 0) {
                    dest -= count1;
                    cursor1 -= count1;
                    len1 -= count1;
                    arraycopy(a, cursor1 + 1, a, dest + 1, count1);
                    if (len1 == 0)
                        goto outer0;
                }
                a[dest--] = tmp[cursor2--];
                if (--len2 == 1)
                    goto outer0;

                count2 = len2 - gallopLeft(a[cursor1], tmp, 0, len2, len2 - 1);
                if (count2 != 0) {
                    dest -= count2;
                    cursor2 -= count2;
                    len2 -= count2;
                    arraycopy(tmp, cursor2 + 1, a, dest + 1, count2);
                    if (len2 <= 1)
                        goto outer0; // len2 == 1 || len2 == 0
                }
                a[dest--] = a[cursor1--];
                if (--len1 == 0)
                    goto outer0;
                minGallop--;
            } while (count1 >= MIN_GALLOP | count2 >= MIN_GALLOP);
            if (minGallop < 0)
                minGallop = 0;
            minGallop += 2;  // Penalize for leaving gallop mode
        }  // End of "outer" loop
        outer0:
        this->minGallop = minGallop < 1 ? 1 : minGallop;  // Write back to field

        if (len2 == 1) {
            assert (len1 > 0);
            dest -= len1;
            cursor1 -= len1;
            arraycopy(a, cursor1 + 1, a, dest + 1, len1);
            a[dest] = tmp[cursor2];  // Move first elt of run2 to front of merge
        } else if (len2 == 0) {
            throw IllegalArgumentException(
                    "Comparison method violates its general contract!");
        } else {
            assert (len1 == 0);
            assert (len2 > 0);
            arraycopy(tmp, 0, a, dest - (len2 - 1), len2);
        }
    }

    std::vector<T> ensureCapacity(int minCapacity) {
        if (tmp.size() < minCapacity) {
            // Compute smallest power of 2 > minCapacity
            int newSize = minCapacity;
            newSize |= newSize >> 1;
            newSize |= newSize >> 2;
            newSize |= newSize >> 4;
            newSize |= newSize >> 8;
            newSize |= newSize >> 16;
            newSize++;

            if (newSize < 0) // Not bloody likely!
                newSize = minCapacity;
            else
                newSize = std::min(newSize, (int) ((unsigned int) a.size() >> 1));

            auto newArray = std::vector<T>(newSize);
            tmp = newArray;
        }
        return tmp;
    }

    static int gallopLeft(T key, std::vector<T> a,
                          int base, int len, int hint) {
        assert (len > 0 && hint >= 0 && hint < len);

        int lastOfs = 0;
        int ofs = 1;
        if (key->compareTo(a[base + hint]) > 0) {
            // Gallop right until a[base+hint+lastOfs] < key <= a[base+hint+ofs]
            int maxOfs = len - hint;
            while (ofs < maxOfs && key->compareTo(a[base + hint + ofs]) > 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int overflow
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // Make offsets relative to base
            lastOfs += hint;
            ofs += hint;
        } else { // key <= a[base + hint]
            // Gallop left until a[base+hint-ofs] < key <= a[base+hint-lastOfs]
            int maxOfs = hint + 1;
            while (ofs < maxOfs && key->compareTo(a[base + hint - ofs]) <= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int overflow
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // Make offsets relative to base
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        }
        assert (-1 <= lastOfs && lastOfs < ofs && ofs <= len);

        /*
         * Now a[base+lastOfs] < key <= a[base+ofs], so key belongs somewhere
         * to the right of lastOfs but no farther right than ofs.  Do a binary
         * search, with invariant a[base + lastOfs - 1] < key <= a[base + ofs].
         */
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((unsigned int) (ofs - lastOfs) >> 1);

            if (key->compareTo(a[base + m]) > 0)
                lastOfs = m + 1;  // a[base + m] < key
            else
                ofs = m;          // key <= a[base + m]
        }
        assert(lastOfs == ofs);    // so a[base + ofs - 1] < key <= a[base + ofs]
        return ofs;
    }

    static int gallopRight(T key, std::vector<T> a,
                           int base, int len, int hint) {
        assert (len > 0 && hint >= 0 && hint < len);

        int ofs = 1;
        int lastOfs = 0;
        if (key->compareTo(a[base + hint]) < 0) {
            // Gallop left until a[b+hint - ofs] <= key < a[b+hint - lastOfs]
            int maxOfs = hint + 1;
            while (ofs < maxOfs && key->compareTo(a[base + hint - ofs]) < 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int overflow
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // Make offsets relative to b
            int tmp = lastOfs;
            lastOfs = hint - ofs;
            ofs = hint - tmp;
        } else { // a[b + hint] <= key
            // Gallop right until a[b+hint + lastOfs] <= key < a[b+hint + ofs]
            int maxOfs = len - hint;
            while (ofs < maxOfs && key->compareTo(a[base + hint + ofs]) >= 0) {
                lastOfs = ofs;
                ofs = (ofs << 1) + 1;
                if (ofs <= 0)   // int overflow
                    ofs = maxOfs;
            }
            if (ofs > maxOfs)
                ofs = maxOfs;

            // Make offsets relative to b
            lastOfs += hint;
            ofs += hint;
        }
        assert (-1 <= lastOfs && lastOfs < ofs && ofs <= len);

        /*
         * Now a[b + lastOfs] <= key < a[b + ofs], so key belongs somewhere to
         * the right of lastOfs but no farther right than ofs.  Do a binary
         * search, with invariant a[b + lastOfs - 1] <= key < a[b + ofs].
         */
        lastOfs++;
        while (lastOfs < ofs) {
            int m = lastOfs + ((unsigned int) (ofs - lastOfs) >> 1);

            if (key->compareTo(a[base + m]) < 0)
                ofs = m;          // key < a[b + m]
            else
                lastOfs = m + 1;  // a[b + m] <= key
        }
        assert (lastOfs == ofs);    // so a[b + ofs - 1] <= key < a[b + ofs]
        return ofs;
    }
};


