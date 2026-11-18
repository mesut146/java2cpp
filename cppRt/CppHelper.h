#pragma once

//Compatibility umbrella: includes every j2cpp runtime helper. New code should
//include only the j2cpp_*.h header it needs; generated sources and the bundled
//mappers use the narrow headers.
#include "j2cpp_str.h"
#include "j2cpp_num.h"
#include "j2cpp_obj.h"
#include "j2cpp_rand.h"
#include "j2cpp_arr.h"
#include "j2cpp_fs.h"
#include "j2cpp_io.h"
#include "j2cpp_ex.h"
#include "j2cpp_ctrl.h"
