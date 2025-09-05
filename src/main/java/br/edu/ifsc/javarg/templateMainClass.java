// ---------------------Esqueleto 1----------------------------------------------------

// package br.edu.ifsc.javarg;

// import br.edu.ifsc.javargexamples.A;
// import br.edu.ifsc.javargexamples.Aextend;
// import br.edu.ifsc.javargexamples.AextendExtend;
// import br.edu.ifsc.javargexamples.B;
// import br.edu.ifsc.javargexamples.C;
// import br.edu.ifsc.javargexamples.D;

// // import java.lang.String;
// // import java.beans.Expression;
// import java.util.function.DoubleToIntFunction;
// import java.util.function.Function;
// // import br.edu.ifsc.javargexamples.D;


// /**
//  *
//  * @author unknown
//  *
//  */
// @SuppressWarnings("all")
// public class MainClass {

//   public static void main(String[] args) {
//     if (new br.edu.ifsc.javargexamples.A(-145, -753, false).a3) {
//       if (true) {
//         if (true) {
//           float qy;
//         }
//         br.edu.ifsc.javargexamples.B am = new br.edu.ifsc.javargexamples.B(false, false);
//       } else {
//         byte a = 1;
//         short u = -107;
//         br.edu.ifsc.javargexamples.Aextend na = new br.edu.ifsc.javargexamples.Aextend(
//           1428588,
//           2591583
//         );
//         byte yg = -128;
//       }
//     } else {
//       int xn;
//       br.edu.ifsc.javargexamples.B kh = new br.edu.ifsc.javargexamples.B(true, true);
//       double cb = 1.1170927842351615E188;
//       boolean lf;
//       br.edu.ifsc.javargexamples.Aextend yp;
//     }
//   }

// public int testeMetodo(){
//   int assai=1;
//   int essaquetroca= __int__var1 + 1;
//   boolean essaTrocaBool=__boolean__boolVar;;
//   double essaTrocaDouble=__double__doubleVar + 300;

//   br.edu.ifsc.javargexamples.D d =  __br.edu.ifsc.javargexamples.D__DVAR;
//   br.edu.ifsc.javargexamples.D dd =  new D(__br.edu.ifsc.javargexamples.B__BVARNOVO, __br.edu.ifsc.javargexamples.C__CVARNOVO);
  

//   double esseRecebeOqJaExiste = __int__var1 + 2131;
//   boolean esseRecebeOqJaExisteBool = __boolean__boolVar;



//   br.edu.ifsc.javargexamples.B novoB = __br.edu.ifsc.javargexamples.B__BVAR;
//   br.edu.ifsc.javargexamples.B novoBdenovo = __br.edu.ifsc.javargexamples.B__BVAR;
//   br.edu.ifsc.javargexamples.C novoC = __br.edu.ifsc.javargexamples.C__CVAR;
//   br.edu.ifsc.javargexamples.A novoA = __br.edu.ifsc.javargexamples.A__AVAR ;



//   testePassadoParametro( __int__valorParametro,esseRecebeOqJaExisteBool);
//   int betano=20000;
//   br.edu.ifsc.javargexamples.C amazing=new br.edu.ifsc.javargexamples.C();
//   return assai;
// }


// public void testePassadoParametro(int qualquercoisa, boolean BoolParametro ){
  
//   boolean recebeParametro=BoolParametro;

//   int recebeaq=qualquercoisa;
//   System.out.println(recebeParametro);
// }


// }

// //--------------------Esqueleto 2-------------------------------------------------------------------

package br.edu.ifsc.javarg;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;


public class MainClass {

  public static void main(String[] args) {
    int contador = 0;

    for (int i = 0; i < 10; i++) {
      contador += i;
    }

    boolean flag = __boolean__flagInicial;

    if (flag) {
        A objetoA = __br.edu.ifsc.javargexamples.A__objA;
      int resultado = (__int__paramA);
      System.out.println(resultado);
    } else {
        br.edu.ifsc.javargexamples.B objetoB = __br.edu.ifsc.javargexamples.B__objB;
      objetoB.setB(__int__valorSet);
    }

    int finalizado = contador + __int__valorExtra;
    System.out.println("Finalizado com: " + finalizado);
  }
}

// //------------------------Esqueleto 3-------------------------------------------------------------------
package br.edu.ifsc.javarg;

import java.util.ArrayList;


import br.edu.ifsc.javargexamples.C;
import br.edu.ifsc.javargexamples.D;


public class MainClass {

  public static void main(String[] args) {
    ArrayList<Integer> lista = new ArrayList<>();
    lista.add(__int__v1);
    lista.add(__int__v2);
    lista.add(__int__v3);

    

   

    C objC = __br.edu.ifsc.javargexamples.C__objC;
    D objD = new D(__br.edu.ifsc.javargexamples.B__bInstancia, objC);

    if (true) {
      System.out.println("Comparação positiva.");
    } else {
      System.out.println("Comparação negativa.");
    }
  }
}

---// //------------------------Esqueleto 4-------------------------------------------------------------------
package br.edu.ifsc.javarg;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.Aextend;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;
import br.edu.ifsc.javargexamples.D;

public class MainClass {

  public static void main(String[] args) {
    br.edu.ifsc.javargexamples.A aInst = new A(__int__a1, __int__a2, __boolean__a3);
    br.edu.ifsc.javargexamples.B bInst = new B(__boolean__b1, __boolean__b2);

    if (aInst.a3 && __boolean__condicional) {
      br.edu.ifsc.javargexamples.Aextend aExt = new Aextend(__int__val1, __int__val2);
      System.out.println("Entrou no if interno.");
    } else {
      System.out.println("Caiu no else.");
    }

    D d = new D(bInst, new C());
    System.out.println("Instância de D criada.");
  }

  public int metodoCalculo() {
    int x = __int__x;
    int y = __int__y;
    double resultado = x + y + __double__delta;

    br.edu.ifsc.javargexamples.C cInst = __br.edu.ifsc.javargexamples.C__cInstancia;
    br.edu.ifsc.javargexamples.D dInst = new D(__br.edu.ifsc.javargexamples.B__bArg, cInst);

    return (int) resultado;
  }
}
// //------------------------Esqueleto 5-------------------------------------------------------------------
package br.edu.ifsc.javarg;

import br.edu.ifsc.javargexamples.A;
import br.edu.ifsc.javargexamples.B;
import br.edu.ifsc.javargexamples.C;
import br.edu.ifsc.javargexamples.D;

import java.util.ArrayList;

public class MainClass {

  public static void main(String[] args) {
    ArrayList<Integer> lista = new ArrayList<>();
    lista.add(__int__v1);
    lista.add(__int__v2);
    lista.add(__int__v3);

    for (int i = 0; i < lista.size(); i++) {
      System.out.println("Valor da lista: " + lista.get(i));
    }

    B b = new B(__boolean__b1, __boolean__b2);
    C c = __br.edu.ifsc.javargexamples.C__objC;
    D d = new D(b, c);

    System.out.println("Objeto D instanciado.");
  }

  public boolean logicaComposta(int a, int b) {
    int resultado = a * b + __int__multiplicador;
    return resultado > 100;
  }
}
