#pragma once

#include <utility>
#include <vector>
#include <unordered_map>
#include <unordered_set>
#include <stdexcept>
#include "TreeMap.h"
#include "Entry.h"
#include "java/lang/Float.h"
#include "java/lang/Integer.h"


template<class K, class V>
class HashMap {
public:
    typedef std::vector<Entry<K, V> *> map_t;
    static const int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16
    static const int MAXIMUM_CAPACITY = 1 << 30;
    constexpr static const float DEFAULT_LOAD_FACTOR = 0.75f;

    static map_t &EMPTY_TABLE() {
        static map_t tt;
        return tt;
    }

    map_t table = EMPTY_TABLE();
    int size0;
    int threshold;
    float loadFactor;
    int modCount;

    static const int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = INT32_MAX;
    int hashSeed = 0;

public:

    HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw std::runtime_error("Illegal initial capacity: " + std::to_string(initialCapacity));
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || java::lang::Float::isNaN(loadFactor))
            throw std::runtime_error("Illegal load factor: " + std::to_string(loadFactor));

        this->loadFactor = loadFactor;
        threshold = initialCapacity;
        init();
    }

    HashMap(int initialCapacity) : HashMap(initialCapacity, DEFAULT_LOAD_FACTOR) {}

    HashMap() : HashMap(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR) {}

    static int roundUpToPowerOf2(int number) {
        // assert number >= 0 : "number must be non-negative";
        return number >= MAXIMUM_CAPACITY
               ? MAXIMUM_CAPACITY
               : (number > 1) ? java::lang::Integer::highestOneBit((number - 1) << 1) : 1;
    }

    void inflateTable(int toSize) {
        // Find a power of 2 >= toSize
        int capacity = roundUpToPowerOf2(toSize);

        threshold = (int) std::min((int) (capacity * loadFactor), MAXIMUM_CAPACITY + 1);
        table = map_t(capacity);//capacity
        initHashSeedAsNeeded(capacity);
    }

    void init() {

    }

    static int shift(int l, int r) {
        return (unsigned int) l >> r;
    }

    bool initHashSeedAsNeeded(int capacity) {
        bool currentAltHashing = hashSeed != 0;
        //bool useAltHashing = (capacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
        bool useAltHashing = false;
        bool switching = currentAltHashing ^useAltHashing;
        if (switching) {
            //hashSeed = useAltHashing? sun.misc.Hashing.randomHashSeed(this): 0;
            hashSeed = 0;
        }
        return switching;
    }

    size_t hasher(K k) {
        int h = hashSeed;

        h ^= k->hashCode();

        // This function ensures that hashCodes that differ only by
        // constant multiples at each bit position have a bounded
        // number of collisions (approximately 8 at default load factor).
        h ^= shift(h, 20) ^ shift(h, 12);
        return h ^ shift(h, 7) ^ shift(h, 4);
    }

    size_t hasher(const std::string &k) {
        int h = hashSeed;
        if (0 != h) {
            //return sun.misc.Hashing.stringHash32((String) k);
            return std::hash<std::string>{}(k);
        }
    }

    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    int size() {
        return size0;
    }

    bool isEmpty() {
        return size0 == 0;
    }

    V get(K key) {
        if (key == nullptr)
            return getForNullKey();
        auto entry = getEntry(key);

        return nullptr == entry ? nullptr : entry->getValue();
    }

    V getForNullKey() {
        if (size0 == 0) {
            return nullptr;
        }
        for (auto e = table[0]; e != nullptr; e = e->next) {
            if (e->key == nullptr)
                return e->value;
        }
        return nullptr;
    }

    bool containsKey(K key) {
        return getEntry(key) != nullptr;
    }

    Entry<K, V> *getEntry(K key) {
        if (size0 == 0) {
            return nullptr;
        }

        int hash = (key == nullptr) ? 0 : hasher(key);
        for (auto e = table[indexFor(hash, table.size())];
             e != nullptr;
             e = e->next) {
            K k;
            if (e->hash == hash &&
                ((k = e->key) == key || (key != nullptr && key->equals(k))))
                return e;
        }
        return nullptr;
    }

    V put(std::pair<K, V> e) {
        return put(e.first, e.second);
    }

    V put(K key, V value) {
        if (table == EMPTY_TABLE()) {
            inflateTable(threshold);
        }
        if (key == nullptr)
            return putForNullKey(value);
        int hash = hasher(key);
        int i = indexFor(hash, table.size());
        for (auto e = table[i]; e != nullptr; e = e->next) {
            K k;
            if (e->hash == hash && ((k = e->key) == key || key->equals(k))) {
                V oldValue = e->value;
                e->value = value;
                //e.recordAccess(this);
                return oldValue;
            }
        }

        modCount++;
        addEntry(hash, key, value, i);
        return nullptr;
    }

    V putForNullKey(V value) {
        for (auto e = table[0]; e != nullptr; e = e->next) {
            if (e->key == nullptr) {
                V oldValue = e->value;
                e->value = value;
                //e.recordAccess(this);
                return oldValue;
            }
        }
        modCount++;
        addEntry(0, nullptr, value, 0);
        return nullptr;
    }

    void putForCreate(K key, V value) {
        int hash = nullptr == key ? 0 : hash(key);
        int i = indexFor(hash, table.length);

        /**
         * Look for preexisting entry for key.  This will never happen for
         * clone or deserialize.  It will only happen for construction if the
         * input Map is a sorted map whose ordering is inconsistent w/ equals.
         */
        for (auto e = table[i]; e != nullptr; e = e.next) {
            K k;
            if (e.hash == hash &&
                ((k = e.key) == key || (key != nullptr && key.equals(k)))) {
                e.value = value;
                return;
            }
        }

        createEntry(hash, key, value, i);
    }

    void resize(int newCapacity) {
        auto oldTable = table;
        int oldCapacity = oldTable.size();
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = java::lang::Integer::MAX_VALUE;
            return;
        }

        auto newTable = map_t(newCapacity);
        transfer(newTable, initHashSeedAsNeeded(newCapacity));
        table = newTable;
        threshold = (int) std::min((int) (newCapacity * loadFactor), MAXIMUM_CAPACITY + 1);
    }

    void transfer(map_t newTable, bool rehash) {
        int newCapacity = newTable.size();
        for (auto e : table) {
            while (nullptr != e) {
                auto next = e->next;
                if (rehash) {
                    e->hash = nullptr == e->key ? 0 : hasher(e->key);
                }
                int i = indexFor(e->hash, newCapacity);
                e->next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    std::unordered_set<Entry<K, V> *> *entrySet() {
        auto set = new std::unordered_set<Entry<K, V> *>();
        for (auto e = table[0]; e != nullptr; e = e->next) {
            set->insert(e);
        }
        return set;
    }

    std::vector<V> *values() {
        auto res = new std::vector<V>();
        for (auto e = table[0]; e != nullptr; e = e->next) {
            res->push_back(e->value);
        }
        return res;
    }

    void addEntry(int hash, K key, V value, int bucketIndex) {
        if ((size0 >= threshold) && (nullptr != table[bucketIndex])) {
            resize(2 * table.size());
            hash = (nullptr != key) ? hasher(key) : 0;
            bucketIndex = indexFor(hash, table.size());
        }

        createEntry(hash, key, value, bucketIndex);
    }

    void createEntry(int hash, K key, V value, int bucketIndex) {
        auto e = table[bucketIndex];
        table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
        size0++;
    }

};
