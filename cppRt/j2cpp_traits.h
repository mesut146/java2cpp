#pragma once

//compile-time checks used by generated code
#include <type_traits>

//java.lang.Number is declared by j2cpp_num.h when present; a forward
//declaration is enough for the primitive-boxing check below
namespace CppHelper { class Number; }

namespace j2cpp {

//A wildcard's deduced element type is acceptable when it is the bound itself,
//derives from it, or is a mapped primitive (which Java boxes to Number).
template <typename T, typename Bound>
struct wildcard_ok {
    using value_type = typename std::remove_pointer<T>::type;
    using bound_type = typename std::remove_pointer<Bound>::type;
    static constexpr bool value =
            std::is_same<value_type, bound_type>::value
            || std::is_convertible<value_type*, bound_type*>::value
            || (std::is_arithmetic<value_type>::value
                && std::is_same<bound_type, CppHelper::Number>::value);
};

} //namespace j2cpp
