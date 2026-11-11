import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** APS: comparação de BinaryInsertionSort, BucketSort e HeapSort. */
public class APSOrdenacao {
    private static final int AQUECIMENTOS = 10;
    private static final int REPETICOES = 30;

    @FunctionalInterface
    interface Ordenador { void ordenar(int[] vetor); }

    record Algoritmo(String nome, Ordenador implementacao) {}
    record Resultado(String algoritmo, int quantidade, double mediaMs,
                     double medianaMs, double minimoMs, double maximoMs) {}

    public static void binaryInsertionSort(int[] v) {
        for (int i = 1; i < v.length; i++) {
            int atual = v[i];
            int esquerda = 0, direita = i;
            while (esquerda < direita) {
                int meio = esquerda + (direita - esquerda) / 2;
                if (atual < v[meio]) direita = meio;
                else esquerda = meio + 1;
            }
            for (int j = i; j > esquerda; j--) v[j] = v[j - 1];
            v[esquerda] = atual;
        }
    }

    /** BucketSort para inteiros; usa InsertionSort implementado manualmente dentro de cada balde. */
    public static void bucketSort(int[] v) {
        if (v.length < 2) return;
        int minimo = v[0], maximo = v[0];
        for (int x : v) {
            if (x < minimo) minimo = x;
            if (x > maximo) maximo = x;
        }
        long amplitude = (long) maximo - minimo + 1;
        int quantidadeBaldes = Math.max(1, (int) Math.sqrt(v.length));
        long largura = Math.max(1L, (amplitude + quantidadeBaldes - 1) / quantidadeBaldes);
        List<List<Integer>> baldes = new ArrayList<>(quantidadeBaldes);
        for (int i = 0; i < quantidadeBaldes; i++) baldes.add(new ArrayList<>());
        for (int x : v) {
            int indice = (int) Math.min(quantidadeBaldes - 1, ((long) x - minimo) / largura);
            baldes.get(indice).add(x);
        }
        int posicao = 0;
        for (List<Integer> balde : baldes) {
            // Ordenação por inserção dentro de cada balde (sem Collections.sort).
            for (int i = 1; i < balde.size(); i++) {
                int atual = balde.get(i);
                int j = i - 1;
                while (j >= 0 && balde.get(j) > atual) {
                    balde.set(j + 1, balde.get(j));
                    j--;
                }
                balde.set(j + 1, atual);
            }
            for (int numero : balde) v[posicao++] = numero;
        }
    }

    public static void heapSort(int[] v) {
        int n = v.length;
        for (int i = n / 2 - 1; i >= 0; i--) ajustarHeap(v, n, i);
        for (int fim = n - 1; fim > 0; fim--) {
            int temp = v[0]; v[0] = v[fim]; v[fim] = temp;
            ajustarHeap(v, fim, 0);
        }
    }

    private static void ajustarHeap(int[] v, int tamanho, int raiz) {
        while (true) {
            int maior = raiz;
            int esquerda = 2 * raiz + 1, direita = esquerda + 1;
            if (esquerda < tamanho && v[esquerda] > v[maior]) maior = esquerda;
            if (direita < tamanho && v[direita] > v[maior]) maior = direita;
            if (maior == raiz) return;
            int temp = v[raiz]; v[raiz] = v[maior]; v[maior] = temp;
            raiz = maior;
        }
    }

    private static int[] lerNumeros(Path arquivo) throws IOException {
        List<String> linhas = Files.readAllLines(arquivo);
        return linhas.stream().filter(s -> !s.isBlank())
                .mapToInt(s -> Integer.parseInt(s.trim())).toArray();
    }

    private static Resultado testar(Algoritmo algoritmo, int[] original) {
        int[] esperado = original.clone();
        Arrays.sort(esperado); // SOMENTE validação, nunca usado para ordenar nos testes.
        long[] tempos = new long[REPETICOES];
        for (int rodada = -AQUECIMENTOS; rodada < REPETICOES; rodada++) {
            int[] copia = original.clone(); // Cópia fora do intervalo medido.
            long inicio = System.nanoTime();
            algoritmo.implementacao().ordenar(copia);
            long fim = System.nanoTime();
            if (!Arrays.equals(copia, esperado))
                throw new IllegalStateException("Ordenação incorreta: " + algoritmo.nome());
            if (rodada >= 0) tempos[rodada] = fim - inicio;
        }
        Arrays.sort(tempos); // Ordena somente os TEMPOS para calcular estatísticas.
        double soma = 0;
        for (long tempo : tempos) soma += tempo;
        return new Resultado(algoritmo.nome(), original.length,
                soma / REPETICOES / 1_000_000.0,
                (tempos[REPETICOES / 2 - 1] + tempos[REPETICOES / 2]) / 2_000_000.0,
                tempos[0] / 1_000_000.0, tempos[REPETICOES - 1] / 1_000_000.0);
    }

    public static void main(String[] args) throws IOException {
        Path pasta = args.length > 0 ? Path.of(args[0]) : Path.of("dados");
        List<Algoritmo> algoritmos = List.of(
                new Algoritmo("BinaryInsertionSort", APSOrdenacao::binaryInsertionSort),
                new Algoritmo("BucketSort", APSOrdenacao::bucketSort),
                new Algoritmo("HeapSort", APSOrdenacao::heapSort));
        StringBuilder csv = new StringBuilder("algoritmo;quantidade;media_ms;mediana_ms;minimo_ms;maximo_ms\n");
        System.out.println("Algoritmo             N       Média (ms)  Mediana (ms)  Mín. (ms)  Máx. (ms)");
        for (int n : new int[]{1000, 5000, 10000}) {
            int[] numeros = lerNumeros(pasta.resolve(n + "_numbers.txt"));
            if (numeros.length != n) throw new IOException("Quantidade inesperada no arquivo " + n);
            for (Algoritmo algoritmo : algoritmos) {
                Resultado r = testar(algoritmo, numeros);
                System.out.printf(Locale.US, "%-22s %5d %12.6f %13.6f %10.6f %10.6f%n",
                        r.algoritmo(), r.quantidade(), r.mediaMs(), r.medianaMs(), r.minimoMs(), r.maximoMs());
                csv.append(String.format(Locale.US, "%s;%d;%.6f;%.6f;%.6f;%.6f%n",
                        r.algoritmo(), r.quantidade(), r.mediaMs(), r.medianaMs(), r.minimoMs(), r.maximoMs()));
            }
        }
        Files.writeString(Path.of("resultados.csv"), csv);
        System.out.println("\nTodos os testes passaram. Arquivo gerado: resultados.csv");
    }
}
