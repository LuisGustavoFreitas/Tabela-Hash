package com.hashtable;

/**
 * Representa um registro na tabela hash.
 * A chave é um inteiro de 9 dígitos, e o valor é um inteiro.
 */
public class Registro {
    private final int chave; // Chave de 9 dígitos
    private final int valor; // Valor associado à chave

    /**
     * Construtor para a classe Registro.
     * @param chave A chave do registro (inteiro de 9 dígitos).
     * @param valor O valor do registro.
     */
    public Registro(int chave, int valor) {
        this.chave = chave;
        this.valor = valor;
    }

    /**
     * Retorna a chave do registro.
     * @return A chave.
     */
    public int getChave() {
        return chave;
    }

    /**
     * Retorna o valor do registro.
     * @return O valor.
     */
    public int getValor() {
        return valor;
    }

    @Override
    public String toString() {
        // Formato: [chave:valor]
        return String.format("[%09d:%d]", chave, valor);
    }
}
