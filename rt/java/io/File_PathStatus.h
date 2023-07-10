#pragma once


namespace java {
    namespace io {

        class PathStatus {
        public:
            //static std::vector<PathStatus *> *$VALUES;
            static PathStatus *CHECKED;
            static PathStatus *INVALID;

        public:
            PathStatus();

            static PathStatus *valueOf(std::string *);

            static std::vector<PathStatus *> *values();


        };//class PathStatus

    }//namespace java
}//namespace io
