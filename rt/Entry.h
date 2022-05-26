#pragma once


template<class K, class V>
class Entry {
public:
    K key;
    V value;
    Entry<K, V> *next;
    int hash;

    /**
     * Creates new entry.
     */
    Entry(int h, K k, V v, Entry<K, V> *n) {
        value = v;
        next = n;
        key = k;
        hash = h;
    }


    K getKey() {
        return key;
    }


    V getValue() {
        return value;
    }


    V setValue(V newValue) {
        V oldValue = value;
        value = newValue;
        return oldValue;
    }


    /*bool equals(Object o) {
       if (!(o instanceof
       Map.Entry))
       return false;
       Map.Entry
       e = (Map.Entry)
       o;
       Object k1 = getKey();
       Object k2 = e.getKey();
       if (k1 == k2 || (k1 != null && k1.equals(k2))) {
           Object v1 = getValue();
           Object v2 = e.getValue();
           if (v1 == v2 || (v1 != null && v1.equals(v2)))
               return true;
       }
       return false;
   }*/

    int hashCode() {
        return getKey()->hashCode() ^ getValue()->hashCode();
        //return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }


    /*std::string toString() {
        return getKey() + "=" + getValue();
    }*/

    /*void recordAccess(HashMap<K, V> &m) {
    }*/

    /*void recordRemoval(HashMap<K, V> &m) {
    }*/
};
