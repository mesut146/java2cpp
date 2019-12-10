#include "test.h"


com::android::dex::Annotation::Annotation(Dex* dex,byte visibility,EncodedValue* encodedAnnotation){
    this::dex=dex;
    this::visibility=visibility;
    this::encodedAnnotation=encodedAnnotation;
}

byte com::android::dex::Annotation::getVisibility(){
    return visibility;
}

EncodedValueReader* com::android::dex::Annotation::getReader(){
    return new EncodedValueReader(encodedAnnotation,ENCODED_ANNOTATION);
}

int com::android::dex::Annotation::getTypeIndex(){
    EncodedValueReader* reader=getReader();
    reader::readAnnotation();
    return reader::getAnnotationType();
}

void com::android::dex::Annotation::writeTo(Dex::Section* out){
    out::writeByte(visibility);
    encodedAnnotation::writeTo(out);
}

int com::android::dex::Annotation::compareTo(Annotation* other){
    return encodedAnnotation::compareTo(other::encodedAnnotation);
}

String* com::android::dex::Annotation::toString(){
    return dex==nullptr?visibility+new String(" ")+getTypeIndex():visibility+new String(" ")+dex::typeNames()::get(getTypeIndex());
}
