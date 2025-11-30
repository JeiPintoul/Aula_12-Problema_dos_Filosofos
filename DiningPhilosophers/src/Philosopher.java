import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Filósofo representado como uma Thread (Runnable).
 * Cada filósofo alterna entre pensar e comer.
 * Implementa duas estratégias para evitar deadlock:
 *  - BUTLER: usa um Semaphore (N-1) para limitar filósofos concorrentes.
 *  - RESOURCE_HIERARCHY: adota ordenação de recursos (pegar garfo de menor id primeiro).
 */
public class Philosopher implements Runnable {
    private final int id;
    private final Fork left;
    private final Fork right;
    private final Main.Strategy strategy;
    private final Butler butler; // pode ser null se não usado
    private final Random rand = new Random();
    private int eatCount = 0;

    // Número de ciclos comer/pensar para demonstração
    private final int EAT_TIMES = 5;

    public Philosopher(int id, Fork left, Fork right, Main.Strategy strategy, Butler butler) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.strategy = strategy;
        this.butler = butler;
    }

    public int getId() {
        return id;
    }

    public int getEatCount() {
        return eatCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < EAT_TIMES; i++) {
                think();

                switch (strategy) {
                    case BUTLER:
                        doWithButler();
                        break;
                    case RESOURCE_HIERARCHY:
                        doWithOrdering();
                        break;
                    case TRY_LOCK_TIMEOUT:
                        doWithTryLockTimeout();
                        break;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Estratégia do mordomo (butler): controla quantos filósofos podem tentar pegar garfos ao mesmo tempo
    private void doWithButler() throws InterruptedException {
        if (butler == null) {
            // fallback caso mal configurado — comporta-se como semáforo com 1 permit
            left.pick();
            right.pick();
            try {
                eat();
            } finally {
                right.put();
                left.put();
            }
        } else {
            butler.acquire();
            try {
                left.pick();
                right.pick();
                eat();
            } finally {
                right.put();
                left.put();
                butler.release();
            }
        }
    }

    // Estratégia de ordenação (Resource Hierarchy): sempre pegar primeiro o garfo de menor id
    private void doWithOrdering() throws InterruptedException {
        Fork first = (left.getId() < right.getId()) ? left : right;
        Fork second = (first == left) ? right : left;

        first.pick();
        second.pick();
        try {
            eat();
        } finally {
            second.put();
            first.put();
        }
    }

    // Estratégia: tryLock com timeout. Tenta pegar cada garfo com timeout; ao falhar, libera e faz backoff.
    private void doWithTryLockTimeout() throws InterruptedException {
        final long TIMEOUT_MS = 300; // timeout de tentativa por garfo
        final int BACKOFF_MIN = 50;
        final int BACKOFF_MAX = 200;

        while (true) {
            // tenta pegar o garfo esquerdo com timeout
            if (left.tryPick(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                try {
                    // se pegou o esquerdo, tenta pegar o direito
                    if (right.tryPick(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                        try {
                            eat();
                            return; // saiu após comer
                        } finally {
                            right.put();
                        }
                    } else {
                        // não conseguiu o direito no timeout
                    }
                } finally {
                    left.put();
                }
            }

            // backoff aleatório antes de tentar novamente — evita livelock
            Thread.sleep(rand.nextInt(BACKOFF_MAX - BACKOFF_MIN + 1) + BACKOFF_MIN);
        }
    }

    private void think() throws InterruptedException {
        System.out.printf("Filósofo %d pensando...\n", id);
        Thread.sleep(rand.nextInt(400) + 200);
    }

    private void eat() throws InterruptedException {
        System.out.printf("Filósofo %d comendo (vez %d)...\n", id, eatCount + 1);
        eatCount++;
        Thread.sleep(rand.nextInt(400) + 200);
    }
}
