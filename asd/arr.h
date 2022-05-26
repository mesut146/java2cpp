template <class T>
struct arr{
  T* ptr;
  int size;

  arr(int a, int b){
    ptr = (T*)malloc(a * b);
  }

  arr<T> get(int i){
    return arr{ptr, size};
  }
};


new arr<int>(5,4)