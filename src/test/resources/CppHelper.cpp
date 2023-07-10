#include <string>

static std::string* replace(std::string& s, const std::string& from, const std::string& to){
  std::string* res = new std::string();
  return res;
}

static std::string* encode(std::string& s, const std::string& encoding){
  std::string* res = new std::string();
  return res;
}

template <typename Args>
static std::string format(std::string& format, Args... args){
    size_t size = snprintf( nullptr, 0, format.c_str(), args ... ) + 1; // Extra space for '\0'
    if( size <= 0 ){ throw std::runtime_error( "Error during formatting." ); }
    std::unique_ptr<char[]> buf( new char[ size ] );
    snprintf( buf.get(), size, format.c_str(), args ... );
    return std::string( buf.get(), buf.get() + size - 1 ); // We don't want the '\0' inside
}