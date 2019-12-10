#include <iostream>

using namespace std;

void line(const char* str){
    cout<<str<<endl;
}

int main(){
    try{
        line("in try");
        auto ret=true;
        if(ret){
            fu();
        }
    }catch(int e){
        line("in catch");
    }
    auto fu=[](int i){
        line("in f");
    };
}
