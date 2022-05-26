#pragma once


namespace java{
namespace util{

class Collections
{
//fields
public:
    static int BINARYSEARCH_THRESHOLD;
    static int COPY_THRESHOLD;
    static std::vector* EMPTY_LIST;
    static Map* EMPTY_MAP;
    static std::unordered_set* EMPTY_SET;
    static int FILL_THRESHOLD;
    static int INDEXOFSUBLIST_THRESHOLD;
    static int REPLACEALL_THRESHOLD;
    static int REVERSE_THRESHOLD;
    static int ROTATE_THRESHOLD;
    static int SHUFFLE_THRESHOLD;
    static Random* r;

//methods
public:
    Collections();

    template <typename T>
    static bool addAll(Collection<java::lang::Object*>* , std::vector<T>* );

    template <typename T>
    static Queue<T>* asLifoQueue(Deque<T>* );

    template <typename T>
    static int binarySearch(std::vector<java::lang::Object*>* , T );

    template <typename T>
    static int binarySearch(std::vector<java::lang::Object*>* , T , Comparator<java::lang::Object*>* );

    template <typename E>
    static Collection<E>* checkedCollection(Collection<E>* , java::lang::Class<E>* );

    template <typename E>
    static std::vector<E>* checkedList(std::vector<E>* , java::lang::Class<E>* );

    template <typename K, typename V>
    static Map<K, V>* checkedMap(Map<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );

    template <typename K, typename V>
    static NavigableMap<K, V>* checkedNavigableMap(NavigableMap<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );

    template <typename E>
    static NavigableSet<E>* checkedNavigableSet(NavigableSet<E>* , java::lang::Class<E>* );

    template <typename E>
    static Queue<E>* checkedQueue(Queue<E>* , java::lang::Class<E>* );

    template <typename E>
    static std::unordered_set<E>* checkedSet(std::unordered_set<E>* , java::lang::Class<E>* );

    template <typename K, typename V>
    static SortedMap<K, V>* checkedSortedMap(SortedMap<K, V>* , java::lang::Class<K>* , java::lang::Class<V>* );

    template <typename E>
    static SortedSet<E>* checkedSortedSet(SortedSet<E>* , java::lang::Class<E>* );

    template <typename T>
    static void copy(std::vector<java::lang::Object*>* , std::vector<java::lang::Object*>* );

    static bool disjoint(Collection<java::lang::Object*>* , Collection<java::lang::Object*>* );

    template <typename T>
    static Enumeration<T>* emptyEnumeration();

    template <typename T>
    static Iterator<T>* emptyIterator();

    template <typename T>
    static std::vector<T>* emptyList();

    template <typename T>
    static ListIterator<T>* emptyListIterator();

    template <typename K, typename V>
    static Map<K, V>* emptyMap();

    template <typename K, typename V>
    static NavigableMap<K, V>* emptyNavigableMap();

    template <typename E>
    static NavigableSet<E>* emptyNavigableSet();

    template <typename T>
    static std::unordered_set<T>* emptySet();

    template <typename K, typename V>
    static SortedMap<K, V>* emptySortedMap();

    template <typename E>
    static SortedSet<E>* emptySortedSet();

    template <typename T>
    static Enumeration<T>* enumeration(Collection<T>* );

    static bool eq(java::lang::Object* , java::lang::Object* );

    template <typename T>
    static void fill(std::vector<java::lang::Object*>* , T );

    static int frequency(Collection<java::lang::Object*>* , java::lang::Object* );

    template <typename T>
    static T get(ListIterator<java::lang::Object*>* , int );

    static int indexOfSubList(std::vector<java::lang::Object*>* , std::vector<java::lang::Object*>* );

    template <typename T>
    static int indexedBinarySearch(std::vector<java::lang::Object*>* , T );

    template <typename T>
    static int indexedBinarySearch(std::vector<java::lang::Object*>* , T , Comparator<java::lang::Object*>* );

    template <typename T>
    static int iteratorBinarySearch(std::vector<java::lang::Object*>* , T );

    template <typename T>
    static int iteratorBinarySearch(std::vector<java::lang::Object*>* , T , Comparator<java::lang::Object*>* );

    static int lastIndexOfSubList(std::vector<java::lang::Object*>* , std::vector<java::lang::Object*>* );

    template <typename T>
    static std::vector<T>* list(Enumeration<T>* );

    template <typename T>
    static T max(Collection<java::lang::Object*>* );

    template <typename T>
    static T max(Collection<java::lang::Object*>* , Comparator<java::lang::Object*>* );

    template <typename T>
    static T min(Collection<java::lang::Object*>* );

    template <typename T>
    static T min(Collection<java::lang::Object*>* , Comparator<java::lang::Object*>* );

    template <typename T>
    static std::vector<T>* nCopies(int , T );

    template <typename E>
    static std::unordered_set<E>* newSetFromMap(Map<E, java::lang::Boolean*>* );

    template <typename T>
    static bool replaceAll(std::vector<T>* , T , T );

    static void reverse(std::vector<java::lang::Object*>* );

    template <typename T>
    static Comparator<T>* reverseOrder();

    template <typename T>
    static Comparator<T>* reverseOrder(Comparator<T>* );

    static void rotate(std::vector<java::lang::Object*>* , int );

    template <typename T>
    static void rotate1(std::vector<T>* , int );

    static void rotate2(std::vector<java::lang::Object*>* , int );

    static void shuffle(std::vector<java::lang::Object*>* );

    static void shuffle(std::vector<java::lang::Object*>* , Random* );

    template <typename T>
    static std::unordered_set<T>* singleton(T );

    template <typename E>
    static Iterator<E>* singletonIterator(E );

    template <typename T>
    static std::vector<T>* singletonList(T );

    template <typename K, typename V>
    static Map<K, V>* singletonMap(K , V );

    template <typename T>
    static Spliterator<T>* singletonSpliterator(T );

    template <typename T>
    static void sort(std::vector<T>* );

    template <typename T>
    static void sort(std::vector<T>* , Comparator<java::lang::Object*>* );

    static void swap(std::vector<java::lang::Object*>* , int , int );

    static void swap(std::vector<java::lang::Object*>* , int , int );

    template <typename T>
    static Collection<T>* synchronizedCollection(Collection<T>* );

    template <typename T>
    static Collection<T>* synchronizedCollection(Collection<T>* , java::lang::Object* );

    template <typename T>
    static std::vector<T>* synchronizedList(std::vector<T>* );

    template <typename T>
    static std::vector<T>* synchronizedList(std::vector<T>* , java::lang::Object* );

    template <typename K, typename V>
    static Map<K, V>* synchronizedMap(Map<K, V>* );

    template <typename K, typename V>
    static NavigableMap<K, V>* synchronizedNavigableMap(NavigableMap<K, V>* );

    template <typename T>
    static NavigableSet<T>* synchronizedNavigableSet(NavigableSet<T>* );

    template <typename T>
    static std::unordered_set<T>* synchronizedSet(std::unordered_set<T>* );

    template <typename T>
    static std::unordered_set<T>* synchronizedSet(std::unordered_set<T>* , java::lang::Object* );

    template <typename K, typename V>
    static SortedMap<K, V>* synchronizedSortedMap(SortedMap<K, V>* );

    template <typename T>
    static SortedSet<T>* synchronizedSortedSet(SortedSet<T>* );

    template <typename T>
    static Collection<T>* unmodifiableCollection(Collection<java::lang::Object*>* );

    template <typename T>
    static std::vector<T>* unmodifiableList(std::vector<java::lang::Object*>* );

    template <typename K, typename V>
    static Map<K, V>* unmodifiableMap(Map<java::lang::Object*, java::lang::Object*>* );

    template <typename K, typename V>
    static NavigableMap<K, V>* unmodifiableNavigableMap(NavigableMap<K, java::lang::Object*>* );

    template <typename T>
    static NavigableSet<T>* unmodifiableNavigableSet(NavigableSet<T>* );

    template <typename T>
    static std::unordered_set<T>* unmodifiableSet(std::unordered_set<java::lang::Object*>* );

    template <typename K, typename V>
    static SortedMap<K, V>* unmodifiableSortedMap(SortedMap<K, java::lang::Object*>* );

    template <typename T>
    static SortedSet<T>* unmodifiableSortedSet(SortedSet<T>* );

    template <typename T>
    static std::vector<T>* zeroLengthArray(java::lang::Class<T>* );


};//class Collections

}//namespace java
}//namespace util
