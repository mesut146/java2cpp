#include "test.h"


com::android::dex::Dex::Dex(byte data):Dex(ByteBuffer::wrap(data)){

}

com::android::dex::Dex::Dex(ByteBuffer* data){
    this->data=data;
    this->data::order(ByteOrder::LITTLE_ENDIAN);
    this->tableOfContents::readFrom(this);
}

com::android::dex::Dex::Dex(int byteCount){
    this->data=ByteBuffer::wrap(new java_array_single<byte>(new int[]{byteCount},1));
    this->data::order(ByteOrder::LITTLE_ENDIAN);
}

com::android::dex::Dex::Dex(InputStream* in){

    bool tryReturned=true,finReturned=true;
    void* res_tryBlock=with_finally([&](){
        try{
    loadFrom(in);
}
        catch(int x){}
        if(0){return nullptr;}
        tryReturned=false
    },[&](){{
    in::close();
}
        if(0){return nullptr;}
        finReturned=false
    });
    if(tryReturned||finReturned){
        return res_tryBlock;
        }
    }

com::android::dex::Dex::Dex(File* file){
    if(FileUtils::hasArchiveSuffix(file::getName())){
        ZipFile* zipFile=new ZipFile(file);
        ZipEntry* entry=zipFile::getEntry(DexFormat::DEX_IN_JAR_NAME);
        if(entry!=nullptr){
            try(InputStream* inputStream=zipFile::getInputStream(entry)){
                loadFrom(inputStream);
            }
            zipFile::close();
        }else{
            throw new DexException(new String("Expected ")+DexFormat::DEX_IN_JAR_NAME+new String(" in ")+file)
        }
    }else if(file::getName()::endsWith(new String(".dex"))){
        try(InputStream* inputStream=new FileInputStream(file)){
            loadFrom(inputStream);
        }
    }else{
        throw new DexException(new String("unknown output extension: ")+file)
    }
}

void com::android::dex::Dex::loadFrom(InputStream* in){
    ByteArrayOutputStream* bytesOut=new ByteArrayOutputStream();
    byte buffer=new java_array_single<byte>(new int[]{8192},1);
    int count;
    while(count=in::read(buffer)!=-1){
        bytesOut::write(buffer,0,count);
    }
    this->data=ByteBuffer::wrap(bytesOut::toByteArray());
    this->data::order(ByteOrder::LITTLE_ENDIAN);
    this->tableOfContents::readFrom(this);
}

static void com::android::dex::Dex::checkBounds(int index,int length){
    if(index<0||index>=length){
        throw new IndexOutOfBoundsException(new String("index:")+index+new String(", length=")+length)
    }
}

void com::android::dex::Dex::writeTo(OutputStream* out){
    byte buffer=new java_array_single<byte>(new int[]{8192},1);
    ByteBuffer* data=this->data::duplicate();
    data::clear();
    while(data::hasRemaining()){
        int count=Math::min(buffer::length,data::remaining());
        data::get(buffer,0,count);
        out::write(buffer,0,count);
    }
}

void com::android::dex::Dex::writeTo(File* dexOut){
    try(OutputStream* out=new FileOutputStream(dexOut)){
        writeTo(out);
    }
}

TableOfContents* com::android::dex::Dex::getTableOfContents(){
    return tableOfContents;
}

Section* com::android::dex::Dex::open(int position){
    if(position<0||position>=data::capacity()){
        throw new IllegalArgumentException(new String("position=")+position+new String(" length=")+data::capacity())
    }
    ByteBuffer* sectionData=data::duplicate();
    sectionData::order(ByteOrder::LITTLE_ENDIAN);
    sectionData::position(position);
    sectionData::limit(data::capacity());
    return new Section(new String("section"),sectionData);
}

Section* com::android::dex::Dex::appendSection(int maxByteCount,String* name){
    if(maxByteCount&3!=0){
        throw new IllegalStateException(new String("Not four byte aligned!"))
    }
    int limit=nextSectionStart+maxByteCount;
    ByteBuffer* sectionData=data::duplicate();
    sectionData::order(ByteOrder::LITTLE_ENDIAN);
    sectionData::position(nextSectionStart);
    sectionData::limit(limit);
    Section* result=new Section(name,sectionData);
    nextSectionStart=limit;
    return result;
}

int com::android::dex::Dex::getLength(){
    return data::capacity();
}

int com::android::dex::Dex::getNextSectionStart(){
    return nextSectionStart;
}

byte com::android::dex::Dex::getBytes(){
    ByteBuffer* data=this->data::duplicate();
    byte result=new java_array_single<byte>(new int[]{data::capacity()},1);
    data::position(0);
    data::get(result);
    return result;
}

List* com::android::dex::Dex::strings(){
    return strings;
}

List* com::android::dex::Dex::typeIds(){
    return typeIds;
}

List* com::android::dex::Dex::typeNames(){
    return typeNames;
}

List* com::android::dex::Dex::protoIds(){
    return protoIds;
}

List* com::android::dex::Dex::fieldIds(){
    return fieldIds;
}

List* com::android::dex::Dex::methodIds(){
    return methodIds;
}

Iterable* com::android::dex::Dex::classDefs(){
    return new ClassDefIterable();
}

TypeList* com::android::dex::Dex::readTypeList(int offset){
    if(offset==0){
        return TypeList::EMPTY;
    }
    return open(offset)::readTypeList();
}

ClassData* com::android::dex::Dex::readClassData(ClassDef* classDef){
    int offset=classDef::getClassDataOffset();
    if(offset==0){
        throw new IllegalArgumentException(new String("offset == 0"))
    }
    return open(offset)::readClassData();
}

Code* com::android::dex::Dex::readCode(Method* method){
    int offset=method::getCodeOffset();
    if(offset==0){
        throw new IllegalArgumentException(new String("offset == 0"))
    }
    return open(offset)::readCode();
}

byte com::android::dex::Dex::computeSignature(){
    MessageDigest* digest;
    try{
        digest=MessageDigest::getInstance(new String("SHA-1"));
    }catch(NoSuchAlgorithmException e){
        throw new AssertionError()
    }
    byte buffer=new java_array_single<byte>(new int[]{8192},1);
    ByteBuffer* data=this->data::duplicate();
    data::limit(data::capacity());
    data::position(SIGNATURE_OFFSET+SIGNATURE_SIZE);
    while(data::hasRemaining()){
        int count=Math::min(buffer::length,data::remaining());
        data::get(buffer,0,count);
        digest::update(buffer,0,count);
    }
    return digest::digest();
}

int com::android::dex::Dex::computeChecksum(){
    Adler32* adler32=new Adler32();
    byte buffer=new java_array_single<byte>(new int[]{8192},1);
    ByteBuffer* data=this->data::duplicate();
    data::limit(data::capacity());
    data::position(CHECKSUM_OFFSET+CHECKSUM_SIZE);
    while(data::hasRemaining()){
        int count=Math::min(buffer::length,data::remaining());
        data::get(buffer,0,count);
        adler32::update(buffer,0,count);
    }
    return adler32::getValue();
}

void com::android::dex::Dex::writeHashes(){
    open(SIGNATURE_OFFSET)::write(computeSignature());
    open(CHECKSUM_OFFSET)::writeInt(computeChecksum());
}

int com::android::dex::Dex::descriptorIndexFromTypeIndex(int typeIndex){
    checkBounds(typeIndex,tableOfContents->typeIds::size);
    int position=tableOfContents->typeIds::off+SizeOf::TYPE_ID_ITEM*typeIndex;
    return data::getInt(position);
}

Section(String* name,ByteBuffer* data){
    this->name=name;
    this->data=data;
    this->initialPosition=data::position();
}

int getPosition(){
    return data::position();
}

int readInt(){
    return data::getInt();
}

short readShort(){
    return data::getShort();
}

int readUnsignedShort(){
    return readShort()&0xffff;
}

byte readByte(){
    return data::get();
}

byte readByteArray(int length){
    byte result=new java_array_single<byte>(new int[]{length},1);
    data::get(result);
    return result;
}

short readShortArray(int length){
    if(length==0){
        return EMPTY_SHORT_ARRAY;
    }
    short result=new java_array_single<short>(new int[]{length},1);
    for(int i=0;i<length;i++){
        iresult=readShort();
    }
    return result;
}

int readUleb128(){
    return Leb128::readUnsignedLeb128(this);
}

int readUleb128p1(){
    return Leb128::readUnsignedLeb128(this)-1;
}

int readSleb128(){
    return Leb128::readSignedLeb128(this);
}

void writeUleb128p1(int i){
    writeUleb128(i+1);
}

TypeList* readTypeList(){
    int size=readInt();
    short types=readShortArray(size);
    alignToFourBytes();
    return new TypeList(this,types);
}

String* readString(){
    int offset=readInt();
    int savedPosition=data::position();
    int savedLimit=data::limit();
    data::position(offset);
    data::limit(data::capacity());

    bool tryReturned=true,finReturned=true;
    void* res_tryBlock=with_finally([&](){
        try{
    int expectedLength=readUleb128();
    String* result=Mutf8::decode(this,new java_array_single<char>(new int[]{expectedLength},1));
    if(result::length()!=expectedLength){
        throw new DexException(new String("Declared length ")+expectedLength+new String(" doesn't match decoded length of ")+result::length())
    }
    return result;
}catch(UTFDataFormatException e){
            throw new DexException(e)
        }
        if(0){return nullptr;}
        tryReturned=false
    },[&](){{
    data::position(savedPosition);
    data::limit(savedLimit);
}
        if(0){return nullptr;}
        finReturned=false
    });
    if(tryReturned||finReturned){
        return res_tryBlock;
        }
    }

FieldId* readFieldId(){
    int declaringClassIndex=readUnsignedShort();
    int typeIndex=readUnsignedShort();
    int nameIndex=readInt();
    return new FieldId(this,declaringClassIndex,typeIndex,nameIndex);
}

MethodId* readMethodId(){
    int declaringClassIndex=readUnsignedShort();
    int protoIndex=readUnsignedShort();
    int nameIndex=readInt();
    return new MethodId(this,declaringClassIndex,protoIndex,nameIndex);
}

ProtoId* readProtoId(){
    int shortyIndex=readInt();
    int returnTypeIndex=readInt();
    int parametersOffset=readInt();
    return new ProtoId(this,shortyIndex,returnTypeIndex,parametersOffset);
}

CallSiteId* readCallSiteId(){
    int offset=readInt();
    return new CallSiteId(this,offset);
}

MethodHandle* readMethodHandle(){
    MethodHandleType* methodHandleType=MethodHandleType::fromValue(readUnsignedShort());
    int unused1=readUnsignedShort();
    int fieldOrMethodId=readUnsignedShort();
    int unused2=readUnsignedShort();
    return new MethodHandle(this,methodHandleType,unused1,fieldOrMethodId,unused2);
}

ClassDef* readClassDef(){
    int offset=getPosition();
    int type=readInt();
    int accessFlags=readInt();
    int supertype=readInt();
    int interfacesOffset=readInt();
    int sourceFileIndex=readInt();
    int annotationsOffset=readInt();
    int classDataOffset=readInt();
    int staticValuesOffset=readInt();
    return new ClassDef(this,offset,type,accessFlags,supertype,interfacesOffset,sourceFileIndex,annotationsOffset,classDataOffset,staticValuesOffset);
}

Code* readCode(){
    int registersSize=readUnsignedShort();
    int insSize=readUnsignedShort();
    int outsSize=readUnsignedShort();
    int triesSize=readUnsignedShort();
    int debugInfoOffset=readInt();
    int instructionsSize=readInt();
    short instructions=readShortArray(instructionsSize);
    Try* tries;
    CatchHandler* catchHandlers;
    if(triesSize>0){
        if(instructions::length%2==1){
            readShort();
        }
        Section* triesSection=open(data::position());
        skip(triesSize*SizeOf::TRY_ITEM);
        catchHandlers=readCatchHandlers();
        tries=triesSection::readTries(triesSize,catchHandlers);
    }else{
        tries=new java_array_single<Try>(new int[]{0},1);
        catchHandlers=new java_array_single<CatchHandler>(new int[]{0},1);
    }
    return new Code(registersSize,insSize,outsSize,debugInfoOffset,instructions,tries,catchHandlers);
}

CatchHandler* readCatchHandlers(){
    int baseOffset=data::position();
    int catchHandlersSize=readUleb128();
    CatchHandler* result=new java_array_single<CatchHandler>(new int[]{catchHandlersSize},1);
    for(int i=0;i<catchHandlersSize;i++){
        int offset=data::position()-baseOffset;
        iresult=readCatchHandler(offset);
    }
    return result;
}

Try* readTries(int triesSize,CatchHandler* catchHandlers){
    Try* result=new java_array_single<Try>(new int[]{triesSize},1);
    for(int i=0;i<triesSize;i++){
        int startAddress=readInt();
        int instructionCount=readUnsignedShort();
        int handlerOffset=readUnsignedShort();
        int catchHandlerIndex=findCatchHandlerIndex(catchHandlers,handlerOffset);
        iresult=new Try(startAddress,instructionCount,catchHandlerIndex);
    }
    return result;
}

int findCatchHandlerIndex(CatchHandler* catchHandlers,int offset){
    for(int i=0;i<catchHandlers::length;i++){
        CatchHandler* catchHandler=icatchHandlers;
        if(catchHandler::getOffset()==offset){
            return i;
        }
    }
    throw new IllegalArgumentException()
}

CatchHandler* readCatchHandler(int offset){
    int size=readSleb128();
    int handlersCount=Math::abs(size);
    int typeIndexes=new java_array_single<int>(new int[]{handlersCount},1);
    int addresses=new java_array_single<int>(new int[]{handlersCount},1);
    for(int i=0;i<handlersCount;i++){
        itypeIndexes=readUleb128();
        iaddresses=readUleb128();
    }
    int catchAllAddress=size<=0?readUleb128():-1;
    return new CatchHandler(typeIndexes,addresses,catchAllAddress,offset);
}

ClassData* readClassData(){
    int staticFieldsSize=readUleb128();
    int instanceFieldsSize=readUleb128();
    int directMethodsSize=readUleb128();
    int virtualMethodsSize=readUleb128();
    ClassData.Field* staticFields=readFields(staticFieldsSize);
    ClassData.Field* instanceFields=readFields(instanceFieldsSize);
    ClassData.Method* directMethods=readMethods(directMethodsSize);
    ClassData.Method* virtualMethods=readMethods(virtualMethodsSize);
    return new ClassData(staticFields,instanceFields,directMethods,virtualMethods);
}

ClassData.Field* readFields(int count){
    ClassData.Field* result=new java_array_single<ClassData.Field>(new int[]{count},1);
    int fieldIndex=0;
    for(int i=0;i<count;i++){
        fieldIndex+=readUleb128();
        int accessFlags=readUleb128();
        iresult=new ClassData.Field(fieldIndex,accessFlags);
    }
    return result;
}

ClassData.Method* readMethods(int count){
    ClassData.Method* result=new java_array_single<ClassData.Method>(new int[]{count},1);
    int methodIndex=0;
    for(int i=0;i<count;i++){
        methodIndex+=readUleb128();
        int accessFlags=readUleb128();
        int codeOff=readUleb128();
        iresult=new ClassData.Method(methodIndex,accessFlags,codeOff);
    }
    return result;
}

byte getBytesFrom(int start){
    int end=data::position();
    byte result=new java_array_single<byte>(new int[]{end-start},1);
    data::position(start);
    data::get(result);
    return result;
}

Annotation* readAnnotation(){
    byte visibility=readByte();
    int start=data::position();
    new EncodedValueReader(this,EncodedValueReader::ENCODED_ANNOTATION)::skipValue();
    return new Annotation(this,visibility,new EncodedValue(getBytesFrom(start)));
}

EncodedValue* readEncodedArray(){
    int start=data::position();
    new EncodedValueReader(this,EncodedValueReader::ENCODED_ARRAY)::skipValue();
    return new EncodedValue(getBytesFrom(start));
}

void skip(int count){
    if(count<0){
        throw new IllegalArgumentException()
    }
    data::position(data::position()+count);
}

void alignToFourBytes(){
    data::position(data::position()+3&~3);
}

void alignToFourBytesWithZeroFill(){
    while(data::position()&3!=0){
        data::put(0);
    }
}

void assertFourByteAligned(){
    if(data::position()&3!=0){
        throw new IllegalStateException(new String("Not four byte aligned!"))
    }
}

void write(byte bytes){
    this->data::put(bytes);
}

void writeByte(int b){
    data::put(b);
}

void writeShort(short i){
    data::putShort(i);
}

void writeUnsignedShort(int i){
    short s=i;
    if(i!=s&0xffff){
        throw new IllegalArgumentException(new String("Expected an unsigned short: ")+i)
    }
    writeShort(s);
}

void write(short shorts){
    for(short s:shorts){
        writeShort(s);
    }
}

void writeInt(int i){
    data::putInt(i);
}

void writeUleb128(int i){
    try{
        Leb128::writeUnsignedLeb128(this,i);
    }catch(ArrayIndexOutOfBoundsException e){
        throw new DexException(new String("Section limit ")+data::limit()+new String(" exceeded by ")+name)
    }
}

void writeSleb128(int i){
    try{
        Leb128::writeSignedLeb128(this,i);
    }catch(ArrayIndexOutOfBoundsException e){
        throw new DexException(new String("Section limit ")+data::limit()+new String(" exceeded by ")+name)
    }
}

void writeStringData(String* value){
    try{
        int length=value::length();
        writeUleb128(length);
        write(Mutf8::encode(value));
        writeByte(0);
    }catch(UTFDataFormatException e){
        throw new AssertionError()
    }
}

void writeTypeList(TypeList* typeList){
    short types=typeList::getTypes();
    writeInt(types::length);
    for(short type:types){
        writeShort(type);
    }
    alignToFourBytesWithZeroFill();
}

int used(){
    return data::position()-initialPosition;
}

String* get(int index){
    checkBounds(index,tableOfContents::stringIds::size);
    return open(tableOfContents::stringIds::off+index*SizeOf::STRING_ID_ITEM)::readString();
}

int size(){
    return tableOfContents::stringIds::size;
}

Integer get(int index){
    return descriptorIndexFromTypeIndex(index);
}

int size(){
    return tableOfContents::typeIds::size;
}

String* get(int index){
    return strings::get(descriptorIndexFromTypeIndex(index));
}

int size(){
    return tableOfContents::typeIds::size;
}

ProtoId* get(int index){
    checkBounds(index,tableOfContents::protoIds::size);
    return open(tableOfContents::protoIds::off+SizeOf::PROTO_ID_ITEM*index)::readProtoId();
}

int size(){
    return tableOfContents::protoIds::size;
}

FieldId* get(int index){
    checkBounds(index,tableOfContents::fieldIds::size);
    return open(tableOfContents::fieldIds::off+SizeOf::MEMBER_ID_ITEM*index)::readFieldId();
}

int size(){
    return tableOfContents::fieldIds::size;
}

MethodId* get(int index){
    checkBounds(index,tableOfContents::methodIds::size);
    return open(tableOfContents::methodIds::off+SizeOf::MEMBER_ID_ITEM*index)::readMethodId();
}

int size(){
    return tableOfContents::methodIds::size;
}

boolean hasNext(){
    return count<tableOfContents::classDefs::size;
}

ClassDef* next(){
    if(!hasNext()){
        throw new NoSuchElementException()
    }
    count++;
    return in::readClassDef();
}

void remove(){
    throw new UnsupportedOperationException()
}

Iterator* iterator(){
    return !tableOfContents::classDefs::exists()?Collections::emptySet()::iterator():new ClassDefIterator();
}
