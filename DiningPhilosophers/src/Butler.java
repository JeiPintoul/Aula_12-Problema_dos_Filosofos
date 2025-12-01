import java.util.concurrent.Semaphore;

/**
 * Encapsula a lógica do "mordomo" (butler) que limita quantos filósofos
 * podem tentar pegar garfos ao mesmo tempo.
 */
public class Butler {
    private final Semaphore sem;

    public Butler(int permits) {
        this.sem = new Semaphore(permits);
    }

    public void acquire() throws InterruptedException {
        sem.acquire();
    }

    public void release() {
        sem.release();
    }
}
