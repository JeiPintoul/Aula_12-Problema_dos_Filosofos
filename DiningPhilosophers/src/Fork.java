import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Representa um garfo como um recurso compartilhado.
 */
public class Fork {
    private final ReentrantLock lock = new ReentrantLock();
    private final int id;
    private final AtomicInteger pickCount = new AtomicInteger(0);
    private final AtomicInteger failCount = new AtomicInteger(0);

    public Fork(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void pick() {
        lock.lock();
        System.out.println("Garfo " + id + " pego");
        pickCount.incrementAndGet();
    }

    /**
     * Tenta pegar o garfo dentro do timeout especificado.
     * Retorna true se conseguiu, false se expirou.
     */
    public boolean tryPick(long timeout, TimeUnit unit) throws InterruptedException {
        boolean acquired = lock.tryLock(timeout, unit);
        if (acquired) {
            System.out.println("Garfo " + id + " pego (try)");
            pickCount.incrementAndGet();
        } else {
            System.out.println("Falha ao pegar garfo " + id + " (timeout)");
            failCount.incrementAndGet();
        }
        return acquired;
    }

    public void put() {
        lock.unlock();
        System.out.println("Garfo " + id + " liberado");
    }

    public int getPickCount() {
        return pickCount.get();
    }

    public int getFailCount() {
        return failCount.get();
    }
}
