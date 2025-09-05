# Gerador de Programas Java Aleatórios com Placeholders

Este projeto tem como objetivo gerar programas Java automaticamente a partir de esqueletos contendo **placeholders** de tipo. O sistema realiza a substituição desses placeholders por valores aleatórios válidos, compila os arquivos gerados, e executa-os para validar se o programa resultante é funcional.

---

## 📁 Estrutura do Projeto

```
.
├── src/
│   ├── main/
│   │   └── java/
│   │       ├── br/edu/ifsc/javarg/         # Contém a MainClass com placeholders
│   │       └── br/edu/ifsc/javargexamples/ # Classes auxiliares usadas como exemplos
│   └── test/
│       └── java/
│           └── br/edu/ifsc/javarg/         # MainTest com métodos de substituição e teste
├── TestesGerados/                          # Programas Java gerados automaticamente
│   └── br/edu/ifsc/javarg/                 # .class resultantes das compilações
└── README.md
```

---

## 🧱 Como funcionam os esqueletos

O ponto de partida são **esqueletos de código Java** que contêm **placeholders com tipagem explícita**, por exemplo:

```java
int x = __int__valorX;
boolean cond = __boolean__condicao;
A a = new A(__int__a1, __boolean__a2);
```

Estes placeholders têm o formato `__Tipo__nome`, onde:

- `Tipo` é o tipo da variável (`int`, `double`, `boolean`, ou uma classe)
- `nome` é um identificador para que o mesmo valor seja reutilizado se necessário

---

## ⚙️ Classe MainTest

A classe `MainTest` contém os métodos principais do pipeline de geração:

### 🔄 Substituição de Placeholders

- `processPlaceholders(String path)`  
  Carrega um esqueleto `.java`, identifica todos os placeholders e os substitui por valores válidos.

- `generateExpressionForType(String tipo)`  
  Gera uma expressão aleatória de acordo com o tipo (`int`, `boolean`, `double`, ou instâncias de classes importadas).

### 📁 Salvamento e Organização

- `saveGeneratedCode(CompilationUnit cu)`  
  Salva o código resultante com o nome `MainClass_X.java`, onde X é incremental, dentro da pasta `TestesGerados`.

### 🔨 Compilação

- `compileWithJavac(File file)`  
  Utiliza o compilador do Amazon Corretto para compilar o arquivo `.java` gerado. Os arquivos `.class` resultantes são armazenados automaticamente na subpasta `TestesGerados/br/...`.

### ▶️ Execução

- `runGeneratedClass(String fullyQualifiedName)`  
  Executa o `.class` compilado utilizando `java` e a `classpath` correta, e retorna se a execução foi bem-sucedida.

### 🔁 Teste em Lote

- `TestCodeGenerationBatch(int n)`  
  Gera, compila e executa `n` programas automaticamente, exibindo quantos foram bem-sucedidos.

---

## ✅ Por que compilar e executar?

É essencial compilar e executar cada programa gerado porque:

- **A compilação garante** que a substituição gerou código Java sintaticamente válido.
- **A execução garante** que o programa não lança exceções em tempo de execução.
- Isso permite validar a robustez do sistema de geração e a qualidade do código gerado automaticamente.

---

## 🧪 Exemplo de uso

```java
@Property(tries = 1)
public void TestCodeGenerationPipeline() throws Exception {
    CompilationUnit generatedCode = processPlaceholders(SKELETON_PATH);
    File generatedFile = saveGeneratedCode(generatedCode);
    compileWithJavac(generatedFile);
}
```

Para testes em massa:

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
    System.out.println("Executados com sucesso: " + success);
    System.out.println("Falhas na execução: " + failure);
}
```

---

## 🧠 Considerações finais

Atualmente, o sistema é mais eficiente com tipos primitivos e classes que usam apenas membros primitivos em seus construtores. O objetivo futuro é expandir a capacidade de geração e substituição para estruturas mais complexas e permitir maior variedade nos esqueletos.