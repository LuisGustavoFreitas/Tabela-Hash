package com.hashtable;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import com.hashtable.TabelaHash.FuncaoHash;

/**
 * Classe principal para a execução do trabalho:
 * 1. Geração de conjuntos de chaves (dados).
 * 2. Execução do procedimento experimental com diferentes configurações.
 * 3. Coleta e registro das métricas de desempenho.
 * 4. Geração do arquivo de auditoria (checksum).
 */
public class Main {

    // Constantes do experimento
    private static final int[] TAMANHOS_TABELA = {1000, 10007, 100003};
    private static final int[] TAMANHOS_DADOS = {1000, 10000, 100000};
    private static final FuncaoHash[] FUNCOES_HASH = FuncaoHash.values();
    private static final int REPETICOES = 5;
    private static final int CHAVE_MAX = 999999999; // Chave de 9 dígitos

    // Constantes para o arquivo de resultados
    private static final String ARQUIVO_RESULTADOS = "results/resultados_analise.csv";
    private static final String ARQUIVO_AUDITORIA = "results/auditoria_checksum.txt";

    /**
     * Gera um conjunto de chaves únicas de 9 dígitos.
     * @param n O número de chaves a serem geradas.
     * @param seed A semente para o gerador de números aleatórios.
     * @return Uma lista de chaves inteiras.
     */
    private static List<Integer> gerarChaves(int n, long seed) {
        Random random = new Random(seed);
        Set<Integer> chavesUnicas = new HashSet<>();
        while (chavesUnicas.size() < n) {
            // Gera um inteiro de 9 dígitos (entre 100.000.000 e 999.999.999)
            int chave = random.nextInt(CHAVE_MAX - 100000000 + 1) + 100000000;
            chavesUnicas.add(chave);
        }
        return new ArrayList<>(chavesUnicas);
    }

    /**
     * Executa o experimento para uma configuração específica.
     * @param m Tamanho da tabela.
     * @param n Tamanho do conjunto de dados.
     * @param funcaoHash Função de hashing.
     * @param seed Semente para a geração de dados.
     * @param repeticao Número da repetição.
     * @param writer Objeto para escrita no arquivo CSV.
     * @param auditoriaWriter Objeto para escrita no arquivo de auditoria.
     * @throws IOException Se houver erro de escrita no arquivo.
     */
    private static void executarExperimento(int m, int n, FuncaoHash funcaoHash, long seed, int repeticao, PrintWriter writer, PrintWriter auditoriaWriter) throws IOException {
        // 1. Geração de Dados
        List<Integer> chaves = gerarChaves(n, seed);
        List<Registro> registros = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            // O valor do registro é o índice + 1, para ter um valor associado.
            registros.add(new Registro(chaves.get(i), i + 1));
        }

        // 2. Inicialização da Tabela Hash
        TabelaHash tabela = new TabelaHash(m, funcaoHash);

        // 3. Inserção e Checksum (Auditoria)
        long tempoInsercaoTotal = 0;
        long checksum = 0;
        List<Integer> h_k_primeiros = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            Registro reg = registros.get(i);
            
            // Auditoria: h(k) dos 10 primeiros valores
            if (i < 10) {
                h_k_primeiros.add(tabela.h(reg.getChave()));
            }

            // Inserção
            long inicio = System.nanoTime();
            tabela.inserir(reg);
            long fim = System.nanoTime();
            tempoInsercaoTotal += (fim - inicio);

            // Checksum: soma dos 100000 primeiros valores de h(k)
            if (i < 100000) {
                checksum += tabela.h(reg.getChave());
            }
        }

        // 4. Auditoria: Escrever h(k) dos 10 primeiros e o checksum
        if (repeticao == 1) { // Apenas na primeira repetição para cada configuração
            String h_k_str = h_k_primeiros.stream().map(String::valueOf).collect(Collectors.joining(","));
            auditoriaWriter.printf("FUNCAO=%s, M=%d, N=%d, SEED=%d, H_K_PRIMEIROS=%s, CHECKSUM=%d%n",
                funcaoHash.name(), m, n, seed, h_k_str, checksum);
        }

        // 5. Contagem de Colisões
        int colisoes = tabela.contarColisoes();

        // 6. Busca de Chaves Presentes (50%)
        // Seleciona 50% das chaves presentes
        List<Integer> chavesPresentes = chaves.stream()
                                            .filter(c -> chaves.indexOf(c) % 2 == 0) // 50%
                                            .collect(Collectors.toList());
        
        long tempoBuscaPresenteTotal = 0;
        long nosBuscaPresenteTotal = 0;
        
        for (int chave : chavesPresentes) {
            long inicio = System.nanoTime();
            tabela.buscar(chave);
            long fim = System.nanoTime();
            tempoBuscaPresenteTotal += (fim - inicio);
            nosBuscaPresenteTotal += tabela.nosPercorridosBusca(chave);
        }

        // 7. Busca de Chaves Ausentes (50%)
        // Gera 50% de chaves ausentes (n/2)
        List<Integer> chavesAusentes = gerarChavesAusentes(chaves, n / 2, seed + 1); // Nova seed para ausentes
        
        long tempoBuscaAusenteTotal = 0;
        long nosBuscaAusenteTotal = 0;

        for (int chave : chavesAusentes) {
            long inicio = System.nanoTime();
            tabela.buscar(chave);
            long fim = System.nanoTime();
            tempoBuscaAusenteTotal += (fim - inicio);
            nosBuscaAusenteTotal += tabela.nosPercorridosBusca(chave);
        }

        // 8. Cálculo das Médias
        double tempoInsercaoMedio = (double) tempoInsercaoTotal / n;
        
        double tempoBuscaPresenteMedio = (double) tempoBuscaPresenteTotal / chavesPresentes.size();
        double nosBuscaPresenteMedio = (double) nosBuscaPresenteTotal / chavesPresentes.size();

        double tempoBuscaAusenteMedio = (double) tempoBuscaAusenteTotal / chavesAusentes.size();
        double nosBuscaAusenteMedio = (double) nosBuscaAusenteTotal / chavesAusentes.size();

        // 9. Escrita dos Resultados (CSV)
        // Colunas: FUNCAO, M, N, SEED, REPETICAO, COLISOES, 
        //          T_INS_MED, T_BUS_PRES_MED, NOS_BUS_PRES_MED, T_BUS_AUS_MED, NOS_BUS_AUS_MED, CHECKSUM
        writer.printf("%s,%d,%d,%d,%d,%d,%.6f,%.6f,%.6f,%.6f,%.6f,%d%n",
            funcaoHash.name(), m, n, seed, repeticao, colisoes,
            tempoInsercaoMedio, tempoBuscaPresenteMedio, nosBuscaPresenteMedio,
            tempoBuscaAusenteMedio, nosBuscaAusenteMedio, checksum);
    }

    /**
     * Gera um conjunto de chaves ausentes que não estão na lista de chaves presentes.
     * @param chavesPresentes Lista de chaves já inseridas.
     * @param n O número de chaves ausentes a serem geradas.
     * @param seed A semente para o gerador de números aleatórios.
     * @return Uma lista de chaves ausentes.
     */
    private static List<Integer> gerarChavesAusentes(List<Integer> chavesPresentes, int n, long seed) {
        Random random = new Random(seed);
        Set<Integer> chavesPresentesSet = new HashSet<>(chavesPresentes);
        List<Integer> chavesAusentes = new ArrayList<>();
        
        while (chavesAusentes.size() < n) {
            int chave = random.nextInt(CHAVE_MAX - 100000000 + 1) + 100000000;
            if (!chavesPresentesSet.contains(chave)) {
                chavesAusentes.add(chave);
            }
        }
        return chavesAusentes;
    }

    /**
     * Ponto de entrada principal.
     * @param args Argumentos de linha de comando (não utilizados).
     */
    public static void main(String[] args) {
        // Cabeçalho do CSV
        String cabecalho = "FUNCAO,M,N,SEED,REPETICAO,COLISOES,T_INS_MED,T_BUS_PRES_MED,NOS_BUS_PRES_MED,T_BUS_AUS_MED,NOS_BUS_AUS_MED,CHECKSUM";
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARQUIVO_RESULTADOS));
             PrintWriter auditoriaWriter = new PrintWriter(new FileWriter(ARQUIVO_AUDITORIA))) {
            
            writer.println(cabecalho);
            auditoriaWriter.println("Arquivo de Auditoria e Sentinelas");

            // Semente base para reprodutibilidade
            long seedBase = 167890L; // Semente escolhida aleatoriamente

            // Loop principal do experimento
            for (FuncaoHash funcaoHash : FUNCOES_HASH) {
                for (int m : TAMANHOS_TABELA) {
                    for (int n : TAMANHOS_DADOS) {
                        System.out.printf("Executando: FUNCAO=%s, M=%d, N=%d%n", funcaoHash.name(), m, n);
                        for (int r = 1; r <= REPETICOES; r++) {
                            // A semente muda a cada repetição para garantir independência
                            long seed = seedBase + (long) r * 1000 + (long) m + (long) n;
                            executarExperimento(m, n, funcaoHash, seed, r, writer, auditoriaWriter);
                        }
                    }
                }
            }

            System.out.println("Experimento concluído. Resultados em: " + ARQUIVO_RESULTADOS);
            System.out.println("Auditoria em: " + ARQUIVO_AUDITORIA);

        } catch (IOException e) {
            System.err.println("Erro ao escrever nos arquivos de resultados: " + e.getMessage());
        }
    }
}
