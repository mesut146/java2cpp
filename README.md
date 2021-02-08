# java2cpp

yet another converter for java to c++ 

it takes a source directory containing java files and outputs corresponding c++ headers and sources by transforming declarations, statements and expressions

The resulting c++ code should be good enough to compile with the least work
## Features

### forward class declarations
creates single header that contains all class declarations for source and libraries 

### write headers for library usages
creates headers with classes and methods for library usages
### anonymous & inner classes
Anonymous classes handled by simply creating a class with a dummy name

It can move inner & anonymous classes to upper levels or separate headers to avoid namespace conflict. 

### mapper
Instead of creating library headers you can command to use mappers to convert java runtime to equivalent c++ types and functions (java.util.List to std::vector,java.lang.String to std::string etc.)


## command line usage
````
java -jar java2cpp.jar -src javaDir -out cppDir -cp rt.jar -cp lib2.jar ...