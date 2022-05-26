using namespace base;

//class Renamer
void Renamer::useField(){
    auto_renamed=6;
    this->auto_renamed=6;
}
void Renamer::useStatic(){
    auto a = Renamer::bool_renamed;
    auto b = Renamer->bool_renamed;
}
void Renamer::mem(){
}
void Renamer::useMem(){
    mem_renamed=5;
    this->mem_renamed=5;
}
void Renamer::useObject(){
    auto r = this;
    Renamer->auto_renamed=5;
    Renamer->mem_renamed=6;
}
void Renamer::useParam(int typename_renamed){
    typename_renamed=5;
}
