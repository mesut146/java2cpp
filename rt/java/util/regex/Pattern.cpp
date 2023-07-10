#include "Pattern.h"
#include <stdexcept>

using namespace java::util::regex;


Matcher *Pattern::matcher(std::string *text) {
    throw std::runtime_error("Pattern::matcher");
}

Pattern *Pattern::compile(std::string *regex) {
    return new Pattern(regex, 0);
}

Pattern::Pattern(std::string *regex, int flags) {
    this->pattern_renamed = regex;
    this->flags_renamed = flags;

}