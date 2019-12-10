template <class T> class java_array{
    public:
        int length;
        T* elems;
        
        java_array(){
            this->length=0;
            this->elems=nullptr;
        }
        
        java_array(T[],int);
        
        /*java_array(T list[],int l){
            length=l;
            elems=new T[length];
            for(int i=0;i<length;i++){
                elems[i]=list[i];
            }
        }*/
        
        //java_array(T[],int);
        
        T* get(int index);//if T primitive then no pointer
        
        void init();
        //T& operator[] (int) const;
};
