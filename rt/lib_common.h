#pragma once

#include <string>
#include <vector>
#include <unordered_set>
#include <unordered_map>
#include <map>
#include <sstream>

//forward declarations
namespace java {
    namespace util {
        template<typename K, typename V>
        class UnmodifiableEntrySet;

        template<typename E>
        class Vector;

        template<typename K, typename V>
        class TreeNode;

        class NaturalOrder;

        template<typename T>
        class PrivateEntryIterator;

        template<typename K, typename V>
        class TreeMap;

        template<typename E>
        class SortedSet;

        class Conversion;

        template<typename E>
        class SerializationProxy;

        class FormatString;

        template<typename K, typename V>
        class SynchronizedMap;

        template<typename K, typename V>
        class EmptyMap;

        class LegacyMergeSort;

        class BitSet;

        template<typename K, typename V>
        class ValueSpliterator;

        template<typename E>
        class EmptyEnumeration;

        template<typename K, typename V>
        class KeySpliterator;

        template<typename E>
        class SingletonSet;

        class FixedString;

        template<typename K, typename V>
        class EmptyNavigableMap;

        class EntrySetView;

        class EntrySet;

        template<typename K, typename V>
        class KeySpliterator;

        template<typename E>
        class UnmodifiableRandomAccessList;

        template<typename K, typename V>
        class CheckedSortedMap;

        class Formatter;

        class AscendingEntrySetView;

        template<typename K, typename V, typename T>
        class CheckedEntry;

        template<typename K, typename V>
        class SortedMap;

        template<typename T>
        class SubMapIterator;

        template<typename E>
        class AbstractSet;

        template<typename K, typename V>
        class DescendingSubMap;

        template<typename E>
        class SynchronizedSet;

        class BigDecimalLayout;

        template<typename K, typename V>
        class SynchronizedNavigableMap;

        class Flags;

        class EntryIterator;

        template<typename E>
        class EmptyList;

        template<typename E>
        class EnumSet;

        class Values;

        template<typename K, typename V>
        class SingletonMap;

        template<typename E>
        class UnmodifiableSet;

        template<typename E>
        class CheckedSortedSet;

        template<typename K, typename V>
        class UnmodifiableEntry;

        template<typename E>
        class ListIterator;

        template<typename E>
        class SynchronizedSortedSet;

        class Locale;

        template<typename T>
        class Spliterator;

        template<typename E>
        class Stack;

        template<typename K, typename V>
        class Node;

        template<typename K, typename V>
        class SimpleEntry;

        template<typename E>
        class UnmodifiableList;

        template<typename K, typename V>
        class EntrySpliterator;

        template<typename E>
        class UnmodifiableCollection;

        template<typename T>
        class Comparator;

        class ListItr;

        template<typename E>
        class AbstractQueue;

        template<typename E>
        class UnmodifiableSortedSet;

        template<typename E>
        class CheckedCollection;

        template<typename E>
        class CheckedQueue;

        class RandomAccess;

        template<typename E>
        class EmptyListIterator;

        class DescendingSubMapKeyIterator;

        template<typename K, typename V>
        class UnmodifiableMap;

        template<typename K, typename V>
        class Entry;

        template<typename K, typename V>
        class SynchronizedSortedMap;

        template<typename E>
        class AbstractList;

        template<typename E>
        class CheckedRandomAccessList;

        template<typename K, typename V>
        class AscendingSubMap;

        template<typename E>
        class VectorSpliterator;

        template<typename E>
        class KeySet;

        template<typename E>
        class EmptyIterator;

        template<typename K, typename V>
        class EntrySpliterator;

        class DateTime;

        class SubMap;

        template<typename K, typename V>
        class ValueSpliterator;

        template<typename K, typename V>
        class HashMap;

        class ValueIterator;

        class ReverseComparator;

        template<typename K, typename V>
        class CheckedMap;

        template<typename E>
        class EmptySet;

        class Collections;

        template<typename E>
        class Enumeration;

        template<typename K, typename V>
        class HashMapSpliterator;

        template<typename K, typename V>
        class UnmodifiableSortedMap;

        template<typename K, typename V>
        class CheckedEntrySet;

        template<typename E>
        class SynchronizedCollection;

        template<typename K, typename V>
        class TreeMapSpliterator;

        template<typename E>
        class CheckedNavigableSet;

        template<typename E>
        class AbstractCollection;

        template<typename K, typename V>
        class NavigableMap;

        template<typename K, typename V>
        class UnmodifiableNavigableMap;

        template<typename K, typename V>
        class Entry;

        //class KeySet;
        class DescendingSubMapEntryIterator;

        class Itr;

        class NoSuchElementException;

        class DescendingEntrySetView;

        template<typename E>
        class CheckedSet;

        class Arrays;

        template<typename E>
        class CheckedList;

        template<typename E>
        class ArrayList;

        class FormatSpecifier;

        class KeyIterator;

        template<typename E>
        class SynchronizedList;

        class DescendingKeyIterator;

        template<typename E>
        class CopiesList;

        class BigDecimalLayoutForm;

        class EntrySet;

        template<typename K, typename V>
        class DescendingKeySpliterator;

        template<typename K, typename V>
        class Map;

        class HashIterator;

        class SubMapEntryIterator;

        template<typename E>
        class AsLIFOQueue;

        template<typename E>
        class SetFromMap;

        template<typename T>
        class ReverseComparator2;

        class Values;

        template<typename E>
        class SingletonList;

        template<typename E>
        class SynchronizedRandomAccessList;

        class EntryIterator;

        template<typename E>
        class Collection;

        template<typename K, typename V>
        class Entry;

        template<typename E>
        class Queue;

        template<typename E>
        class SynchronizedNavigableSet;

        template<typename K, typename V>
        class AbstractMap;

        class SubMapKeyIterator;

        template<typename K, typename V>
        class UnmodifiableEntrySetSpliterator;

        class ValueIterator;

        template<typename K, typename V>
        class NavigableSubMap;

        template<typename K, typename V>
        class CheckedNavigableMap;

        class KeyIterator;

        template<typename E>
        class UnmodifiableNavigableSet;

        template<typename E>
        class NavigableSet;

        template<typename K, typename V>
        class SimpleImmutableEntry;

        template<typename E>
        class Iterator;

        template<typename E>
        class EmptyNavigableSet;

        namespace concurrent {
            template<typename K, typename V>
            class TreeBin;

            template<typename E>
            class BlockingQueue;

            template<typename E>
            class ArrayBlockingQueue;

            template<typename K, typename V, typename U>
            class ForEachTransformedEntryTask;

            class ScheduledExecutorService;

            template<typename T>
            class PrivilegedCallable;

            class ThreadFactory;

            template<typename K, typename V>
            class ValuesView;

            template<typename K, typename V>
            class MapReduceValuesToIntTask;

            template<typename K, typename V, typename U>
            class SearchMappingsTask;

            class FinalizableDelegatedExecutorService;

            class Executor;

            template<typename K, typename V, typename U>
            class MapReduceMappingsTask;

            template<typename K, typename V>
            class MapReduceMappingsToDoubleTask;

            template<typename K, typename V, typename R>
            class BulkTask;

            class CallerRunsPolicy;

            class TimeUnit;

            template<typename K, typename V>
            class MapReduceValuesToDoubleTask;

            template<typename K, typename V>
            class KeyIterator;

            template<typename K, typename V>
            class EntrySetView;

            template<typename K, typename V>
            class MapEntry;

            template<typename K, typename V>
            class KeySpliterator;

            template<typename K, typename V>
            class MapReduceMappingsToIntTask;

            template<typename K, typename V>
            class ReservationNode;

            class CounterCell;

            class DelegatedExecutorService;

            template<typename K, typename V>
            class ForEachMappingTask;

            template<typename K, typename V>
            class ConcurrentMap;

            class ExecutorService;

            template<typename K, typename V, typename U>
            class MapReduceKeysTask;

            template<typename K, typename V>
            class KeySetView;

            class RejectedExecutionHandler;

            template<typename K, typename V>
            class MapReduceEntriesToLongTask;

            template<typename K, typename V>
            class MapReduceKeysToDoubleTask;

            class DelegatedScheduledExecutorService;

            template<typename K, typename V, typename U>
            class ForEachTransformedMappingTask;

            template<typename K, typename V>
            class ForEachValueTask;

            template<typename K, typename V, typename U>
            class ForEachTransformedValueTask;

            class ThreadPoolExecutor;

            template<typename K, typename V>
            class MapReduceMappingsToLongTask;

            template<typename K, typename V, typename U>
            class SearchKeysTask;

            template<typename K, typename V>
            class ForEachEntryTask;

            class PrivilegedThreadFactory;

            template<typename V>
            class Future;

            template<typename K, typename V>
            class EntryIterator;

            class ExecutionException;

            template<typename K, typename V>
            class ReduceKeysTask;

            template<typename K, typename V>
            class MapReduceEntriesToDoubleTask;

            template<typename K, typename V>
            class ReduceValuesTask;

            template<typename K, typename V, typename U>
            class MapReduceValuesTask;

            class Executors;

            template<typename K, typename V, typename U>
            class MapReduceEntriesTask;

            template<typename K, typename V>
            class ForwardingNode;

            template<typename V>
            class Callable;

            template<typename T>
            class PrivilegedCallableUsingCurrentClassLoader;

            template<typename K, typename V>
            class Node;

            template<typename K, typename V>
            class Segment;

            template<typename K, typename V>
            class ValueIterator;

            template<typename K, typename V>
            class MapReduceKeysToIntTask;

            template<typename K, typename V, typename E>
            class CollectionView;

            template<typename K, typename V>
            class Traverser;

            template<typename K, typename V>
            class TreeNode;

            template<typename K, typename V>
            class ValueSpliterator;

            template<typename K, typename V>
            class MapReduceKeysToLongTask;

            template<typename T>
            class CountedCompleter;

            template<typename K, typename V>
            class MapReduceValuesToLongTask;

            template<typename K, typename V>
            class TableStack;

            template<typename K, typename V, typename U>
            class ForEachTransformedKeyTask;

            template<typename K, typename V>
            class BaseIterator;

            template<typename K, typename V>
            class MapReduceEntriesToIntTask;

            template<typename T>
            class RunnableAdapter;

            template<typename K, typename V>
            class ReduceEntriesTask;

            template<typename K, typename V>
            class ConcurrentHashMap;

            class AbstractExecutorService;

            class DefaultThreadFactory;

            template<typename K, typename V, typename U>
            class SearchEntriesTask;

            template<typename K, typename V>
            class ForEachKeyTask;

            template<typename K, typename V, typename U>
            class SearchValuesTask;

            template<typename V>
            class ForkJoinTask;

            template<typename K, typename V>
            class EntrySpliterator;

            namespace locks {
                class Lock;

                class ReentrantLock;

            } //namespace locks

            namespace atomic {
                class AtomicInteger;

            } //namespace atomic

        } //namespace concurrent

        namespace regex {
            class LazyLoop;

            class SliceUS;

            class First;

            class GroupRef;

            class SliceIS;

            class StartS;

            class Bound;

            class Prolog;

            class Loop;

            class Script;

            class Utype;

            class Node;

            class LastNode;

            class UnixDot;

            class Pos;

            class All;

            class Begin;

            class NotBehindS;

            class Neg;

            class CharPropertyNames;

            class Dollar;

            class Caret;

            class Curly;

            class End;

            class GroupHead;

            class GroupCurly;

            class LineEnding;

            class CharPropertyFactory;

            class Single;

            class SliceI;

            class Category;

            class SliceS;

            class SliceU;

            class Conditional;

            class TreeInfo;

            class SliceNode;

            class BmpCharProperty;

            class BnMS;

            class GroupTail;

            class Slice;

            class VertWS;

            class HorizWS;

            class BranchConn;

            class Pattern;

            class Ctype;

            class BitClass;

            class Behind;

            class CIBackRef;

            class UnixDollar;

            class Ques;

            class Branch;

            class Block;

            class BehindS;

            class BnM;

            class BackRef;

            class Start;

            class UnixCaret;

            class Matcher;

            class LastMatch;

            class SingleS;

            class Dot;

            class SingleU;

            class SingleI;

            class CharProperty;

            class NotBehind;

            class MatchResult;

            class CloneableProperty;

        } //namespace regex

        namespace zip {
            class Adler32;

            class DeflaterOutputStream;

            class InflaterInputStream;

            class XEntry;

            class ZipEntry;

            class ZipFileInflaterInputStream;

            class ZipFileInputStream;

            class ZipFile;

            class ZipOutputStream;

            class ZipEntryIterator;

            class ZipConstants;

            class Checksum;

        } //namespace zip

        namespace jar {
            class JarOutputStream;

            class Manifest;

            class FastInputStream;

            class Attributes;

            class JarEntry;

            class Name;

        } //namespace jar

        namespace logging {
            class LoggerBundle;

            class Level;

            class Logger;

            class SystemLoggerHelper;

        } //namespace logging

    } //namespace util

    namespace lang {
        class Integer;

        class CharSequence;

        class StringBuffer;

        class AbstractStringBuilder;

        class Float;

        class Error;

        class AssertionError;

        template<typename T>
        class Iterable;

        class Runnable;

        class Throwable;

        class IllegalStateException;

        class Atomic;

        class Cloneable;

        class IllegalArgumentException;

        class InterruptedException;

        class Character;

        class Exception;

        class RuntimeException;

        class NullPointerException;

        class Math;

        class IndexOutOfBoundsException;

        class Long;

        class StringBuilder;

        template<typename T>
        class ThreadLocal;

        class ThreadLocalMap;

        class Object;

        class System;

        class Double;

        class UnsupportedOperationException;

        template<typename T>
        class Comparable;

    } //namespace lang

    namespace io {
        class Serializable;

        class ByteArrayOutputStream;

        class DataInputStream;

        class PrintStream;

        class StringWriter;

        class BufferedReader;

        class OutputStream;

        class FilterWriter;

        class Closeable;

        class IOException;

        class InputStreamReader;

        class OutputStreamWriter;

        class FileReader;

        class PrintWriter;

        class FileOutputStream;

        class File;

        class EOFException;

        class Flushable;

        class ByteArrayInputStream;

        class DataInput;

        class PathStatus;

        class TempDirectory;

        class FileNotFoundException;

        class Writer;

        class InputStream;

        class UTFDataFormatException;

        class FilterInputStream;

        class FileInputStream;

        class FilterOutputStream;

        class Reader;

    } //namespace io

    namespace nio {
        class Buffer;

        class ByteBuffer;

        class ByteOrder;

    } //namespace nio

    namespace security {
        class MessageDigest;

        class Delegate;

        class MessageDigestSpi;

    } //namespace security

} //namespace java
namespace sun {
    namespace security {
        namespace util {
            class MessageDigestSpi2;

        } //namespace util

    } //namespace security

} //namespace sun
