APS - Comparação de algoritmos de ordenação

REQUISITO: Java 21 ou superior.

PASSOS (terminal aberto na pasta APS_Ordenacao):
1) javac APSOrdenacao.java
2) java APSOrdenacao

O programa lê os arquivos na subpasta dados e gera resultados.csv.
Opcional: java APSOrdenacao caminho/para/pasta/com/arquivos

METODOLOGIA:
- 3 algoritmos: BinaryInsertionSort, BucketSort, HeapSort.
- 3 conjuntos de entrada: 1.000, 5.000 e 10.000 inteiros.
- 10 execuções de aquecimento e 30 execuções cronometradas por combinação.
- Cada execução recebe uma cópia idêntica dos dados originais.
- Cronômetro (System.nanoTime) inclui apenas a função de ordenação.
- Leitura dos arquivos, cópia dos vetores e verificação ficam fora da medição.
- São geradas média, mediana, mínimo e máximo, em milissegundos.
- BucketSort distribui valores inteiros em aproximadamente sqrt(n) baldes e
  usa InsertionSort implementado manualmente para ordenar cada balde.
- Arrays.sort é usado exclusivamente para validar os resultados e calcular
  estatísticas dos tempos; não substitui os três algoritmos implementados.

IMPORTANTE: tempos variam conforme computador, JVM e carga do sistema.
Para o relatório, execute novamente no computador do grupo e registre
processador, RAM, sistema operacional, versão do Java e condições dos testes.
