#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class TreeMap: public AbstractMap<K, V>, public NavigableMap<K, V>, public java::lang::Cloneable, public java::io::Serializable{
//fields
public:
    static bool BLACK;
    static bool RED;
    static java::lang::Object* UNBOUNDED;
    Comparator<java::lang::Object*>* comparator_renamed;
    NavigableMap<K, V>* descendingMap_renamed;
    EntrySet* entrySet_renamed;
    int modCount;
    KeySet<K>* navigableKeySet_renamed;
    Entry<K, V>* root;
    static long serialVersionUID;
    int size_renamed;

//methods
public:
    TreeMap();

    TreeMap(Comparator<java::lang::Object*>* );

    TreeMap(Map<java::lang::Object*, java::lang::Object*>* );

    TreeMap(SortedMap<K, java::lang::Object*>* );

    void addAllForTreeSet(SortedSet<java::lang::Object*>* , V );

    void buildFromSorted(int , Iterator<java::lang::Object*>* , java::io::ObjectInputStream* , V );

    Entry<K, V>* buildFromSorted(int , int , int , int , Iterator<java::lang::Object*>* , java::io::ObjectInputStream* , V );

    Entry<K, V>* ceilingEntry(K );

    K ceilingKey(K );

    void clear();

    java::lang::Object* clone();

    template <typename K, typename V>
    static bool colorOf(Entry<K, V>* );

    Comparator<java::lang::Object*>* comparator();

    int compare(java::lang::Object* , java::lang::Object* );

    static int computeRedLevel(int );

    bool containsKey(java::lang::Object* );

    bool containsValue(java::lang::Object* );

    void deleteEntry(Entry<K, V>* );

    Iterator<K>* descendingKeyIterator();

    NavigableSet<K>* descendingKeySet();

    Spliterator<K>* descendingKeySpliterator();

    NavigableMap<K, V>* descendingMap();

    std::unordered_set<Entry<K, V>*>* entrySet();

    template <typename K, typename V>
    static Entry<K, V>* exportEntry(Entry<K, V>* );

    Entry<K, V>* firstEntry();

    K firstKey();

    void fixAfterDeletion(Entry<K, V>* );

    void fixAfterInsertion(Entry<K, V>* );

    Entry<K, V>* floorEntry(K );

    K floorKey(K );

    void forEach(java::util::function::BiConsumer<java::lang::Object*, java::lang::Object*>* );

    V get(java::lang::Object* );

    Entry<K, V>* getCeilingEntry(K );

    Entry<K, V>* getEntry(java::lang::Object* );

    Entry<K, V>* getEntryUsingComparator(java::lang::Object* );

    Entry<K, V>* getFirstEntry();

    Entry<K, V>* getFloorEntry(K );

    Entry<K, V>* getHigherEntry(K );

    Entry<K, V>* getLastEntry();

    Entry<K, V>* getLowerEntry(K );

    SortedMap<K, V>* headMap(K );

    NavigableMap<K, V>* headMap(K , bool );

    Entry<K, V>* higherEntry(K );

    K higherKey(K );

    template <typename K>
    static K key(Entry<K, java::lang::Object*>* );

    Iterator<K>* keyIterator();

    template <typename K, typename V>
    static K keyOrNull(Entry<K, V>* );

    std::unordered_set<K>* keySet();

    Spliterator<K>* keySpliterator();

    template <typename K>
    static Spliterator<K>* keySpliteratorFor(NavigableMap<K, java::lang::Object*>* );

    Entry<K, V>* lastEntry();

    K lastKey();

    template <typename K, typename V>
    static Entry<K, V>* leftOf(Entry<K, V>* );

    Entry<K, V>* lowerEntry(K );

    K lowerKey(K );

    NavigableSet<K>* navigableKeySet();

    template <typename K, typename V>
    static Entry<K, V>* parentOf(Entry<K, V>* );

    Entry<K, V>* pollFirstEntry();

    Entry<K, V>* pollLastEntry();

    template <typename K, typename V>
    static Entry<K, V>* predecessor(Entry<K, V>* );

    V put(K , V );

    void putAll(Map<java::lang::Object*, java::lang::Object*>* );

    void readObject(java::io::ObjectInputStream* );

    void readTreeSet(int , java::io::ObjectInputStream* , V );

    V remove(java::lang::Object* );

    V replace(K , V );

    bool replace(K , V , V );

    void replaceAll(java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    template <typename K, typename V>
    static Entry<K, V>* rightOf(Entry<K, V>* );

    void rotateLeft(Entry<K, V>* );

    void rotateRight(Entry<K, V>* );

    template <typename K, typename V>
    static void setColor(Entry<K, V>* , bool );

    int size();

    SortedMap<K, V>* subMap(K , K );

    NavigableMap<K, V>* subMap(K , bool , K , bool );

    template <typename K, typename V>
    static Entry<K, V>* successor(Entry<K, V>* );

    SortedMap<K, V>* tailMap(K );

    NavigableMap<K, V>* tailMap(K , bool );

    static bool valEquals(java::lang::Object* , java::lang::Object* );

    Collection<V>* values();

    void writeObject(java::io::ObjectOutputStream* );


};//class TreeMap

}//namespace java
}//namespace util
