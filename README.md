# Jantar dos Filósofos — Implementação em Java

Este repositório contém uma implementação didática do Problema dos Filósofos (Dining Philosophers) usando Threads em Java.

Objetivo: demonstrar soluções de sincronização que evitem deadlock e possibilitem que todos os filósofos comam.

Arquivos principais:
- `src/Main.java` — programa principal; instancia filósofos e garfos, escolhe estratégia.
- `src/Fork.java` — representa um garfo (recurso) usando `ReentrantLock`.
- `src/Philosopher.java` — classe que implementa o comportamento do filósofo (Runnable).

Estratégias implementadas:
- Butler (Mordomo) — padrão: Semaphore (controle de recursos)
  - Técnica: limitar em N-1 o número de filósofos que podem tentar pegar garfos simultaneamente.
  - Evita deadlock pois não permite que todos os filósofos segurem um garfo e esperem pelo outro.
  - Padrões/Conceitos: Semaphore (controle de acesso), Monitor/Lock para garfos.

 - Resource Hierarchy (Ordenação de Recursos)
  - Técnica: aplicar uma ordem total aos recursos (garfos) e sempre pegá-los na mesma ordem (menor id primeiro).
  - Evita espera circular (circular wait) e, portanto, deadlock.
  - Padrões/Conceitos: Hierarquia de recursos (resource ordering), uso de `ReentrantLock`.

- TryLock com timeout (TRY_LOCK_TIMEOUT)
  - Técnica: usar `tryLock(timeout)` (aqui `ReentrantLock.tryLock(timeout, TimeUnit)`) para tentar adquirir cada garfo. Se a tentativa expirar, o filósofo solta qualquer garfo já adquirido, faz um pequeno backoff aleatório e tenta novamente.
  - Semelhante ao comportamento de `timeout` em requisições HTTP: não esperamos indefinidamente por um recurso.
  - Vantagens: evita deadlock sem precisar de um mordomo central; fácil de relacionar com timeouts em requests/IO; demonstra `tryLock` e backoff.
  - Observação: exigência de tratamento de `InterruptedException`; backoff reduz chance de livelock.

Como compilar e executar (Windows, `cmd.exe`):

1) Abra o prompt na pasta `DiningPhilosophers`.

2) Compile:

```
javac src\\*.java
```

3) Execute (padrão: Butler):

```
java -cp src Main
```

4) Execute usando a estratégia de ordenação (Resource Hierarchy):

```
java -cp src Main ordering
```

5) Execute usando a estratégia tryLock com timeout:

```
java -cp src Main trylock
```