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