
namespace ns1{

    class car{
        public:
        
        int year;
        
        car(int y){
            year=y;
        }
        void print();
        
        class inner{
            public: int price;
            inner(int p){
                price=p;
            }
        };
    
    };

}


