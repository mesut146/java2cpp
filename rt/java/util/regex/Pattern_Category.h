#pragma once


namespace java{
namespace util{
namespace regex{

class Category: public CharProperty{
//fields
public:
    int typeMask;

//methods
public:
    Category(int );

    bool isSatisfiedBy(int );


};//class Category

}//namespace java
}//namespace util
}//namespace regex
