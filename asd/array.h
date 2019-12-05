class array<T>{
    public:
        int length;
        T** elems;
        T* get(int index);//if T primitive then no pointer
        void init();
};
