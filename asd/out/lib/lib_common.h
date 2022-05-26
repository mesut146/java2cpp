#pragma once


//forward declarations
namespace java{
    namespace lang{
        class System;
        class Integer;
        class Character;
        class Short;
        class Cloneable;
        class CharSequence;
        class Exception;
        class Throwable;
        template <typename T>
        class Iterable;
        class String;
        class Number;
        class RuntimeException;
        class Object;
        template <typename T>
        class Comparable;
    
        namespace constant{
            class ConstantDesc;
            class Constable;
        
        }//namespace constant
    
    }//namespace lang

    namespace util{
        template <typename E>
        class AbstractList;
        template <typename E>
        class AbstractCollection;
        template <typename E>
        class Collection;
        template <typename E>
        class List;
        template <typename E>
        class ArrayList;
        class RandomAccess;
    
    }//namespace util

    namespace io{
        class Serializable;
    
    }//namespace io

}//namespace java


