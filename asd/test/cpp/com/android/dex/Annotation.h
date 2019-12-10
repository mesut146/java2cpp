#pragma once

#include "com::android::dex::EncodedValueReade.h"
#include "com::android::dex::EncodedValueReade.h"

using namespace com::java::lang;
using namespace com::android::dex;

namespace com::android::dex{
    class Annotation:Comparable{

        Dex* dex;
        byte visibility;
        EncodedValue* encodedAnnotation;

        Annotation(Dex* dex,byte visibility,EncodedValue* encodedAnnotation);
        byte getVisibility();
        EncodedValueReader* getReader();
        int getTypeIndex();
        void writeTo(Dex::Section* out);
        int compareTo(Annotation* other);
        String* toString();

    };//class Annotation
}//ns
