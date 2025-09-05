package br.edu.ifsc.javargtest;

import br.edu.ifsc.javargtest.JRGLog.Severity;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.*;
import java.lang.String;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
// import com.google.common.base.Optional;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jqwik.api.*;
import net.jqwik.api.lifecycle.BeforeTry;
import org.junit.Test;

import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithParameters;

/**
 *
 * @author samuel
 *
 */
public class MainTests {

    private static final String SKELETON_PATH = "src/main/java/br/edu/ifsc/javarg/MainClass.java";

    private CompilationUnit mSkeleton;

    private ClassTable mCT;

    private JRGBase mBase;

    private JRGCore mCore;

    private JRGStmt mStmt;

    private JRGOperator mOperator;

    private Map<String, String> mCtx;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("__([^_]+)__([\\w]+)"); // Regex para
                                                                                               // __tipo__nome

    private Map<String, Expression> placeholderValues;

    public MainTests() throws FileNotFoundException, IOException {
        mSkeleton = StaticJavaParser.parse(new File(SKELETON_PATH));

        dumpAST();

        JRGLog.logLevel = Severity.MSG_XDEBUG; ////////////////////////////////
    }

    @BeforeTry
    public void createObjects() {
        mCT = new ClassTable(loadImports());

        mBase = new JRGBase(mCT);

        mCore = new JRGCore(mCT, mBase);

        mStmt = new JRGStmt(mCT, mBase, mCore);

        mOperator = new JRGOperator(mCT, mBase, mCore);

        mCtx = new HashMap<String, String>();

        placeholderValues = new HashMap<>();

    }

    @Property(tries = 1)
    public void printarImports() throws ClassNotFoundException, FileNotFoundException {
        List<ImportDeclaration> salam = imports();

        for (ImportDeclaration importDeclaration : salam) {
            System.out.println("nome do import " + importDeclaration.getNameAsString());
        }
        entrarNosImports(salam);
    }

    @Property(tries = 1)
    public void printaEssa() throws ClassNotFoundException, FileNotFoundException {
        List<ClassOrInterfaceDeclaration> classes = mSkeleton.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration classDecl : classes) {
            // Obter nome da classe
            String className = classDecl.getNameAsString();
            System.out.println("nome da classe nessa maldição " + className);
            processClass(classDecl);
        }
    }

    @Property(tries = 1)
    public void entrarNosImports(List<ImportDeclaration> salam) throws ClassNotFoundException, FileNotFoundException {
        String basePath = "src/main/java/";

        System.out.println("Entrou no entrar nos imports");

        for (ImportDeclaration importDeclaration : salam) {
            String importPath = importDeclaration.getNameAsString().replace('.', File.separatorChar) + ".java";
            File importFile = new File(basePath, importPath);

            // Verificação de existência do arquivo
            if (importFile.exists()) {
                try {
                    CompilationUnit importUnit = StaticJavaParser.parse(importFile);
                    List<ClassOrInterfaceDeclaration> classes = importUnit.findAll(ClassOrInterfaceDeclaration.class);
                    for (ClassOrInterfaceDeclaration classDecl : classes) {
                        System.out.println("processando classe" + classDecl.getNameAsString());
                        processClass(classDecl);
                    }
                } catch (IOException e) {
                    System.out.println("Erro ao processar o arquivo: " + importPath);
                    e.printStackTrace();
                }
            } else {
                System.out.println("Arquivo de import não encontrado: " + importPath);
            }
        }
    }

    // novo imports
    @Property(tries = 10)
    List<ImportDeclaration> imports() throws ClassNotFoundException, FileNotFoundException {
        mSkeleton = StaticJavaParser.parse(new File(SKELETON_PATH));

        List<ClassOrInterfaceDeclaration> classes = mSkeleton.findAll(ClassOrInterfaceDeclaration.class);

        for (ClassOrInterfaceDeclaration classDecl : classes) {
            // Obter nome da classe
            String className = classDecl.getNameAsString();
            System.out.println("olaa ");

            // Chamar método para obter métodos, variáveis, etc.
            processClass(classDecl);
        }

        return mSkeleton.getImports();

    }

    private void processClass(ClassOrInterfaceDeclaration classDecl)
            throws ClassNotFoundException, FileNotFoundException {
        // Extrair métodos e salvar em listas
        System.out.println("Entrou no processClass");
        List<MethodDeclaration> methods = classDecl.getMethods();
        List<FieldDeclaration> fields = classDecl.findAll(FieldDeclaration.class);
        List<VariableDeclarator> allVariabless = classDecl.findAll(VariableDeclarator.class);
        List<MethodDeclaration> allMethods = new ArrayList<>();
        List<VariableDeclarator> allVariables = new ArrayList<>();
        List<VariableDeclarator> allfieldVariables = new ArrayList<>();

        // tentei colocar asssim pra ver se conseguia entrar nas variaveis da classe
        for (VariableDeclarator variable : allVariabless) {
            System.out.println("Variavel da classe: " + variable.getNameAsString());
        }

        for (MethodDeclaration method : methods) {

            String className = classDecl.getNameAsString();
            System.out.println("Nome da classe: " + className);

            // Nome do método
            String methodName = method.getNameAsString();
            System.out.println("Nome do método: " + methodName);

            String type = method.getTypeAsString();
            System.out.println("Tipo de retorno: " + type);
            // Parâmetros do método
            for (FieldDeclaration field : fields) {
                List<VariableDeclarator> fieldVariables = field.getVariables();
                for (VariableDeclarator variable : fieldVariables) {
                    allfieldVariables.add(variable);
                    System.out.println("Campo da classe: " + variable.getNameAsString());
                }
            }

            List<Parameter> parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                System.out.println("Parâmetro: " + parameter.getNameAsString());
                System.out.println("Tipo do parâmetro: " + parameter.getTypeAsString());
            }
            // Capturar campos da classe

            // Corpo do método
            BlockStmt body = method.getBody().orElse(null);
            System.out.println("Body: " + body);
            allMethods.add(method);

            if (body != null) {
                // Variáveis locais no método
                System.out.println("entrou no corpo");
                List<VariableDeclarator> variables = body.findAll(VariableDeclarator.class);
                // Aqui você pode armazenar métodos, variáveis, etc. em coleções para uso
                allVariables.addAll(variables);
                for (VariableDeclarator variable : variables) {
                    System.out.println("Variável: " + variable.getNameAsString());
                }

            }
            replaceVariaveis(allVariables, allfieldVariables);
        }
    }

    public void replaceVariaveis(List<VariableDeclarator> allVariables, List<VariableDeclarator> fieldVariables)
            throws ClassNotFoundException {
        Set<String> alreadyReplaced = new HashSet<>();

        // Substituir variáveis locais
        for (VariableDeclarator variable : allVariables) {
            substituirVariavel(variable, alreadyReplaced);
        }

        // Substituir campos da classe
        for (VariableDeclarator fieldVariable : fieldVariables) {
            substituirVariavel(fieldVariable, alreadyReplaced);
        }
    }

    // O JAVA TA SE PERDENDO QUANDO VEM UM VALOR STRING ACHO Q PQ STIRNG É UMA
    // CLASSE E ELE É BEM DO BURRO
    private void substituirVariavel(VariableDeclarator variable, Set<String> alreadyReplaced)
            throws ClassNotFoundException {
        String varName = variable.getNameAsString();

        if (varName.startsWith("replace") && !alreadyReplaced.contains(varName)) {
            // Obter o tipo da variável
            Type type = StaticJavaParser.parseType(variable.getTypeAsString());

            // Gerar expressão com base no tipo
            Arbitrary<Expression> expressionArb = mCore.genExpression(mCtx, type);
            Expression expression = expressionArb.sample();

            // Verifique se a expressão é um literal primitivo (tipo IntegerLiteralExpr,
            // StringLiteralExpr)
            if (expression.isLiteralExpr()) {
                variable.setInitializer(expression);
                System.out.println("Variável substituída: " + varName + " = " + expression);
            } else {
                System.out.println("Expressão gerada não é um literal: " + expression);
            }

            // Marcar a variável como já substituída
            alreadyReplaced.add(varName);
        }
    }

    @Property(tries = 1)
    public List<String> loadImports() {
        NodeList<ImportDeclaration> imports = mSkeleton.getImports();

        List<String> list = new ArrayList<>();

        Iterator<ImportDeclaration> it = imports.iterator();
        while (it.hasNext()) {
            ImportDeclaration i = it.next();
            // System.out.println("Import no loadImports: " + i.getName().asString());
            list.add(i.getName().asString());
        }

        return list;
    }

    // NOVA ABORDAGEM REPLACES SEM DECLARAR
    // private Map<String, Expression> placeholderValues = new HashMap<>();
    // private Map<String, List<String>> variablePlaceholderMap = new HashMap<>();
    // // Armazena a relação variáveis <->
    // // placeholders
    // private Map<String, Expression> methodParameterPlaceholders = new
    // HashMap<>(); // Armazena o valor do placeholder
    // // para parâmetros de métodos

    // public CompilationUnit processPlaceholders() throws FileNotFoundException {
    // File file = new File(SKELETON_PATH);
    // CompilationUnit compilationUnit = StaticJavaParser.parse(file);

    // // Substituindo placeholders declarados no formato __Tipo__nome
    // List<VariableDeclarator> allVariables =
    // compilationUnit.findAll(VariableDeclarator.class);

    // for (VariableDeclarator variable : allVariables) {
    // Optional<Expression> initializerOpt = variable.getInitializer();

    // if (initializerOpt.isPresent()) {
    // Expression initializer = initializerOpt.get();

    // // Verificar se o placeholder é um tipo primitivo ou qualificado
    // if (initializer.toString().startsWith("__")) {
    // // Fazer o split no placeholder
    // String[] parts = initializer.toString().split("__");
    // if (parts.length == 3) {
    // String tipo = parts[1];
    // String nome = parts[2];

    // // Verificar se o tipo é uma classe qualificada (iniciada com "__br")
    // if (tipo.startsWith("br")) {
    // // Placeholder de classe qualificada
    // processClassPlaceholder(initializer, tipo, nome);
    // } else {
    // // Placeholder de tipo primitivo
    // processPrimitivePlaceholder(initializer, tipo, nome);
    // }
    // }
    // }
    // }
    // }

    // // Processar chamadas de método para placeholders
    // processMethodCalls(compilationUnit);

    // return compilationUnit;
    // }

    // private void processPrimitivePlaceholder(Expression initializer, String tipo,
    // String nome) {
    // initializer.walk(expr -> {
    // if (expr instanceof NameExpr) {
    // NameExpr nameExpr = (NameExpr) expr;
    // String name = nameExpr.getNameAsString();

    // // Verificar se o nome segue o padrão __Tipo__nome
    // if (name.startsWith("__") && name.contains("__")) {
    // String[] parts = name.split("__");
    // if (parts.length == 3) {
    // String tipoLocal = parts[1]; // O tipo é extraído do placeholder
    // String nomeLocal = parts[2]; // O nome da variável
    // System.out.println("Nome Local primitivo: "+nomeLocal);

    // // Resolver o tipo de acordo com o valor do tipo
    // Type javaType = StaticJavaParser.parseType(tipoLocal);
    // replacePlaceholder(nameExpr, javaType, nomeLocal);

    // // Adicionar a variável ao mapeamento de placeholders
    // variablePlaceholderMap.putIfAbsent(nomeLocal, new ArrayList<>());
    // variablePlaceholderMap.get(nomeLocal).add(nameExpr.toString());
    // }
    // }
    // }
    // });
    // }

    // private void processClassPlaceholder(Expression initializer, String tipo,
    // String nome) {

    // if (initializer.toString().contains("__")) {
    // System.out.println("Initializer no process"+initializer );
    // String[] parts = initializer.toString().split("__");
    // if (parts.length == 3) {
    // String tipoLocal = parts[1]; // O tipo é extraído do placeholder
    // String nomeLocal = parts[2]; // O nome da variávelvar
    // System.out.println("Nome Local "+nomeLocal);
    // System.out.println("Tipo Local "+tipoLocal);
    // String input = nomeLocal; // A string completa, pode ter qualquer formato

    // // Usar uma expressão regular para capturar qualquer coisa antes de "VAR"
    // String pattern = "(\\w+VAR)"; // \w+ corresponde a qualquer sequência de
    // caracteres (letras/dígitos) antes de "VAR"

    // // Procurar a correspondência
    // java.util.regex.Pattern compiledPattern =
    // java.util.regex.Pattern.compile(pattern);
    // java.util.regex.Matcher matcher = compiledPattern.matcher(input);

    // if (matcher.find()) {
    // String matchedVar = matcher.group(1); // Pega a primeira correspondência que
    // contenha "VAR"
    // System.out.println("Correspondência encontrada: " + matchedVar); // Saída:
    // BVAR
    // NameExpr nameExpr =
    // StaticJavaParser.parseExpression(matchedVar).asNameExpr();

    // } else {
    // System.out.println("Nenhuma correspondência encontrada.");
    // }
    // String gambiarraNomeTipo = tipoLocal + " " + nomeLocal;

    // initializer.walk(expr -> {
    // if (expr instanceof NameExpr) {
    // NameExpr nameExpr = (NameExpr) expr;
    // String name = nameExpr.getNameAsString();
    // System.out.println("Initializer "+initializer );
    // System.out.println("Expr: "+expr);

    // // Verificar se o nome segue o padrão __br.edu.classe__
    // if(expr.toString().contains("__")){
    // Type javaType = resolveType(tipoLocal); // Tipo de classe qualificada
    // // NameExpr nameExpr = (NameExpr) expr;
    // replacePlaceholder(nameExpr, javaType, nomeLocal);

    // // Adicionar a variável ao mapeamento de placeholders
    // variablePlaceholderMap.putIfAbsent(nomeLocal, new ArrayList<>());
    // variablePlaceholderMap.get(nomeLocal).add(nameExpr.toString());

    // }
    // // Resolver o tipo da classe qualificada
    // }
    // });
    // }
    // };
    // }

    // private void replacePlaceholder(NameExpr nameExpr, Type javaType, String
    // nome) {
    // // Verifica se já existe um valor para o placeholder
    // if (placeholderValues.containsKey(nome)) {
    // nameExpr.replace(placeholderValues.get(nome));
    // } else {
    // // Gerar um novo valor e substituir
    // Arbitrary<Expression> newExpression = mCore.genExpression(mCtx, javaType);
    // Expression sampledExpression = newExpression.sample();

    // placeholderValues.put(nome, sampledExpression); // Armazenar o valor gerado
    // nameExpr.replace(sampledExpression); // Substituir o placeholder pela nova
    // expressão
    // }

    // // Certifique-se de não incluir a expressão completa no mapa de variáveis
    // variablePlaceholderMap.putIfAbsent(nome, new ArrayList<>());
    // variablePlaceholderMap.get(nome).add(nome); // Apenas o nome, sem a expressão
    // completa
    // }

    // private Type resolveType(String tipo) {
    // try {
    // // O StaticJavaParser pode identificar o tipo diretamente
    // return StaticJavaParser.parseType(tipo);
    // } catch (Exception e) {
    // throw new RuntimeException("Erro ao processar o tipo: " + tipo, e);
    // }
    // }

    // @Property(tries = 1)
    // public void testaEssaPorra() {

    // Arbitrary<Expression> exp = mCore.genExpression(mCtx,
    // StaticJavaParser.parseType("int"));
    // System.out.println(exp.sample());
    // }

    // private Arbitrary<Expression> generateExpressionForType(Type type) {
    // if (type.isPrimitiveType()) {
    // // Para tipos primitivos, chamamos o gerador de expressões correspondente
    // return mCore.genExpression(mCtx, type);
    // } else if (type.isClassOrInterfaceType()) {
    // String typeName = type.asClassOrInterfaceType().getNameAsString();

    // // Se for uma String, geramos uma StringLiteralExpr

    // if (typeName.equals("String")) {

    // // Converter LiteralExpr para Expression
    // return mCore.genExpression(mCtx, type);

    // // return mBase.genPrimitiveString().map(expr -> (Expression) expr);
    // // return Arbitraries.strings().map(StringLiteralExpr::new);
    // }
    // if (typeName.equals("Expression")) {

    // // se usar object creation nao vai encontrar a classe EXPRESSION, pq ela não
    // // existe
    // // teria que criar um gerador? talvez um statement?
    // System.out.println(mStmt.genStatement(mCtx).sample()); // esse print da nulo
    // // return mCore.genObjectCreation().map(expr -> (Expression) expr);

    // }

    // // Para outros tipos de classe, criamos uma expressão de instanciamento (new
    // // C())
    // // AQUI
    // // return Arbitraries.just(new ObjectCreationExpr(null,
    // // StaticJavaParser.parseClassOrInterfaceType(typeName),
    // NodeList.nodeList()));
    // try {
    // return mCore.genObjectCreation(mCtx, type);
    // } catch (ClassNotFoundException e) {

    // e.printStackTrace();
    // }
    // }

    // throw new RuntimeException("Tipo desconhecido ou não suportado: " + type);
    // }

    // private void processMethodCalls(CompilationUnit compilationUnit) {
    // List<MethodDeclaration> allMethods =
    // compilationUnit.findAll(MethodDeclaration.class);

    // for (MethodDeclaration method : allMethods) {
    // method.walk(MethodCallExpr.class, call -> {
    // String methodName = call.getNameAsString();
    // try {
    // ResolvedMethodDeclaration resolvedMethod = call.resolve();

    // // Verificar os argumentos da chamada de método
    // List<Expression> arguments = call.getArguments();
    // for (int i = 0; i < arguments.size(); i++) {
    // if (arguments.get(i).isNameExpr()) {
    // NameExpr argExpr = arguments.get(i).asNameExpr();
    // String argumentName = argExpr.getNameAsString();

    // // Verificar se o argumento está associado a um placeholder
    // for (Map.Entry<String, List<String>> entry :
    // variablePlaceholderMap.entrySet()) {
    // if (entry.getValue().contains(argumentName)) {
    // String nome = entry.getKey();

    // // Obter o parâmetro correto do método chamado
    // if (i < resolvedMethod.getNumberOfParams()) {
    // // Substituir o argumento do método chamado
    // String paramName = resolvedMethod.getParam(i).getName();

    // // Adicionar o parâmetro do método ao mapa de placeholders
    // variablePlaceholderMap.get(nome).add(paramName);

    // // Obter o tipo original do placeholder
    // Type tipo = StaticJavaParser.parseType(nome); // Correção aqui

    // // Substituir o parâmetro do método chamado pelo valor do placeholder
    // replacePlaceholder(argExpr, tipo, nome);
    // }
    // }
    // }
    // }
    // }
    // } catch (Exception e) {
    // System.out.println("Erro ao resolver o método: " + methodName);
    // e.printStackTrace();
    // }
    // });

    // // Substituir parâmetros dentro dos métodos chamados
    // for (Parameter parameter : method.getParameters()) {
    // String key = method.getNameAsString() + "#" +
    // method.getParameters().indexOf(parameter);

    // // Se o parâmetro tem ligação com um placeholder, substitua todas as
    // ocorrências
    // // dele
    // if (methodParameterPlaceholders.containsKey(key)) {
    // method.findAll(NameExpr.class).forEach(nameExpr -> {
    // if (nameExpr.getNameAsString().equals(parameter.getNameAsString())) {
    // nameExpr.replace(methodParameterPlaceholders.get(key));
    // }
    // });
    // }
    // }
    // }
    // }

    // ------------------USO DE REGEX----------------------------

    public CompilationUnit processPlaceholders(String filePath) throws Exception {
        // Ler o conteúdo do arquivo como uma string
        String fileContent = new String(Files.readAllBytes(Paths.get(filePath)));

        // Substituir os placeholders diretamente no conteúdo
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(fileContent);
        StringBuffer processedContent = new StringBuffer();

        while (matcher.find()) {
            String tipo = matcher.group(1);
            String nome = matcher.group(2);

            // Verifica se já existe um valor para o placeholder, caso contrário, gera um
            // novo
            if (!placeholderValues.containsKey(nome)) {
                Type javaType = resolveType(tipo);
                Arbitrary<Expression> newExpression = mCore.genExpression(mCtx, javaType);
                Expression sampledExpression = newExpression.sample();
                placeholderValues.put(nome, sampledExpression);
            }

            // Inserir o valor substituído no conteúdo
            matcher.appendReplacement(processedContent, placeholderValues.get(nome).toString());
        }
        matcher.appendTail(processedContent);

        // Parsear o conteúdo modificado para uma unidade de compilação
        return StaticJavaParser.parse(processedContent.toString());
    }

    private Type resolveType(String tipo) {
        // Resolver o tipo, seja ele primitivo ou uma classe complexa
        try {
            return StaticJavaParser.parseType(tipo);
        } catch (Exception e) {
            throw new RuntimeException("Tipo inválido: " + tipo);
        }
    }

    @Property(tries = 2)
    public boolean testPlaceholderSubstitution() throws Exception {
        CompilationUnit modifiedUnit = processPlaceholders(SKELETON_PATH);

        // Verificar se a substituição foi realizada corretamente
        // assertNotNull(modifiedUnit); // Verifica se a unit foi carregada e modificada

        System.out.println(modifiedUnit); // Imprimir a unidade de compilação modificada
        saveGeneratedCode(modifiedUnit); // Salvar o código gerado em um arquivo
        return true;
    }

    // Salvar o código gerado em um arquivo
    private static final String OUTPUT_DIRECTORY = "TestesGerados";

    public static File saveGeneratedCode(CompilationUnit compilationUnit) throws IOException {
        // Garantir que o diretório de saída existe
        Path outputPath = Paths.get(OUTPUT_DIRECTORY);
        if (!Files.exists(outputPath)) {
            Files.createDirectory(outputPath);
        }

        // Contar o número de arquivos .java já presentes na pasta para gerar um novo
        // nome
        File outputDir = outputPath.toFile();
        int fileCount = (int) Arrays.stream(outputDir.listFiles())
                .filter(file -> file.getName().endsWith(".java"))
                .count();

        // Definir novo nome de classe e nome do arquivo
        String newClassName = "MainClass_" + (fileCount + 1);
        String fileName = newClassName + ".java";

        // Alterar o nome da classe principal no CompilationUnit
        compilationUnit.getTypes()
                .stream()
                .filter(type -> type.isClassOrInterfaceDeclaration())
                .findFirst()
                .ifPresent(type -> type.setName(newClassName));

        // Criar o novo arquivo
        File outputFile = new File(outputDir, fileName);

        // Escrever o conteúdo atualizado no arquivo
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(compilationUnit.toString());
        }

        System.out.println("Código salvo em: " + outputFile.getAbsolutePath());
        return outputFile;
    }

    public static boolean compileWithJavac(File file) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "C:\\Program Files\\Amazon Corretto\\jdk11.0.25_9\\bin\\javac",
                "-classpath", "src/main/java",
                "-d", "TestesGerados", // ⬅️ Diretório de saída para .class
                file.getAbsolutePath()
            );
    
            processBuilder.redirectErrorStream(true); // Redireciona a saída de erro para o stream de saída
            Process process = processBuilder.start();
    
            // Redirecionar a saída do processo para evitar bloqueio
            try (InputStream is = process.getInputStream()) {
                is.transferTo(System.out); // Redireciona para a saída padrão
            }
    
            // Espera que o processo termine com limite de tempo
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
    
            if (!finished) {
                process.destroy();
                System.out.println("Compilação interrompida por timeout.");
                return false;
            }
    
            int exitCode = process.exitValue();
    
            if (exitCode == 0) {
                System.out.println("Compilação com javac bem-sucedida para " + file.getName());
                return true;
            } else {
                System.out.println("Erro na compilação com javac para " + file.getName());
                return false;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    // Executar a classe gerada
    public static boolean runGeneratedClass(String fullyQualifiedClassName) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-cp", "TestesGerados;src/main/java",  // Inclui o caminho adicional para os .class e fontes
                fullyQualifiedClassName  // Ex: br.edu.ifsc.javarg.MainClass_10
            );
    
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
    
            // Redireciona a saída para o console
            try (InputStream is = process.getInputStream()) {
                is.transferTo(System.out);
            }
    
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);
    
            if (!finished) {
                process.destroy();
                System.out.println("Execução interrompida por timeout.");
                return false;
            }
    
            int exitCode = process.exitValue();
    
            if (exitCode == 0) {
                System.out.println("Execução bem-sucedida para: " + fullyQualifiedClassName);
                return true;
            } else {
                System.out.println("Execução falhou com código: " + exitCode);
                return false;
            }
    
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    @Property(tries = 1)
public void TestCodeGenerationBatch() throws Exception {
    int successCount = 0;
    int failureCount = 0;

    for (int i = 1; i <= 10; i++) {
        try {
            System.out.println("\n=== Iteração " + i + " ===");

            // Geração de código
            CompilationUnit generatedCode = processPlaceholders(SKELETON_PATH);
            placeholderValues.clear();

            // Salvamento com nome sequencial
            File generatedFile = saveGeneratedCode(generatedCode); // já salva como MainClass_i.java

            // Compilação
            boolean compiled = compileWithJavac(generatedFile);

            if (!compiled) {
                System.out.println("Falha na compilação do arquivo: " + generatedFile.getName());
                failureCount++;
                continue;
            }

            // Execução
            String className = "br.edu.ifsc.javarg." + generatedFile.getName().replace(".java", "");
            boolean executed = runGeneratedClass(className);

            if (executed) {
                successCount++;
            } else {
                failureCount++;
            }

        } catch (Exception e) {
            System.out.println("Erro inesperado na iteração " + i + ": " + e.getMessage());
            failureCount++;
        }
    }

    System.out.println("\n==== RESULTADO FINAL ====");
    System.out.println("Executados com sucesso: " + successCount);
    System.out.println("Falhas na execução: " + failureCount);
}


    @Property(tries = 1)

    public void TestCodeGenerationPipeline() throws Exception {
        // 1. Processar placeholders e gerar código
        CompilationUnit generatedCode = processPlaceholders(SKELETON_PATH);

        // 2. Salvar o código gerado
        placeholderValues.clear(); // Limpar os valores dos placeholders
        File generatedFile = saveGeneratedCode(generatedCode);

        // 3. Compilar o código gerado
        System.out.println("Compilando o código gerado sem erros...");
        compileWithJavac(generatedFile);

    }

    /*
     *
     * Write AST - Arbitrary Sintax Tree to file
     * using FileWriter output filename is `ast.dot`
     *
     */
    private void dumpAST() throws IOException {
        DotPrinter printer = new DotPrinter(true);

        try (
                FileWriter fileWriter = new FileWriter("ast.dot");
                PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.print(printer.output(mSkeleton));
        }
    }

    private void imprimiDados(CompilationUnit Classe) throws IOException {
        // DotPrinter printer = new DotPrinter(true);

        PrettyPrinter printer = new PrettyPrinter();

        try (
                FileWriter arq = new FileWriter("MainClass.java");
                PrintWriter gravarArq = new PrintWriter(arq)) {
            // gravarArq.print(mSkeleton.toString());
            // gravarArq.print(mSkeleton.toString());
            gravarArq.print(printer.print(Classe));
        }
    }

    public void compila(String arquivo2) throws IOException {
        PrintWriter saida = new PrintWriter(new FileWriter("logCompilacao.txt"));

        int resultadoCompilacao = com.sun.tools.javac.Main.compile(
                new String[] { arquivo2 },
                saida);
    }

    /**
     * ********************************
     * *
     * Tests start here * * ********************************
     */

    /*
     *
     * Generate a random primitive type all available primitive
     * types can be found at JRGBase.java `primitiveTypes()` which
     * then use 'net.jqwik.api.Arbitraries' to fetch all possible types
     *
     */
    // @Example
    boolean checkGenPrimitiveType() {
        Arbitrary<PrimitiveType.Primitive> t = mBase.primitiveTypes();

        Arbitrary<LiteralExpr> e = t.flatMap(
                tp -> mBase.genPrimitiveType(new PrimitiveType(tp)));

        System.out.println(
                "Expressão gerada (tipo primitivo): " + e.sample().toString());

        return true;
    }

    /*
     *
     * Generate a random String literal with min_length = 1
     * and max_length = 5, ranging chars from 'a' to 'z'
     *
     */
    // @Example
    boolean checkGenPrimitiveString() {
        Arbitrary<LiteralExpr> s = mBase.genPrimitiveString();

        System.out.println("Frase gerada: " + s.sample());

        return true;
    }

    /*
     *
     * Generate a random Method from `JRGCore.java` ClassTable
     *
     */
    // @Property(tries = 10)
    // @Example
    boolean checkGenMethodInvokation() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenMethodInvokation" + "::inicio");

        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("br.edu.ifsc.javargexamples.A");

        // Arbitrary<MethodCallExpr> e = mCore.genMethodInvokation(c);

        Arbitrary<MethodCallExpr> e = mCore.genMethodInvokation(
                mCtx,
                ReflectParserTranslator.reflectToParserType("int"));

        if (e != null) {
            System.out.println("Method gerado: " + e.sample().toString());
        } else {
            JRGLog.showMessage(
                    Severity.MSG_ERROR,
                    "Não foi possível gerar " + "criação do método");
        }

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenMethodInvokation" + "::fim");

        return true;
    }

    // @Example
    // @Property(tries = 10)

    boolean checkGenLambdaInvokation() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenLambdaInvokation" + "::inicio");

        ClassOrInterfaceType c = new ClassOrInterfaceType();
        c.setName("java.util.function.DoubleToIntFunction");// interface
        if (mCT.isFunctionalInterface(c.asString())) {
            // System.out.println("antes do genLambdaInvokation");

            Arbitrary<MethodCallExpr> e = mCore.genLambdaInvokation(
                    mCtx,
                    ReflectParserTranslator.reflectToParserType("int") // int????????
            );
            // System.out.println("depois do genLambdaInvokation");

            if (e != null) {
                System.out.println("\n \n \nLAMBDA GERADO:: " + e.sample().toString());
            } else {
                JRGLog.showMessage(
                        Severity.MSG_ERROR,
                        "Não foi possível gerar " + "criação do lambda");
            }
        }
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenLambdaInvokation" + "::fim");

        return true;
    }

    /*
     *
     * Get all superTypes from subsequent class calls
     * from ClassTable given a Class path as a parameter
     *
     * In fact, should be called `checkSuperTypes()`
     *
     */
    // @Example
    boolean checkSubTypes2() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes" + "::inicio");

        List<Class> b = mCT.subTypes2("br.edu.ifsc.javargexamples.B");
        // List<Class> b = mCT.subTypes2("java.util.function.DoubleToIntFunction");
        b.forEach(
                i -> {
                    System.out.println("subTypes: " + i.toString());
                });

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes" + "::final");

        return true;
    }

    /*
     *
     * Picks a random Method from a list of avaiable methods
     * from `JRGCore.java` using the given type "int" as a parameter
     *
     */
    // @Property(tries=10)
    // @Example
    boolean checkGenCandidatesMethods() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesMethods" + "::inicio");

        Arbitrary<Method> b = mCore.genCandidatesMethods("int");

        System.out.println("Candidatos Methods: " + b.sample());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesMethods" + "::fim");

        return true;
    }

    // @Example
    // @Property(tries = 10)
    boolean checkGenConcreteCandidatesMethods() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenConcreteCandidatesMethods" + "::inicio");

        Arbitrary<Method> b = mCore.genConcreteCandidatesMethods("int");

        System.out.println("Concrete Candidates Methods: " + b.sample());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenConcreteCandidatesMethods" + "::fim");

        return true;
    }

    // @Property(tries = 10) //SÓ EXISTE UM MÉTODO LAMBDA DISPONÍVEL NO MOMENTO...
    // pq?
    // @Example
    boolean checkGenLambdaCandidateMethods() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenLambdaCandidateMethods" + "::inicio");

        Arbitrary<Method> b = mCore.genLambdaCandidatesMethods("int");

        System.out.println("Candidatos métodos Lambda: " + b.sample());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenLambdaCandidateMethods:" + ":fim");

        return true;
    }

    /*
     *
     * Generate Lambda expressions from `JRGCore.java`
     *
     */
    // @Example
    // @Property(tries = 10)
    boolean checkGenLambdaExpr() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLambdaExpr::inicio");

        ClassOrInterfaceType fi = new ClassOrInterfaceType();

        fi.setName("java.util.function.DoubleToIntFunction");

        Arbitrary<LambdaExpr> e = mCore.genLambdaExpr(mCtx, fi);

        System.out.println("checkGenLambdaExpr: Expr gerada: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLambdaExpr::fim");

        return true;
    }

    // @Example
    boolean checkIsFunctionalInterface() throws ClassNotFoundException {
        if (mCT.isFunctionalInterface("java.util.function.DoubleToIntFunction")) {
            System.out.println("It is a functional interface");
        } else {
            System.out.println("It is not a functional interface");
        }

        return true;
    }

    /*
     *
     * Generate a new Class or Interface type from `JRGCore.java`
     *
     */
    // @Example
    boolean checkGenObjectCreation() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenObjectCreation" + "::inicio");

        ClassOrInterfaceType c = new ClassOrInterfaceType();

        c.setName("br.edu.ifsc.javargexamples.A");// mudei de B para A!!

        // Arbitrary<ObjectCreationExpr> e = mCore.genObjectCreation(c);
        Arbitrary<Expression> e = mCore.genObjectCreation(mCtx, c);

        if (e != null) {
            System.out.println("ObjectCreation gerado: " + e.sample().toString());
        } else {
            JRGLog.showMessage(
                    Severity.MSG_ERROR,
                    "Não foi possível gerar " + "criação de objeto");
        }

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenObjectCreation::fim");

        return true;
    }

    /*
     *
     * Picks a random Field/Attribute from a list of avaiable fields/attributes
     * from `JRGCore.java` using the given type "int" as a parameter
     *
     */
    // @Example
    // @Property(tries = 10)
    boolean checkGenCandidatesFields() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesFields" + "::inicio");

        Arbitrary<Field> b = mCore.genCandidatesField("int");

        System.out.println("Candidatos Fields: " + b.sample());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesFields:" + ":fim");

        return true;
    }

    /*
     *
     * Picks a random Constructor from a list of avaiable constructors
     * from `JRGCore.java` using the given type class as a parameter
     *
     */
    @Property(tries = 1)
    boolean checkGenCandidatesConstructors() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesConstructors" + "::inicio");

        Arbitrary<Constructor> b = mCore.genCandidatesConstructors(
                "br.edu." + "ifsc.javargexamples.B");

        System.out.println("Candidatos Constructors: " + b.sample());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidatesConstructors" + "::fim");

        return true;
    }

    /*
     *
     * Generate a selection of random expressions using attributes and literal
     * integers
     *
     */
    // @Property(tries = 1)
    boolean checkGenExpression() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::inicio");

        try {
            Arbitrary<Expression> e = mCore.genExpression(
                    mCtx,
                    ReflectParserTranslator.reflectToParserType("int"));
            System.out.println("Expressão gerada: " + e.sample());
        } catch (Exception ex) {
            System.out.println("Erro: " + ex.getMessage());
            return false;
        }

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");

        return true;
    }

    @Property(tries = 20)

    // public void replaceCodigo() {

    // String code = "public class Exemplo {\n" +
    // " int num = ?int?;\n" +
    // " String name = ?String?;\n" +
    // " double value = ?double?;\n" +
    // "}";

    // List<String> types = extractTypes(code);
    // System.out.println("Tipos encontrados:");
    // for (String type : types) {
    // System.out.println(type);
    // }

    // code = replaceTypes(code, types);
    // System.out.println("Codigo com substituicao:");
    // System.out.println(code);
    // }

    // public List<String> extractTypes(String code) { // obtenho os tipos marcados
    // List<String> types = new ArrayList<>();
    // Pattern pattern = Pattern.compile("\\?(.*?)\\?");
    // Matcher matcher = pattern.matcher(code);

    // while (matcher.find()) {
    // String type = matcher.group(1).trim();
    // types.add(type);
    // }

    // return types;
    // }

    // public String replaceTypes(String code, List<String> types) { // passo o
    // codigo e a lista de tipos
    // for (String type : types) {
    // String value = getReplacementValue(type); // chama o metodo que vai
    // substituir com base no tipo
    // code = code.replace("?" + type + "?", value);
    // }
    // return code;
    // }

    // public String getReplacementValue(String type) { // verifica o tipo e
    // retorna, se eu quiser usar os Gens vou ter q
    // // por .toString() a principio
    // switch (type) {
    // case "int":
    // Arbitrary<Expression> e = mCore.genExpression(
    // mCtx,
    // ReflectParserTranslator.reflectToParserType("int"));
    // String printar = e.sample().toString();
    // System.out.println("Expressao gerada: " + printar);
    // return printar.toString();

    // case "String":
    // Arbitrary<LiteralExpr> s = mBase.genPrimitiveString();

    // return s.sample().toString();

    // case "double":
    // Arbitrary<Expression> doubles = mCore.genExpression(
    // mCtx,
    // ReflectParserTranslator.reflectToParserType("double"));
    // String printarDouble = doubles.sample().toString();
    // System.out.println("Expressao gerada double: " + printarDouble);
    // return printarDouble.toString();
    // // Adicionar mais casos para outros tipos, se necessário
    // default:
    // return "null";
    // }

    // }

    /*
     *
     * Generate a statement for accessing an attribute of type 'int'
     * from `JRGCore.java` using 'tname: int' as parameter
     *
     */
    // @Example
    boolean checkGenAttributeAccess() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenAtributteAcess" + "::inicio");

        Arbitrary<FieldAccessExpr> e = mCore.genAttributeAccess(
                mCtx,
                ReflectParserTranslator.reflectToParserType("int"));

        System.out.println("Acesso gerado: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpression::fim");

        return true;
    }

    /*
     *
     * Generate a Cast expression for convertion
     * from `JRGCore.java` using a Class as parameter
     *
     */
    // @Example
    boolean checkGenUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast" + "::inicio");

        Arbitrary<CastExpr> e = mCore.genUpCast(
                mCtx,
                ReflectParserTranslator.reflectToParserType(
                        "br.edu.ifsc." + "javargexamples.Aextend"));

        System.out.println("CheckGenUpCast: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenUpCast" + "::final");

        return true;
    }

    /*
     *
     * !ERROR "Jwqik empty set of values"
     *
     */
    // @Example
    boolean checkGenVar() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar" + "::inicio");

        Arbitrary<NameExpr> e = mCore.genVar(
                mCtx,
                ReflectParserTranslator.reflectToParserType("int"));

        System.out.println("checkGenVar: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVar" + "::final");
        return true;
    }

    /*
     *
     * Get all super() from subsequents class inheritance calls
     * from ClassTable given a Class path as a parameter
     *
     */
    // @Example
    boolean checkSuperTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes" + "::inicio");

        List<Class> b = mCT.superTypes(
                "br.edu.ifsc." + "javargexamples.AextendExtend");

        b.forEach(
                i -> {
                    System.out.println("SuperTypes: " + i);
                });

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSuperTypes" + "::final");

        return true;
    }

    /*
     *
     * Get the subTypes from a given class object
     * from ClassTable given a Class path as a parameter
     *
     */
    // @Example
    // @Property(tries = 5)

    boolean checkSubTypes() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes" + "::inicio");

        List<Class> b = mCT.subTypes("br.edu.ifsc." + "javargexamples.A");

        b.forEach(
                i -> {
                    System.out.println("subTypes: " + i.toString());
                });

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkSubTypes" + "::final");

        return true;
    }

    /*
     *
     * Get the candidates for up-casting
     * from `JRGCore.java` using the class object as a parameter
     *
     */
    // @Example
    boolean checkGenCandidateUpCast() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidateUpCast" + "::inicio");

        Arbitrary<Class> b = mCore.genCandidateUpCast(
                "br.edu.ifsc." + "javargexamples.A");

        System.out.println("Candidatos UpCast: " + b.sample().getName());

        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenCandidateUpCast" + "::final");

        return true;
    }

    /*
     *
     * Generate a BlockStmt containing a random program
     * from `JRGStmt.java` using the imports from `MainClass.java`
     * the code is generated from a list variables up to conditional statements
     *
     * # A further improvement would be to write this BlockStmt to a file instead
     * of writing to console everytime the test is ran
     *
     */
    // @Example
    // @Property(tries=3)
    boolean checkGenBlockStmt() throws ClassNotFoundException, IOException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::inicio");

        Arbitrary<BlockStmt> e = mStmt.genBlockStmt(mCtx);

        System.out.println("BlockStmt: " + e.sample());

        ClassOrInterfaceDeclaration classe = mSkeleton
                .getClassByName("MainClass")
                .get();

        List<MethodDeclaration> ms = classe.getMethods();

        MethodDeclaration m = ms.get(0);

        m.setBody(e.sample());

        imprimiDados(mSkeleton);

        compila("MainClass.java");

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenBlockStmt::fim");

        return true;
    }

    /*
     *
     * Generate a variety of variable declarations and assignments
     * using arbitrary data types and a valid string for the variable
     * label from `JRGStmt.java`
     *
     */
    // @Property(tries = 100)
    boolean checkGenVarDeclAssign() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");

        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarDeclAssign(mCtx);

        System.out.println("checkGengenVarDeclaration: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");

        return true;
    }

    /*
     *
     * Generate a variety of ONLY variable declarations using arbitrary
     * data types and a valid string for the variable label from `JRGStmt.java`
     *
     */
    // @Property(tries = 100)
    boolean checkGenVarDecl() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::inicio");

        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarDecl(mCtx);

        System.out.println("checkGengenVarDeclaration: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclaration::fim");

        return true;
    }

    /*
     *
     * Generate If and Else statements from `JRGStmt.java`
     *
     */
    // @Example
    boolean checkGenIfStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::inicio");

        Arbitrary<IfStmt> e = mStmt.genIfStmt(mCtx);

        System.out.println("checkGenIfStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenIfStmt::fim");

        return true;
    }

    /*
     *
     * !ERROR - It's using a binaryExpr and looping conditional for some reason
     * Idk if it is supposed to be like this
     *
     */
    // @Example
    boolean checkWhileStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::inicio");

        Arbitrary<WhileStmt> e = mStmt.genWhileStmt(mCtx);

        System.out.println("checkWhileStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkWhileStmt::fim");

        return true;
    }

    /*
     *
     * Generate conditional statements and a MainClass as well as functions
     * inside it with statements within itself from `JRGStmt.java`
     *
     */
    // @Example
    boolean checkGenStatement() throws ClassNotFoundException, IOException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatement::inicio");

        Arbitrary<Statement> e = mStmt.genStatement(mCtx);

        System.out.println("checkGenStatement: " + e.sample());

        System.out.println(mSkeleton.getClassByName("MainClass"));

        ClassOrInterfaceDeclaration classe = mSkeleton
                .getClassByName("MainClass")
                .get();

        classe.addMethod(
                "main",
                Modifier.publicModifier().getKeyword(),
                Modifier.Keyword.STATIC);
        // mSkeleton.addInterface(e.sample().toString());

        classe.addInitializer().addAndGetStatement(e.sample());

        // imprimiDados(mSkeleton.addClass(classe.toString()));
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatement::fim");

        return true;
    }

    /*
     *
     * Generate a Logical statement from `JRGStmt.java`
     *
     */
    // @Example
    boolean checkGenExpressionStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpressionStmt::inicio");

        Arbitrary<ExpressionStmt> e = mStmt.genExpressionStmt(mCtx);

        System.out.println("checkGenExpressionStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenExpressionStmt::fim");

        return true;
    }

    /*
     *
     * Generate a Logical Expressions from `JRGOperator.java`
     *
     */
    // @Example
    // @Property(tries = 10)
    boolean checkGenLogiExpression() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLogiExpression::inicio");

        Arbitrary<BinaryExpr> e = mOperator.genLogiExpression(mCtx);

        System.out.println("checkGenLogiExpression: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenLogiExpression::fim");

        return true;
    }

    /*
     *
     * Generate a Relational Expressions from `JRGOperator.java`
     * Using comparision signs as <, ==, >= for example
     *
     */
    // @Example
    // @Property(tries = 10)
    boolean checkGenRelaExpression() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenRelaExpression::inicio");

        Arbitrary<BinaryExpr> e = mOperator.genRelaExpression(mCtx);

        System.out.println("checkGenRelaExpression: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenRelaExpression::fim");

        return true;
    }

    /*
     *
     * Generate a Arithmetic Expressions from `JRGOperator.java`
     * Using %, ==, +, -, * between two or more statements for example
     *
     */
    // @Example
    // @Property(tries = 10)
    boolean checkGenArithExpression() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenArithExpression::inicio");

        Arbitrary<BinaryExpr> e = mOperator.genArithExpression(
                mCtx,
                ReflectParserTranslator.reflectToParserType("int"));

        System.out.println("checkGenArithExpression: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenArithExpression::fim");

        return true;
    }

    /*
     *
     * Generate statements in a array format from `JRGStmt.java`
     *
     */
    // @Example
    boolean checkGenStatementList() {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatementList::inicio");

        Arbitrary<NodeList<Statement>> e = mStmt.genStatementList(mCtx);

        System.out.println("checkGenStatementList: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenStatementList::fim");

        return true;
    }

    /*
     *
     * Generate statements for variable declaration
     * From `JRGStmt.java`
     *
     */
    // @Property(tries = 10)
    boolean checkGenVarDeclarationStmt() throws ClassNotFoundException {
        JRGLog.showMessage(
                Severity.MSG_XDEBUG,
                "checkGenVarDeclarationStmt::inicio");

        Arbitrary<ExpressionStmt> e = mStmt.genVarDeclarationStmt(mCtx);

        System.out.println("checkGenVarDeclarationStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarDeclarationStmt::fim");

        return true;
    }

    /*
     *
     * !ERROR - empty set of values
     *
     */
    // @Example
    boolean checkGenVarAssingStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarAssingStmt::inicio");

        Arbitrary<VariableDeclarationExpr> e = mStmt.genVarAssingStmt(mCtx);

        System.out.println("checkGenVarAssingStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenVarAssingStmt::fim");

        return true;
    }

    /*
     *
     * !ERROR - empty set of values
     *
     */
    // @Example
    boolean checkGenTypeAssingStmt() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::inicio");

        Arbitrary<AssignExpr> e = mStmt.genTypeAssingStmt(mCtx);

        System.out.println("checkGenTypeAssingStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::fim");

        return true;
    }

    /*
     *
     * Generate For Loopings expressions with statements within
     * the loop using `JRGStmt.java`
     *
     */
    // @Example
    // @Property(tries=4)
    boolean checkGenFor() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::inicio");

        Arbitrary<ForStmt> e = mStmt.genForStmt(mCtx);
        // mStmt.genForStmt(mCtx);
        System.out.println("checkGenTypeAssingStmt: " + e.sample());

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::fim");

        return true;
    }

    /*
     *
     * !IDK = Generate a selection of variable declarations and assignments
     *
     */
    // @Example
    boolean checkGenList() throws ClassNotFoundException {
        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::inicio");

        List<Statement> e = mStmt.genList(mCtx);
        // mStmt.genForStmt(mCtx);
        System.out.println("checkGenTypeAssingStmt: " + e.get(0));

        JRGLog.showMessage(Severity.MSG_XDEBUG, "checkGenTypeAssingStmt::fim");

        return true;
    }
}
