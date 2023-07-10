#pragma once

#include <vector>
#include <string>
#include "common.h"
#include "Pattern.h"

namespace java {
    namespace util {
        namespace regex {

            class MatchResult {
            };


            class Matcher : public MatchResult {
//fields
            public:
                static int ENDANCHOR;
                static int NOANCHOR;
                int acceptMode;
                bool anchoringBounds;
                int first;
                int from;
                std::vector<int> *groups;
                bool hitEnd_renamed;
                int last;
                int lastAppendPosition;
                std::vector<int> *locals;
                int lookbehindTo;
                int oldLast;
                Pattern *parentPattern;
                bool requireEnd_renamed;
                std::string *text;
                int to;
                bool transparentBounds;

//methods
            public:
                Matcher();

                Matcher(Pattern *, std::string *);

                //Matcher *appendReplacement(java::lang::StringBuffer *, std::string *);

                //java::lang::StringBuffer *appendTail(java::lang::StringBuffer *);

                wchar_t charAt(int);

                int end();

                int end(int);

                int end(std::string *);

                bool find();

                bool find(int);

                int getMatchedGroupIndex(std::string *);

                //java::lang::CharSequence *getSubSequence(int, int);

                int getTextLength();

                std::string *group();

                std::string *group(int);

                std::string *group(std::string *);

                int groupCount();

                bool hasAnchoringBounds();

                bool hasTransparentBounds();

                bool hitEnd();

                bool lookingAt();

                bool match(int, int);

                bool matches();

                Pattern *pattern();

                static std::string *quoteReplacement(std::string *);

                Matcher *region(int, int);

                int regionEnd();

                int regionStart();

                std::string *replaceAll(std::string *);

                std::string *replaceFirst(std::string *);

                bool requireEnd();

                Matcher *reset();

                Matcher *reset(java::lang::CharSequence *);

                bool search(int);

                int start();

                int start(int);

                int start(std::string *);

                MatchResult *toMatchResult();

                std::string *toString();

                Matcher *useAnchoringBounds(bool);

                Matcher *usePattern(Pattern *);

                Matcher *useTransparentBounds(bool);


            };//class Matcher

        }//namespace java
    }//namespace util
}//namespace regex
