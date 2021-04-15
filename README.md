# java2cpp

yet another converter for java to c++ 

it takes a source directory containing java files and outputs corresponding c++ headers and sources by transforming declarations, statements and expressions

The resulting c++ code should be good enough to compile with the least amount of work
## Features

### forward class declarations
creates single header that contains all class declarations for source and libraries 

### write headers for library usages
creates headers for referenced libraries so that you can implement missing parts

### anonymous & inner classes
Anonymous classes handled by simply creating a class with a dummy name

Inner classes have a reference to enclosing class instance and all references replaced with parent scope

It can move inner classes to upper levels or separate headers to avoid namespace conflict. 
### static block
A static block is transformed into a static method called ``si``, it is your responsibility call this from your main entry point

### mapper
Instead of creating library headers you can use mappers to convert java runtime to equivalent c++ counterparts (List to std::vector, String to std::string etc.)

Pre-added mappers is under src/main/resources/mappers however you can add your mapper by ``-mapper <json>``

Keep in mind that mappers can only transpile a portion of source
## command line usage
````
java -jar java2cpp.jar -src javaDir -out cppDir [-map] [-mapper <mapperpath>] -cp rt.jar -cp lib2.jar ...