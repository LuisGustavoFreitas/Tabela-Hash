package com.hashtable;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementação de Tabela Hash com Encabeamento Separado.
 */
public class TabelaHash {

    private final int m; // Tamanho da tabela (número de baldes)
    private final List<Registro>[] tabela;
    private final FuncaoHash funcaoHash;
    private int colisoes;

    /**
     * Enumeração para as funções de hashing suportadas.
     */
    public enum FuncaoHash {
        DIVISAO,
        MULTIPLICACAO,
        DOBRAMENTO
    }

    /**
     * Construtor da Tabela Hash.
     * @param m O tamanho da tabela.
     * @param funcaoHash A função de hashing a ser utilizada.
     */
    @SuppressWarnings("unchecked")
    public TabelaHash(int m, FuncaoHash funcaoHash) {
        this.m = m;
        this.funcaoHash = funcaoHash;
        this.tabela = new LinkedList[m];
        for (int i = 0; i < m; i++) {
            tabela[i] = new LinkedList<>();
        }
        this.colisoes = 0;
    }

    /**
     * Calcula o índice (balde) para uma dada chave usando a função de hashing selecionada.
     * @param k A chave (inteiro de 9 dígitos).
     * @return O índice do balde na tabela.
     */
    public int h(int k) {
        switch (funcaoHash) {
            case DIVISAO: return h_divisao(k);
            case MULTIPLICACAO: return h_multiplicacao(k);
            case DOBRAMENTO: return h_dobramento(k);
            default: throw new IllegalArgumentException("Função de Hash desconhecida: " + funcaoHash);
        }
    }

    // --- Funções de Hashing ---

    /**
     * Função de Hashing por Divisão: h(k) = k mod m.
     * @param k A chave.
     * @return O índice.
     */
    private int h_divisao(int k) {
        return k % m;
    }

    /**
     * Função de Hashing por Multiplicação: h(k) = floor(m * (k * A mod 1)).
     * A = 0.6180339887 (constante irracional).
     * @param k A chave.
     * @return O índice.
     */
    private int h_multiplicacao(int k) {
        final double A = 0.6180339887;
        double val = k * A;
        double frac = val - Math.floor(val); // k * A mod 1
        return (int) Math.floor(m * frac);
    }

    /**
     * Função de Hashing por Dobramento: Divide a chave em blocos de 3 dígitos e soma.
     * @param k A chave (inteiro de 9 dígitos).
     * @return O índice.
     */
    private int h_dobramento(int k) {
        // A chave é de 9 dígitos, dividida em 3 blocos de 3 dígitos.
        int bloco1 = k / 1000000; // 3 dígitos mais significativos
        int bloco2 = (k / 1000) % 1000; // 3 dígitos do meio
        int bloco3 = k % 1000; // 3 dígitos menos significativos

        int soma = bloco1 + bloco2 + bloco3;
        // Aplica a função de divisão (mod m) no resultado da soma para obter o índice.
        return soma % m;
    }

    // --- Operações Obrigatórias ---

    /**
     * Insere um registro na tabela hash.
     * @param registro O registro a ser inserido.
     * @return O número de nós percorridos (sempre 1, pois insere no final da lista).
     */
    public int inserir(Registro registro) {
        int indice = h(registro.getChave());
        List<Registro> lista = tabela[indice];

        // Se a lista não estiver vazia, houve colisão.
        if (!lista.isEmpty()) {
            colisoes++;
        }

        // Inserção no final da lista encadeada correspondente.
        lista.add(registro);
        return 1; // 1 nó percorrido (o nó de inserção)
    }

    /**
     * Busca um registro na tabela hash.
     * @param chave A chave a ser buscada.
     * @return O registro encontrado ou null se ausente.
     */
    public Registro buscar(int chave) {
        int indice = h(chave);
        List<Registro> lista = tabela[indice];
        int nosPercorridos = 0;

        // Percorre sequencialmente a lista encadeada.
        for (Registro registro : lista) {
            nosPercorridos++;
            if (registro.getChave() == chave) {
                // Retorna o registro e o número de nós percorridos.
                // O número de nós percorridos é retornado como um atributo do Registro
                // para fins de métrica, mas o método retorna o Registro.
                // Para o experimento, o método de execução irá capturar essa métrica.
                return registro;
            }
        }
        return null; // Ausente
    }

    /**
     * Conta o número de colisões (baldes com mais de um elemento).
     * @return O número total de colisões (baldes não vazios após o primeiro elemento).
     */
    public int contarColisoes() {
        // A definição de colisão é: "Colisão no balde: ocorre quando, ao inserir k, o compartimento h(k)
        // não está vazio, contabilizando +1 por ocorrência."
        // A variável 'colisoes' já faz essa contagem durante a inserção.
        return colisoes;
    }

    /**
     * Retorna o número de nós percorridos para uma busca.
     * Esta função é auxiliar para a métrica.
     * @param chave A chave a ser buscada.
     * @return O número de nós percorridos.
     */
    public int nosPercorridosBusca(int chave) {
        int indice = h(chave);
        List<Registro> lista = tabela[indice];
        int nosPercorridos = 0;

        for (Registro registro : lista) {
            nosPercorridos++;
            if (registro.getChave() == chave) {
                return nosPercorridos;
            }
        }
        // Se não encontrar, retorna o tamanho da lista (todos os nós percorridos).
        return lista.size();
    }

    /**
     * Retorna o tamanho da tabela (m).
     * @return O tamanho da tabela.
     */
    public int getTamanho() {
        return m;
    }

    /**
     * Retorna a lista encadeada (balde) em um índice específico.
     * @param indice O índice do balde.
     * @return A lista de registros no balde.
     */
    public List<Registro> getLista(int indice) {
        return tabela[indice];
    }

    /**
     * Retorna a função de hashing utilizada.
     * @return A função de hashing.
     */
    public FuncaoHash getFuncaoHash() {
        return funcaoHash;
    }
}
