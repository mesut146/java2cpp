# java2cpp

A Java → C++ source-to-source transpiler.

It reads a directory of `.java` files, parses them with the Eclipse JDT parser (with binding resolution), and writes a compilable starting point: C++ headers, sources, forward declarations, library stubs and a `CMakeLists.txt`. The goal is output that needs as few manual fixes as possible — it is **not** a JVM or a complete Java runtime.

> Generated code is a scaffold, not a finished port. Expect to review `log.txt` and adjust the result by hand.

## How it works

Conversion runs in two passes over all discovered sources:

1. **Pre-pass** (`PreVisitor`) builds a global class model — types, methods, fields, inheritance, inner-class relationships — so forward references and library classes can be resolved before any code is emitted.
2. **Generation pass** writes declarations (`HeaderWriter`) and method bodies (`SourceVisitor`).

The C++ generator emits C++ text straight into a `Code` buffer.

## Features

- **Forward declarations** — a single `common.h` declares all source and library classes.
- **Library headers** — referenced classes on the classpath get stub headers under `lib/` so you can implement the missing parts yourself.
- **Inner, anonymous and local classes** — inner classes receive a reference to their enclosing instance; anonymous and local classes become generated structs in the `.cpp`. Effectively-final locals they capture are stored as fields and passed to the generated constructor; enclosing fields are reached via `_parent_ref`, including qualified `Outer.this`, `Outer.super.method()` and `Outer.super.field`.
- **Static blocks** — transformed into a static `si` / `si_init` method. Call it from your own entry point.
- **Records, sealed classes and pattern switches** — records become value structs with accessors plus auto-generated `toString`/`equals`/`hashCode`; sealed interfaces are plain interfaces; pattern switches become if/`dynamic_cast` chains.

- **Boxing** — primitives assigned to `Object` are wrapped in a generated polymorphic `j2cpp::Box<T>` (defined in `common.h`), and casts back to wrapper types unbox with `j2cpp::unbox<T>`. Mapped `Number` targets use `CppHelper::Number`. Both rewrites are annotated and logged.
- **Interfaces** — become abstract C++ classes with public virtual bases and `= 0` for abstract declarations; default methods stay concrete (and overridable), static/private methods are never virtual, and interface constants become `constexpr`. `implements` and interface casts use ordinary C++ inheritance and `dynamic_cast`.
- **Constants** — `static final` primitives with a constant value become `constexpr`.
- **try/catch/finally** — translated to C++ `try`/`catch` with `finally` modeled by `j2cpp::try_finally_value` / `try_finally_void`: `finally` runs on normal completion, early return and exceptions, and a `return`/`throw` in `finally` overrides the try result or pending exception. With try-with-resources, resources close before `finally` runs, including on exceptions.
- **Mappers** — replace Java runtime types with native equivalents (`List<T>` → `std::vector`, `String` → `std::string`, etc.) and rewrite method calls accordingly. See [Mappers](#mappers).
- **CMake integration** — a `CMakeLists.txt` is generated next to the output.

## Build

Requires **JDK 17** to build (the project targets Java 16 source level) and Maven 3.8+:

```sh
mvn clean package
```

The runnable jar is `target/java2cpp-1.0-jar-with-dependencies.jar`. CI runs the same command.

## Command line

```
java -jar target/java2cpp-1.0-jar-with-dependencies.jar \
    -src <java-dir> -out <output-dir> [options]
```

| Option | Description |
| --- | --- |
| `-src`, `-source` | Source directory containing `.java` files (required) |
| `-out`, `-output` | Directory for generated files (required) |
| `-cp`, `-classpath` | Classpath jar or source directory; may be repeated |
| `-mapper <path>` | Additional mapper JSON; resolved relative to the working directory or `-src`; may be repeated |
| `-stats` | Collect call statistics into `stats.txt` instead of converting |
| `-no-mappers` | Disable mappers: keep Java types and generate library stubs instead |
| `-help`, `--help` | Print usage |

Examples:

```sh
# basic C++ conversion
java -jar java2cpp.jar -src ./java -out ./cpp

# with classpath jars and a custom mapper
java -jar java2cpp.jar -src ./java -out ./cpp \
    -cp ./rt.jar -cp ./libs/foo.jar -mapper ./my-mapper.json
```

The source path must be an existing directory; missing required arguments print usage instead of failing with an exception.

## Output layout

```
<out>/
├── CMakeLists.txt
├── common.h                  # forward declarations
├── include/                  # headers (only when Config.separateInclude = true)
├── lib/                      # generated stubs for classpath classes
├── src/                      # generated .cpp files, mirroring packages
└── log.txt                   # unresolved bindings, mapper warnings, errors
```

## Mappers

A mapper is a small JSON file describing a Java class, its C++ target, and method rewrites. Bundled mappers live in `src/main/resources/cpp/`.

Enabled by default: `list`, `map`, `set`, `stack`, `string`, `arrays`, `Boolean`, `Integer`, `sys`, `math`, `primitives`, `double`, `float`, `long`, `short`, `stringbuilder`, `objects`, `optional`, `random`, `uuid`. These cover the common collection/String APIs plus `Math`, boxed primitives, `StringBuilder`, `Objects`, `Optional` (including `ofNullable`), `Random`, `UUID`, `Number` (boxed primitives), `String.split(regex, limit)` and extra `String.valueOf` overloads, `Arrays.copyOfRange`, console I/O (`System.out`/`System.err` print to real C++ streams), filesystem APIs (`File`, `Path`, `Paths`, `Files` → `<filesystem>`), stream types (`BufferedReader`, `FileReader`, `PrintWriter`, `Scanner`) with try-with-resources, and `Throwable`/`Exception` with working `getMessage`, `getCause`, `addSuppressed` and `printStackTrace` (concrete class name and demangled native stack frames). Thrown exceptions are temporaries; other objects are not freed (no GC). Mappers may also supply constant field values (e.g. `Math.PI`, `System.out`).

Example mapper:

```json
{
  "name": "java.util.List<T>,java.util.ArrayList<T>",
  "target": "std::vector",
  "include": "j2cpp_arr.h",
  "methods": [
    { "name": "add(<T>)", "target": "push_back($1)" },
    { "name": "get(int)", "target": "at($1)" },
    { "name": "contains(java.lang.Object)",
      "target": "(${varName}->find($1) != ${varName}->end())",
      "external": true,
      "include": "<algorithm>" }
  ]
}
```

Placeholder rules:

- `$0` / `${varName}` — the receiver expression (both spellings are accepted in C++ mappers).
- `$1`, `$2`, … — call arguments, rendered in the target language.
- `external: true` — the target is a complete expression; no receiver prefix is added.
- `expr` — the resulting expression when `target` is a list of statements; the last line is used otherwise. Multi-line targets are emitted before the statement containing the call.
- `warning` — written to `log.txt`.
- `include` — class-level or method-level include, written into the generated source.

Mappers only cover part of a Java API; anything unmapped falls back to generated classes and library stubs.

## Using it as a library

```java
Converter converter = new Converter("path/to/java", "path/to/cpp");
converter.addClasspath("path/to/rt.jar");
Mapper.instance.initMappers(false);
converter.convert();
```

`Cmd.main(String[])` is the CLI entry point; `Main` is a thin facade kept for the build configurations.

## Configuration

`com.mesut.j2cpp.Config` holds static switches; set them before calling `convert()` when using the API. Notable ones:

| Setting | Default | Effect |
| --- | --- | --- |
| `useMappers` | `true` | Map Java runtime types to native C++ equivalents; `false` keeps Java types and stub headers |
| `writeLibHeader` | `true` | Generate header stubs for classpath classes |
| `common_forwards` / `include_common_forwards` | `true` | Generate and include `common.h` |
| `separateInclude` | `false` | Put headers in an `include/` subdirectory |
| `fields_in_constructors` | `true` | Move field initializers into constructors |
| `static_field_cofui` | `true` | Initialize static fields with construct-on-first-use |
| `ptr_parameter`, `ptr_method`, `ptr_field`, `ptr_typeArg` | `true` | Represent Java references as C++ pointers |
| `mainClass` | `null` | Class whose static initializer becomes `si_init` |
| `type_int`, `type_char`, … | C++ primitives | Mapping for Java primitive types |

## Project layout

```
src/main/java/com/mesut/j2cpp/
├── Converter.java        # orchestration and output writing
├── Cmd.java / Main.java  # command line entry points
├── Config.java           # generation switches
├── ast/                  # C++ declaration model
├── cppast/               # C++ expression and statement model
├── visitor/              # JDT visitors (headers, sources, helpers)
├── map/                  # class map and JSON mappers
└── util/                 # dependency, type and naming helpers
cppRt/                    # small hand-written helper files
```

## Tests

```sh
mvn test
```

Tests under `src/test/java` exercise the CLI, helpers and sample conversions. `CppCompileTest` runs `g++ -fsyntax-only` over generated C++ — including every bundled sample source — and several behavior tests compile and **run** generated code to verify try/finally, switch, anonymous classes, inner-class parent references, String mapper operations, boxing/List/array behavior, static initializers (call `si()`), multi-catch/rethrow, anonymous/local classes capturing locals, `assert`, and switch expressions. All skip if `g++` is unavailable. Hand-written Java snippets live in `src/test/resources/samples/base/` and `samples/mapper/`; they are data files, not compiled tests, and are used by `ConversionTest` through the classpath.

## Runtime support

Mappers are the default: Java runtime types become native C++ equivalents and `LibHandler` generates stub headers under `lib/` for anything unmapped, including `lib/lib_common.h` (referenced by the generated `common.h`). Use `-no-mappers` (or `Config.useMappers = false`) to keep Java types and target that stub-based workflow; generated sources still compile with `g++`. Stub methods are emitted header-only with default bodies. A growing hand-written runtime under `jre/` replaces generated stubs in `-no-mappers` mode: currently `Object`, `String`, `Number`, `Integer`, `Boolean`, `List` and `ArrayList` have real C++ implementations. Classes listed in `jre/implemented.txt` skip stub generation and are included from `<out>/jre`; the rest remain compile-only stubs. `JreRuntimeTest` verifies that a converted program's output matches the real JVM.

- `cppRt/j2cpp_*.h` — domain-specific runtime helpers; add `cppRt/` to your include path:

  | Header | Provides |
  | --- | --- |
  | `j2cpp_str.h` | `format`, `concat`, `StringBuilder`, `split`/`replace`/`join`, string conversions |
  | `j2cpp_num.h` | boxed primitive helpers, `Number`, `boxNumber` |
  | `j2cpp_obj.h` | `Objects` helpers, record `equals`/`hashCode`/`toString` helpers |
  | `j2cpp_rand.h` | `Random`, `Math.random`, `UUID` |
  | `j2cpp_arr.h` | `make1`–`make4`/`from`, `toArray`, `arraycopy`, bit/integer helpers, `arrays::` alias |
  | `j2cpp_fs.h` | `File`/`Path`/`Files` via `<filesystem>` |
  | `j2cpp_io.h` | `System.out`/`err`, readers, scanner, writers |
  | `j2cpp_ex.h` | `Throwable`, stack traces, `ScopeGuard` |
  | `j2cpp_ctrl.h` | `try_finally_value` / `try_finally_void` |
  | `CppHelper.h` | compatibility umbrella including all of the above |
- Generated `.cpp` files include `common.h`, the mapper-required headers, and their own header, so they compile standalone against the generated `lib/` stubs. The converter copies the `cppRt/` headers into the output directory and adds that directory to the generated `CMakeLists.txt`, so `cmake .. && cmake --build .` works without extra include flags.

## Limitations

- This is a syntactic transpiler: no garbage collection, reflection, threads, or full Java standard library is provided. Generated `lib/` stubs and `cppRt/` helpers cover only part of what real Java code needs.
- Java generic type arguments are preserved in generated signatures and class templates, but method-level type variables become C++ method templates (`template <class T>`) so `T` is deduced at call sites; types that cannot be templated fall back to erasure at the leftmost bound. Every such rewrite is marked with a `/* j2cpp: ... */` comment in the generated C++ and recorded in `log.txt`. Wildcard type arguments in parameters become template parameters (`W0`, `W1`, …), so `List<Integer>` is accepted where `List<? extends Number>` is expected; each rewrite is annotated and logged. Upper bounds are enforced with `j2cpp::wildcard_ok` / `static_assert`, including mapped primitives (which Java boxes to `Number`); unbounded and `? super` wildcards are left unchecked.
- Some Java constructs are only partially supported. `synchronized` is logged and approximated. Check `log.txt` after conversion for warnings.
