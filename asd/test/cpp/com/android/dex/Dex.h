#pragma once

#include "com/android/dex/Code/CatchHandler.h"
#include "com/android/dex/Code/Try.h"
#include "com/android/dex/MethodHandle/MethodHandleType.h"
#include "com/android/dex/util/ByteInput.h"
#include "com/android/dex/util/ByteOutput.h"
#include "com/android/dex/util/FileUtils.h"
#include "java/io/ByteArrayOutputStream.h"
#include "java/io/File.h"
#include "java/io/FileInputStream.h"
#include "java/io/FileOutputStream.h"
#include "java/io/IOException.h"
#include "java/io/InputStream.h"
#include "java/io/OutputStream.h"
#include "java/io/UTFDataFormatException.h"
#include "java/nio/ByteBuffer.h"
#include "java/nio/ByteOrder.h"
#include "java/security/MessageDigest.h"
#include "java/security/NoSuchAlgorithmException.h"
#include "java/util/AbstractList.h"
#include "java/util/Collections.h"
#include "java/util/Iterator.h"
#include "java/util/List.h"
#include "java/util/NoSuchElementException.h"
#include "java/util/RandomAccess.h"
#include "java/util/zip/Adler32.h"
#include "java/util/zip/ZipEntry.h"
#include "java/util/zip/ZipFile.h"

using namespace com::java::lang;
using namespace com::android::dex;

namespace com::android::dex{
    class Dex{
        private:
            static int CHECKSUM_OFFSET=8;
            static int CHECKSUM_SIZE=4;
            static int SIGNATURE_OFFSET=CHECKSUM_OFFSET+CHECKSUM_SIZE;
            static int SIGNATURE_SIZE=20;
            static java_array_single<short>* EMPTY_SHORT_ARRAY=new java_array_single<short>(new int[]{0},1);
            java_array<java_array<java_array_single<Object>>>* obj3=new java_array<java_array<java_array_single<Object>>>(new int[]{1,2,0},3);
            java_array_single<byte>* str;
            ByteBuffer* data;
            TableOfContents* tableOfContents=new TableOfContents();
            int nextSectionStart=0;
            StringTable* strings=new StringTable();
            TypeIndexToDescriptorIndexTable* typeIds=new TypeIndexToDescriptorIndexTable();
            TypeIndexToDescriptorTable* typeNames=new TypeIndexToDescriptorTable();
            ProtoIdTable* protoIds=new ProtoIdTable();
            FieldIdTable* fieldIds=new FieldIdTable();
            MethodIdTable* methodIds=new MethodIdTable();

        public:
            Dex(java_array_single<byte>* data);
            Dex(java_array<java_array<java_array_single<int>>>* myarr);
            Dex(int byteCount);
            Dex(InputStream* in);
            Dex(File* file);
            void writeTo(OutputStream* out);
            void writeTo(File* dexOut);
            TableOfContents* getTableOfContents();
            Section* open(int position);
            Section* appendSection(int maxByteCount,String* name);
            int getLength();
            int getNextSectionStart();
            java_array_single<byte>* getBytes();
            List<String>* strings();
            List<Integer>* typeIds();
            List<String>* typeNames();
            List<ProtoId>* protoIds();
            List<FieldId>* fieldIds();
            List<MethodId>* methodIds();
            Iterable<ClassDef>* classDefs();
            TypeList* readTypeList(int offset);
            ClassData* readClassData(ClassDef* classDef);
            Code* readCode(Method* method);
            java_array_single<byte>* computeSignature();
            int computeChecksum();
            void writeHashes();
            int descriptorIndexFromTypeIndex(int typeIndex);
        private:
            Dex(ByteBuffer* data);
            void loadFrom(InputStream* in);
            static void checkBounds(int index,int length);

        class Section:ByteInput,ByteOutput{
            private:
                String* name;
                ByteBuffer* data;
                int initialPosition;

            public:
                int getPosition();
                int readInt();
                short readShort();
                int readUnsignedShort();
                byte readByte();
                java_array_single<byte>* readByteArray(int length);
                java_array_single<short>* readShortArray(int length);
                int readUleb128();
                int readUleb128p1();
                int readSleb128();
                void writeUleb128p1(int i);
                TypeList* readTypeList();
                String* readString();
                FieldId* readFieldId();
                MethodId* readMethodId();
                ProtoId* readProtoId();
                CallSiteId* readCallSiteId();
                MethodHandle* readMethodHandle();
                ClassDef* readClassDef();
                Annotation* readAnnotation();
                EncodedValue* readEncodedArray();
                void skip(int count);
                void alignToFourBytes();
                void alignToFourBytesWithZeroFill();
                void assertFourByteAligned();
                void write(java_array_single<byte>* bytes);
                void writeByte(int b);
                void writeShort(short i);
                void writeUnsignedShort(int i);
                void write(java_array_single<short>* shorts);
                void writeInt(int i);
                void writeUleb128(int i);
                void writeSleb128(int i);
                void writeStringData(String* value);
                void writeTypeList(TypeList* typeList);
                int used();
            private:
                Section(String* name,ByteBuffer* data);
                Code* readCode();
                java_array_single<CatchHandler>* readCatchHandlers();
                java_array_single<Try>* readTries(int triesSize,java_array_single<CatchHandler>* catchHandlers);
                int findCatchHandlerIndex(java_array_single<CatchHandler>* catchHandlers,int offset);
                CatchHandler* readCatchHandler(int offset);
                ClassData* readClassData();
                java_array_single<Field>* readFields(int count);
                java_array_single<Method>* readMethods(int count);
                java_array_single<byte>* getBytesFrom(int start);

        };//class Section

        class StringTable:AbstractList,RandomAccess{

            public:
                String* get(int index);
                int size();

        };//class StringTable

        class TypeIndexToDescriptorIndexTable:AbstractList,RandomAccess{

            public:
                Integer get(int index);
                int size();

        };//class TypeIndexToDescriptorIndexTable

        class TypeIndexToDescriptorTable:AbstractList,RandomAccess{

            public:
                String* get(int index);
                int size();

        };//class TypeIndexToDescriptorTable

        class ProtoIdTable:AbstractList,RandomAccess{

            public:
                ProtoId* get(int index);
                int size();

        };//class ProtoIdTable

        class FieldIdTable:AbstractList,RandomAccess{

            public:
                FieldId* get(int index);
                int size();

        };//class FieldIdTable

        class MethodIdTable:AbstractList,RandomAccess{

            public:
                MethodId* get(int index);
                int size();

        };//class MethodIdTable

        class ClassDefIterator:Iterator{
            private:
                Dex.Section* in=open(tableOfContents->classDefs->off);
                int count=0;

            public:
                boolean hasNext();
                ClassDef* next();
                void remove();

        };//class ClassDefIterator

        class ClassDefIterable:Iterable{

            public:
                Iterator<ClassDef>* iterator();

        };//class ClassDefIterable

    };//class Dex
}//ns
