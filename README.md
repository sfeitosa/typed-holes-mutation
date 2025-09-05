# Leveraging Typed-holes and Random Code Generation for Test Case Mutation

This paper explores the use of typed holes to mutate incomplete programs or test cases using random code generation in a type-directed manner in a way that can be useful for applying them to unit- or property-based tests. More precisely, the process follows a structured pipeline: First, missing segments of the source code are annotated with their expected types. Next, the system parses the annotated code to identify the typed-holes and extract the surrounding context. Finally, a randomized expression generator synthesizes and inserts candidate expressions to fill each typed-hole. Our evaluation involved mutating 10 base programs, producing 1,000 mutations per program. Every generated variant passed compilation and execution checks on three distinct Java compiler versions.

---

## 📁 Project Structure
```
.
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── br/edu/ifsc/javarg/         # Contains the MainClass with placeholders
│   │       └── br/edu/ifsc/javargexamples/ # Aux. Classes used as exemples
│   └── test/
│       └── java/
│           └── br/edu/ifsc/javarg/         # MainTest with methods to replace and test
├── TestesGerados/                          
│   └── br/edu/ifsc/javarg/                 # Resulting .class 
└── README.md
```

---

## 🧱 How Code Skeletons Work

The starting point is Java code skeletons that contain explicitly typed placeholders, for example:

```java
int x = __int__valX;
boolean cond = __boolean__cond;
A a = new A(__int__a1, __boolean__a2);
```

These placeholders follow the format `__Tipo__nome`, where:

- `Type` is the variable type (`int`, `double`, `boolean`, or a class)
- `name` is an identifier so the same value can be reused if needed

---

## ⚙️ MainTest Class

The `MainTest` class contains the main methods of the generation pipeline:

### 🔄 Placeholder Replacement

- `processPlaceholders(String path)`  
  Loads a `.java` skeleton, identifies all placeholders, and replaces them with valid values.

- `generateExpressionForType(String type)`  
  Generates a random expression according to the type (`int`, `boolean`, `double`, or instances of imported classes).

### 📁 Saving and Organization

- `saveGeneratedCode(CompilationUnit cu)`  
  Saves the resulting code with the name `MainClass_X.java`, where X is incremental, within the `TestesGerados` folder.

### 🔨 Compilation

- `compileWithJavac(File file)`  
  Uses the Amazon Corretto compiler to compile the generated `.java` file. The resulting `.class` files are automatically stored in the `TestesGerados/br/...` subfolder.

### ▶️ Execution

- `runGeneratedClass(String fullyQualifiedName)`  
  Executes the compiled `.class` using `java` and the correct `classpath`, and returns whether the execution was successful.

### 🔁 Batch Testing

- `TestCodeGenerationBatch(int n)`  
  Automatically generates, compiles, and executes `n` programs, displaying how many were successful.

---

## ✅ Why Compile and Execute?

It is essential to compile and execute each generated program because:

- **Compilation ensures** that the substitution produced syntactically valid Java code.
- **Execution ensures** that the program does not throw runtime exceptions.
- This validates the robustness of the generation system and the quality of the automatically generated code.

---

## 🧪 Example of Use

```java
@Property(tries = 1)
public void TestCodeGenerationPipeline() throws Exception {
    CompilationUnit generatedCode = processPlaceholders(SKELETON_PATH);
    File generatedFile = saveGeneratedCode(generatedCode);
    compileWithJavac(generatedFile);
}
```

For batch testing:

```java
@Property(tries = 1)
public void TestCodeGenerationBatch() throws Exception {
    int success = 0, failure = 0;
    for (int i = 1; i <= 1000; i++) {
        File generatedFile = saveGeneratedCode(processPlaceholders(SKELETON_PATH));
        if (compileWithJavac(generatedFile)) {
            boolean ran = runGeneratedClass("br.edu.ifsc.javarg.MainClass_" + i);
            if (ran) success++;
            else failure++;
        }
    }
    System.out.println("Success execution: " + success);
    System.out.println("Faling execution: " + failure);
}
```

---
