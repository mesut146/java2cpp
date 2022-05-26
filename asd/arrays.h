#pragma once

#include <vector>

typedef std::vector<T>* t1;
typedef std::vector<t1>* t2;

template <class T>
std::vector<std::vector<T>*>* make2d(int a, int b){
  auto res = new std::vector<std::vector<T>*>(a);
  for(int i=0;i<a;i++){
    res->at(i) = new std::vector<T>(b);
  }
}

template <class T>
std::vector<std::vector<std::vector<T>*>*>* make3d(int a, int b, int c){
  auto res = new std::vector<std::vector<std::vector<T>*>*>(a);
  for(int i=0;i<a;i++){
    res->at(i) = make2d(b, c);
  }
}

template <class T>
std::vector<std::vector<std::vector<T>*>*>* make4d(int a, int b, int c, int d){
  auto res = new std::vector<std::vector<std::vector<T>*>*>(a);
  for(int i=0;i<a;i++){
    res->at(i) = make3d(b, c, d);
  }
}
