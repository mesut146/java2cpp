template <typename T> class java_array{
    public:
        int length;
        T* elems;
        T* get(int index);//if T primitive then no pointer
        void init();
        T operator[] (int i);
};
