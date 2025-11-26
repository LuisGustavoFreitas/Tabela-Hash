# Tabela Hash com Encabeamento Separado

**Instituição:** PUCPR  
**Disciplina:** Resolução de Problemas Estruturados em Computação  
**Professor:** Andrey Cabral  
**Alunos:**   Luis Gustavo Freitas Kulzer  

---

##  Descrição do Projeto

Este projeto implementa uma **Tabela Hash com Encabeamento Separado** em Java e realiza uma **Análise Experimental** rigorosa para comparar o desempenho de três funções de *hashing* clássicas: **Divisão**, **Multiplicação** e **Dobramento**.

O foco da análise está em como a **Taxa de Ocupação ($\alpha = n/m$)** afeta as métricas de colisões, o custo de busca (nós percorridos) e o tempo de execução, validando os conceitos teóricos de estruturas de dados.

##  Conceitos e Implementação

### Funções de Hashing

As funções foram implementadas na classe `TabelaHash.java` e testadas em um design fatorial completo.

| Função | Fórmula | Descrição |
| :--- | :--- | :--- |
| **Divisão** | $h(k) = k \mod m$ | Mais simples, mas sensível à escolha de $m$ e ao padrão das chaves. |
| **Multiplicação** | $h(k) = \lfloor m \cdot (k \cdot A \mod 1) \rfloor$ | Utiliza a constante irracional $A = 0.6180339887$ (razão áurea), sendo robusta contra padrões nos dados. |
| **Dobramento** | $h(k) = (\text{bloco}_1 + \text{bloco}_2 + \text{bloco}_3) \mod m$ | Tenta misturar os bits da chave, mas se mostrou menos eficaz na prática. |

### Configurações do Experimento

O experimento cobriu todas as combinações de:

- **Tamanhos de Tabela ($m$):** 1009, 10007, 100003  
- **Tamanhos de Dados ($n$):** 1000, 10000, 100000  
- **Repetições:** 5 repetições independentes por configuração  

---

## Resultados e Análise

### 1. Colisões e Uniformidade

O gráfico abaixo mostra o número médio de colisões em função da taxa de ocupação ($\alpha$).

**Gráfico 1: Colisões Médias por Taxa de Ocupação e Função de Hashing**  
![Gráfico 1](results/graficos/colisoes_vs_alpha.png)

**Conclusão:**  
A função **Multiplicação** e a **Divisão** (com $m$ primo) apresentaram a melhor distribuição de chaves, resultando no menor número de colisões.  
A função **Dobramento** demonstrou ser a menos uniforme.

---

### 2. Custo de Busca (Nós Percorridos)

Esta é a métrica mais importante, pois reflete o custo real da operação de busca.

**Gráfico 2: Nós Percorridos Médios por Busca vs. Taxa de Ocupação**  
![Gráfico 2](results/graficos/nos_percorridos_vs_alpha.png)

**Conclusão:**  
O resultado confirma a teoria: o custo de busca é **linearmente proporcional a $\alpha$**, como esperado para encadeamento separado.  
A função **Dobramento** exige mais nós percorridos para $\alpha > 1$ devido à pior dispersão.

---

### Tabela de Resumo das Métricas

A tabela a seguir resume as métricas médias para todas as configurações testadas.

> *(Insira aqui a tabela gerada automaticamente ou mantenha o link para `tabela_resumo.md`)*

---

## 📂 Estrutura do Projeto

