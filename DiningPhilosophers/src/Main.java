/**
 * 1) Execute (padrão: 'BUTLER'):
 *    java -cp src Main
 * 2) Execute com estratégia de ordenação:
 *    java -cp src Main ordering
 * 3) Execute com estratégia de trylock:
 *    java -cp src Main trylock
 */
public class Main {
    public enum Strategy { BUTLER, RESOURCE_HIERARCHY, TRY_LOCK_TIMEOUT }

    public static void main(String[] args) throws InterruptedException {
        final int N = 5; // número de filósofos/garfos

        // Seleção de estratégia via argumento
        Strategy strategy = Strategy.BUTLER;
        if (args.length > 0) {
            String s = args[0].toLowerCase();
            if (s.equals("ordering") || s.equals("resource") || s.equals("resource_hierarchy")) {
                strategy = Strategy.RESOURCE_HIERARCHY;
            } else if (s.equals("trylock") || s.equals("try_lock") || s.equals("try-lock") || s.equals("timeout")) {
                strategy = Strategy.TRY_LOCK_TIMEOUT;
            } else if (s.equals("butler")) {
                strategy = Strategy.BUTLER;
            }
        }

        System.out.println("Estratégia escolhida: " + strategy);

        // Criar garfos
        Fork[] forks = new Fork[N];
        for (int i = 0; i < N; i++) forks[i] = new Fork(i);

        // 'Butler' controla acesso para evitar deadlock (permite no máximo N-1 filósofos comendo/pegando garfos)
        // criar o butler apenas se a estratégia selecionada for BUTLER
        Butler butler = null;
        if (strategy == Strategy.BUTLER) {
            butler = new Butler(N - 1);
        }

        // medir tempo de execução
        long start = System.nanoTime();

        // Criar e iniciar filósofos
        Thread[] threads = new Thread[N];
        Philosopher[] philosophers = new Philosopher[N];
        for (int i = 0; i < N; i++) {
            Fork left = forks[i];
            Fork right = forks[(i + 1) % N];
            philosophers[i] = new Philosopher(i, left, right, strategy, butler);
            threads[i] = new Thread(philosophers[i], "Filosofo-" + i);
            threads[i].start();
        }

        // Espera todos terminarem (cada filósofo realiza um número limitado de ciclos)
        for (Thread t : threads) t.join();

        long end = System.nanoTime();
        long elapsedMs = (end - start) / 1_000_000;

        // Estatísticas finais
        System.out.println("\nJantar terminado. Estatísticas:");
        int totalEats = 0;
        for (Philosopher p : philosophers) {
            System.out.printf("Filósofo %d comeu %d vezes\n", p.getId(), p.getEatCount());
            totalEats += p.getEatCount();
        }

        int totalPicks = 0;
        int totalFails = 0;
        for (Fork f : forks) {
            totalPicks += f.getPickCount();
            totalFails += f.getFailCount();
        }

        System.out.println();
        System.out.printf("Tempo total de execução: %d ms\n", elapsedMs);
        System.out.printf("Total de refeições (somatório): %d\n", totalEats);
        System.out.printf("Total de aquisições de garfo: %d\n", totalPicks);
        if (strategy == Strategy.TRY_LOCK_TIMEOUT) System.out.printf("Total de falhas tryLock (timeouts): %d\n", totalFails);
    }
}
