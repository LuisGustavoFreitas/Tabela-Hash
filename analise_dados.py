import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np
import os

# Configuração para garantir que o texto em português seja exibido corretamente
plt.rcParams['font.family'] = 'sans-serif'
plt.rcParams['font.sans-serif'] = ['DejaVu Sans']
plt.rcParams['figure.figsize'] = (12, 6)
sns.set_theme(style="whitegrid")

ARQUIVO_RESULTADOS = "TabelaHash_AnaliseExperimental/results/resultados_analise.csv"
DIRETORIO_GRAFICOS = "TabelaHash_AnaliseExperimental/results/graficos"

# Cria o diretório para os gráficos
os.makedirs(DIRETORIO_GRAFICOS, exist_ok=True)

def carregar_e_preparar_dados():
    """Carrega o CSV, calcula as médias por configuração e a taxa de ocupação."""
    try:
        df = pd.read_csv(ARQUIVO_RESULTADOS)
    except FileNotFoundError:
        print(f"Erro: Arquivo {ARQUIVO_RESULTADOS} não encontrado.")
        return None

    # Renomear colunas para facilitar o uso
    df.columns = ['Funcao', 'M', 'N', 'Seed', 'Repeticao', 'Colisoes', 
                  'T_Ins_Med', 'T_Bus_Pres_Med', 'Nos_Bus_Pres_Med', 
                  'T_Bus_Aus_Med', 'Nos_Bus_Aus_Med', 'Checksum']

    # Calcular a Taxa de Ocupação (alpha = N / M)
    df['Alpha'] = df['N'] / df['M']

    # Calcular a média das métricas por configuração (Funcao, M, N, Alpha)
    df_media = df.groupby(['Funcao', 'M', 'N', 'Alpha']).agg(
        Colisoes_Med=('Colisoes', 'mean'),
        T_Ins_Med=('T_Ins_Med', 'mean'),
        T_Bus_Pres_Med=('T_Bus_Pres_Med', 'mean'),
        Nos_Bus_Pres_Med=('Nos_Bus_Pres_Med', 'mean'),
        T_Bus_Aus_Med=('T_Bus_Aus_Med', 'mean'),
        Nos_Bus_Aus_Med=('Nos_Bus_Aus_Med', 'mean')
    ).reset_index()

    return df_media

def gerar_grafico_colisoes(df):
    """Gera o gráfico de Colisões Médias vs. Taxa de Ocupação."""
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=df, x='Alpha', y='Colisoes_Med', hue='Funcao', marker='o')
    plt.title('Colisões Médias por Taxa de Ocupação e Função de Hashing', fontsize=14)
    plt.xlabel('Taxa de Ocupação (α = N/M)', fontsize=12)
    plt.ylabel('Colisões Médias', fontsize=12)
    plt.legend(title='Função de Hashing')
    plt.tight_layout()
    plt.savefig(os.path.join(DIRETORIO_GRAFICOS, 'colisoes_vs_alpha.png'))
    plt.close()

def gerar_grafico_nos_percorridos(df):
    """Gera o gráfico de Nós Percorridos Médios (Busca Presente e Ausente) vs. Taxa de Ocupação."""
    
    # Preparar dados para plotagem
    df_plot = df.melt(id_vars=['Funcao', 'Alpha'], 
                      value_vars=['Nos_Bus_Pres_Med', 'Nos_Bus_Aus_Med'],
                      var_name='Tipo_Busca', 
                      value_name='Nos_Medios')
    
    df_plot['Tipo_Busca'] = df_plot['Tipo_Busca'].replace({
        'Nos_Bus_Pres_Med': 'Chave Presente',
        'Nos_Bus_Aus_Med': 'Chave Ausente'
    })

    plt.figure(figsize=(12, 7))
    sns.lineplot(data=df_plot, x='Alpha', y='Nos_Medios', hue='Funcao', style='Tipo_Busca', marker='o')
    
    plt.title('Nós Percorridos Médios por Busca vs. Taxa de Ocupação', fontsize=14)
    plt.xlabel('Taxa de Ocupação (α = N/M)', fontsize=12)
    plt.ylabel('Nós Percorridos Médios', fontsize=12)
    plt.legend(title='Legenda', loc='upper left')
    plt.tight_layout()
    plt.savefig(os.path.join(DIRETORIO_GRAFICOS, 'nos_percorridos_vs_alpha.png'))
    plt.close()

def gerar_grafico_tempo_busca(df):
    """Gera o gráfico de Tempo Médio de Busca (Presente e Ausente) vs. Taxa de Ocupação."""
    
    # Preparar dados para plotagem
    df_plot = df.melt(id_vars=['Funcao', 'Alpha'], 
                      value_vars=['T_Bus_Pres_Med', 'T_Bus_Aus_Med'],
                      var_name='Tipo_Busca', 
                      value_name='Tempo_Medio_ns')
    
    df_plot['Tipo_Busca'] = df_plot['Tipo_Busca'].replace({
        'T_Bus_Pres_Med': 'Chave Presente',
        'T_Bus_Aus_Med': 'Chave Ausente'
    })

    plt.figure(figsize=(12, 7))
    sns.lineplot(data=df_plot, x='Alpha', y='Tempo_Medio_ns', hue='Funcao', style='Tipo_Busca', marker='o')
    
    plt.title('Tempo Médio de Busca (ns) vs. Taxa de Ocupação', fontsize=14)
    plt.xlabel('Taxa de Ocupação (α = N/M)', fontsize=12)
    plt.ylabel('Tempo Médio (nanossegundos)', fontsize=12)
    plt.legend(title='Legenda', loc='upper left')
    plt.tight_layout()
    plt.savefig(os.path.join(DIRETORIO_GRAFICOS, 'tempo_busca_vs_alpha.png'))
    plt.close()

def gerar_tabela_resumo(df):
    """Gera uma tabela de resumo com as métricas médias para o relatório."""
    
    # Selecionar e formatar as colunas para a tabela
    df_resumo = df[['Funcao', 'M', 'N', 'Alpha', 'Colisoes_Med', 
                    'Nos_Bus_Pres_Med', 'Nos_Bus_Aus_Med', 
                    'T_Bus_Pres_Med', 'T_Bus_Aus_Med']].copy()
    
    # Arredondar valores para melhor visualização
    df_resumo['Colisoes_Med'] = df_resumo['Colisoes_Med'].round(0).astype(int)
    df_resumo['Nos_Bus_Pres_Med'] = df_resumo['Nos_Bus_Pres_Med'].round(2)
    df_resumo['Nos_Bus_Aus_Med'] = df_resumo['Nos_Bus_Aus_Med'].round(2)
    df_resumo['T_Bus_Pres_Med'] = df_resumo['T_Bus_Pres_Med'].round(2)
    df_resumo['T_Bus_Aus_Med'] = df_resumo['T_Bus_Aus_Med'].round(2)
    df_resumo['Alpha'] = df_resumo['Alpha'].round(2)

    # Renomear colunas para o relatório
    df_resumo.columns = ['Função', 'M', 'N', 'α', 'Colisões Médias', 
                         'Nós Pres. (Méd.)', 'Nós Aus. (Méd.)', 
                         'Tempo Pres. (ns)', 'Tempo Aus. (ns)']
    
    # Salvar a tabela em um arquivo Markdown
    markdown_table = df_resumo.to_markdown(index=False)
    with open("TabelaHash_AnaliseExperimental/results/tabela_resumo.md", "w") as f:
        f.write(markdown_table)
    
    return df_resumo

def main():
    df_media = carregar_e_preparar_dados()
    if df_media is not None:
        print("Gerando gráficos...")
        gerar_grafico_colisoes(df_media)
        gerar_grafico_nos_percorridos(df_media)
        gerar_grafico_tempo_busca(df_media)
        
        print("Gerando tabela de resumo...")
        gerar_tabela_resumo(df_media)
        
        print("Análise de dados concluída. Gráficos e tabela salvos em TabelaHash_AnaliseExperimental/results.")

if __name__ == "__main__":
    main()
