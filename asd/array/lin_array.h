#include <iostream>

using namespace std;

template <typename T>
class lin_array
{
    public:


        lin_array(){

        }

    
         void init(){
            if(typeid(T)==typeid(int)){
                cout<<"it is int"<<endl;
                return;
            }
            //T=lin_array<lin_array<int>>
            
        }


};
