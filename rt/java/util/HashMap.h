#pragma once


namespace java{
namespace util{


template <typename K, typename V>
class HashMap: public AbstractMap<K, V>, public Map<K, V>{
//fields
public:
    static int DEFAULT_INITIAL_CAPACITY;
    static float DEFAULT_LOAD_FACTOR;
    static int MAXIMUM_CAPACITY;
    static int MIN_TREEIFY_CAPACITY;
    static int TREEIFY_THRESHOLD;
    static int UNTREEIFY_THRESHOLD;
    std::unordered_set<Entry<K, V>*>* entrySet_renamed;
    float loadFactor_renamed;
    int modCount;
    static long serialVersionUID;
    int size_renamed;
    std::vector<Node<K, V>*>* table;
    int threshold;

//methods
public:
    HashMap();

    HashMap(int );

    HashMap(Map<java::lang::Object*, java::lang::Object*>* );

    HashMap(int , float );

    void afterNodeAccess(Node<K, V>* );

    void afterNodeInsertion(bool );

    void afterNodeRemoval(Node<K, V>* );

    int capacity();

    void clear();

    java::lang::Object* clone();

    static java::lang::Class<java::lang::Object*>* comparableClassFor(java::lang::Object* );

    static int compareComparables(java::lang::Class<java::lang::Object*>* , java::lang::Object* , java::lang::Object* );

    V compute(K , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    V computeIfAbsent(K , java::util::function::Function<java::lang::Object*, java::lang::Object*>* );

    V computeIfPresent(K , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    bool containsKey(java::lang::Object* );

    bool containsValue(java::lang::Object* );

    std::unordered_set<Entry<K, V>*>* entrySet();

    void forEach(java::util::function::BiConsumer<java::lang::Object*, java::lang::Object*>* );

    V get(java::lang::Object* );

    Node<K, V>* getNode(int , java::lang::Object* );

    V getOrDefault(java::lang::Object* , V );

    static int hash(java::lang::Object* );

    void internalWriteEntries(java::io::ObjectOutputStream* );

    bool isEmpty();

    std::unordered_set<K>* keySet();

    float loadFactor();

    V merge(K , V , java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    Node<K, V>* newNode(int , K , V , Node<K, V>* );

    TreeNode<K, V>* newTreeNode(int , K , V , Node<K, V>* );

    V put(K , V );

    void putAll(Map<java::lang::Object*, java::lang::Object*>* );

    V putIfAbsent(K , V );

    void putMapEntries(Map<java::lang::Object*, java::lang::Object*>* , bool );

    V putVal(int , K , V , bool , bool );

    void readObject(java::io::ObjectInputStream* );

    void reinitialize();

    V remove(java::lang::Object* );

    bool remove(java::lang::Object* , java::lang::Object* );

    Node<K, V>* removeNode(int , java::lang::Object* , java::lang::Object* , bool , bool );

    V replace(K , V );

    bool replace(K , V , V );

    void replaceAll(java::util::function::BiFunction<java::lang::Object*, java::lang::Object*, java::lang::Object*>* );

    Node<K, V>* replacementNode(Node<K, V>* , Node<K, V>* );

    TreeNode<K, V>* replacementTreeNode(Node<K, V>* , Node<K, V>* );

    std::vector<Node<K, V>*>* resize();

    int size();

    static int tableSizeFor(int );

    void treeifyBin(std::vector<Node<K, V>*>* , int );

    Collection<V>* values();

    void writeObject(java::io::ObjectOutputStream* );


};//class HashMap

}//namespace java
}//namespace util
