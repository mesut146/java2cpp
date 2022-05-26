#pragma once


namespace java{
namespace lang{

class ThreadLocalMap
{
//fields
public:
    static int INITIAL_CAPACITY;
    int size;
    std::vector<Entry*>* table;
    int threshold;

//methods
public:
    ThreadLocalMap(ThreadLocalMap* );

    ThreadLocalMap(ThreadLocal<Object*>* , Object* );

    bool cleanSomeSlots(int , int );

    void expungeStaleEntries();

    int expungeStaleEntry(int );

    Entry* getEntry(ThreadLocal<Object*>* );

    Entry* getEntryAfterMiss(ThreadLocal<Object*>* , int , Entry* );

    static int nextIndex(int , int );

    static int prevIndex(int , int );

    void rehash();

    void remove(ThreadLocal<Object*>* );

    void replaceStaleEntry(ThreadLocal<Object*>* , Object* , int );

    void resize();

    void set(ThreadLocal<Object*>* , Object* );

    void setThreshold(int );


};//class ThreadLocalMap

}//namespace java
}//namespace lang
