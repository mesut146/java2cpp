#pragma once

#include <map>
#include <vector>
#include <string>
#include "common.h"
#include "Matcher.h"

namespace java {
    namespace util {
        namespace regex {

            class Pattern {
            public:
                static int CANON_EQ;
                static int CASE_INSENSITIVE;
                static int COMMENTS;
                static int DOTALL;
                static int GREEDY;
                static int INDEPENDENT;
                static int LAZY;
                static int LITERAL;
                static int MAX_REPS;
                static int MULTILINE;
                static int POSSESSIVE;
                static int UNICODE_CASE;
                static int UNICODE_CHARACTER_CLASS;
                static int UNIX_LINES;
                //static Node *accept_renamed;
                std::vector<int> *buffer;
                int capturingGroupCount;
                bool compiled;
                int cursor_renamed;
                int flags_renamed;
                //std::vector<GroupHead *> *groupNodes;
                bool hasSupplementary;
                //static Node *lastAccept;
                int localCount;
                //static Node *lookbehindEnd;
                // Node *matchRoot;
                //std::map<std::string *, java::lang::Integer *> *namedGroups_renamed;
                std::string *normalizedPattern;
                std::string *pattern_renamed;
                int patternLength;
                //Node *root;
                static long serialVersionUID;
                std::vector<int> *temp;

            public:
                Pattern(std::string *, int);

                void RemoveQEQuoting();

                void accept(int, std::string *);

                void addFlag();

                void append(int, int);

                //Node *atom();

                //CharProperty *bitsOrSingle(BitClass *, int);

                int c();

                //CharProperty *caseInsensitiveRangeFor(int, int);

                //CharProperty *charPropertyNodeFor(std::string *);

                //CharProperty *clazz(bool);

                //Node *closure(Node *);

                void compile();

                static Pattern *compile(std::string *);

                static Pattern *compile(std::string *, int);

                std::string *composeOneStep(std::string *);

                //static int countChars(java::lang::CharSequence *, int, int);

                //static int countCodePoints(java::lang::CharSequence *);

                //Node *createGroup(bool);

                int cursor();

                int escape(bool, bool, bool);

                //Node *expr(Node *);

                //CharProperty *family(bool, bool);

                bool findSupplementary(int, int);

                int flags();

                int getClass(int);

                //Node *group0();

                std::string *groupname(int);

                bool has(int);

                //static bool hasBaseCharacter(Matcher *, int, java::lang::CharSequence *);

                static bool inRange(int, int, int);

                //static CharProperty *intersection(CharProperty *, CharProperty *);

                bool isLineSeparator(int);

                static bool isSupplementary(int);

                void mark(int);

                Matcher *matcher(std::string *);

                //static bool matches(std::string *, java::lang::CharSequence *);

                //java::util::Map<std::string *, java::lang::Integer *> *namedGroups();

                //CharProperty *newSingle(int);

                //Node *newSlice(std::vector<int> *, int, bool);

                int next();

                int nextEscaped();

                void normalize();

                //int normalizeCharClass(java::lang::StringBuilder *, int);

                int o();

                int parsePastLine();

                int parsePastWhitespace(int);

                std::string *pattern();

                int peek();

                int peekPastLine();

                int peekPastWhitespace(int);

                //static void printObjectTree(Node *);

                std::string *produceEquivalentAlternation(std::string *);

                std::vector<std::string *> *producePermutations(std::string *);

                static std::string *quote(std::string *);

                //CharProperty *range(BitClass *);

                //static CharProperty *rangeFor(int, int);

                int read();

                int readEscaped();

                //Node *ref(int);

                //Node *sequence(Node *);

                //static CharProperty *setDifference(CharProperty *, CharProperty *);

                void setcursor(int);

                int skip();

                //std::vector<std::string *> *split(java::lang::CharSequence *);

                //std::vector<std::string *> *split(java::lang::CharSequence *, int);

                void subFlag();

                std::string *toString();

                int u();

                //CharProperty *unicodeBlockPropertyFor(std::string *);

                //CharProperty *unicodeScriptPropertyFor(std::string *);

                //static CharProperty *union_renamed(CharProperty *, CharProperty *);

                void unread();

                int uxxxx();

                int x();


            };//class Pattern

        }//namespace java
    }//namespace util
}//namespace regex
