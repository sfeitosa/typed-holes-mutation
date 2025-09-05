package br.edu.ifsc.javargtest;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.Aextend;
import br.edu.ifsc.javargexamples.AextendExtend;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;
import net.jqwik.api.Arbitrary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.StaticJavaParser;


/**
 *
 * @author unknown
 *
 */
@SuppressWarnings("all")
public class CodeReplacer {

    private JRGBase mBase;
  
    private JRGCore mCore;
    private Map<String, String> mCtx;
  
    public CodeReplacer(JRGCore jrgCore, Map<String,String>ctx) {
      mCore =jrgCore;
      mCtx = ctx;
  
      
    }
    
    public void replaceCodigo(String code){

    // String code = "public class Exemplo {\n" +
    // "    int num = ?int?;\n" +
    // "    String name = ?String?;\n" +
    // "    ?double? value = 3.14;\n" +
    // "}";

        List<String> types = extractTypes(code);
        System.out.println("Tipos encontrados:");
            for (String type : types) {
                System.out.println(type);
        }

        code = replaceTypes(code, types);
        System.out.println("Código com substituição:");
        System.out.println(code);
    }
    

public  List<String> extractTypes(String code) {          //obtenho os tipos marcados
    List<String> types = new ArrayList<>();
    Pattern pattern = Pattern.compile("\\?(.*?)\\?");
    Matcher matcher = pattern.matcher(code);

    while (matcher.find()) {
        String type = matcher.group(1).trim();
        types.add(type);
    }

    return types;
}

public  String replaceTypes(String code, List<String> types) { //passo o codigo e a lista de tipos
    for (String type : types) {
        String value = getReplacementValue(type);  //chama o metodo que vai substituir com base no tipo
        code = code.replace("?" + type + "?", value);
    }
    return code;
    }

public  String getReplacementValue(String type) { //verifica o tipo e retorna, se eu quiser usar os Gens vou ter q por .toString() a principio
    switch (type) {
        case "int":
       Arbitrary<Expression> e= mCore.genExpression(mCtx, ReflectParserTranslator.reflectToParserType("int"));
        return e.sample().toString();
        // case INT:    Esse é o genInt mas como funcionaria com arbitraries...?
        //     return Arbitraries
        //       .integers()
        //       .map(i -> new IntegerLiteralExpr(String.valueOf(i)));

        case "String":
        return "Substituicao";
        //return genPrimitiveString()

        case "double":
        return "3.14159";
        // Adicionar mais casos para outros tipos, se necessário
        default:
        return "null";
        }

    }




 

}


