#pragma once


namespace java{
namespace util{
namespace jar{

class Attributes: public java::util::Map<java::lang::Object*, java::lang::Object*>, public java::lang::Cloneable{
//fields
public:
    java::util::Map<java::lang::Object*, java::lang::Object*>* map;

//methods
public:
    Attributes();

    Attributes(int );

    Attributes(Attributes* );

    void clear();

    java::lang::Object* clone();

    bool containsKey(java::lang::Object* );

    bool containsValue(java::lang::Object* );

    std::unordered_set<java::util::Entry<java::lang::Object*, java::lang::Object*>*>* entrySet();

    bool equals(java::lang::Object* );

    java::lang::Object* get(java::lang::Object* );

    std::string* getValue(std::string* );

    std::string* getValue(Name* );

    int hashCode();

    bool isEmpty();

    std::unordered_set<java::lang::Object*>* keySet();

    java::lang::Object* put(java::lang::Object* , java::lang::Object* );

    void putAll(java::util::Map<java::lang::Object*, java::lang::Object*>* );

    std::string* putValue(std::string* , std::string* );

    void read(FastInputStream* , std::vector<char>* );

    java::lang::Object* remove(java::lang::Object* );

    int size();

    java::util::Collection<java::lang::Object*>* values();

    void write(java::io::DataOutputStream* );

    void writeMain(java::io::DataOutputStream* );


};//class Attributes

}//namespace java
}//namespace util
}//namespace jar
