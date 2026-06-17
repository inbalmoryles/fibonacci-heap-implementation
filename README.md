# Fibonacci Heap

A from-scratch Java implementation of a **Fibonacci heap**, a mergeable priority queue that achieves amortized O(1) for `insert`, `findMin`, and `decreaseKey`, and amortized O(log n) for `deleteMin` — the asymptotically fastest known structure for algorithms like Dijkstra's shortest path and Prim's MST that rely heavily on key-decrease operations.

## Why a Fibonacci Heap

Binary heaps make `decreaseKey` cost O(log n) because fixing the heap property requires bubbling up through the tree. Fibonacci heaps sidestep this with **lazy structural repair**: nodes are cut from their parent and dropped into the root list immediately (O(1)), and the expensive rebalancing (`consolidate`) is deferred until the next `deleteMin`. The amortized cost is paid for using a potential function over the number of trees and marked nodes, which is implemented and exposed directly in this codebase (`potential()`).

## Operations

| Operation | Description | Amortized Cost |
|---|---|---|
| `insert(key, info)` | Insert a new key/value pair, return its `HeapNode` | O(1) |
| `findMin()` | Return the minimum node | O(1) |
| `deleteMin()` | Remove the minimum, consolidate trees of equal rank | O(log n) |
| `decreaseKey(node, diff)` | Decrease a key, cascading-cut up to the root if needed | O(1) |
| `delete(node)` | Remove an arbitrary node (implemented via `decreaseKey` + `deleteMin`) | O(log n) |
| `meld(heap2)` | Merge two heaps by splicing root lists | O(1) |
| `size()` / `numTrees()` | Current node count / root-list (tree) count | O(1) |
| `totalLinks()` / `totalCuts()` | Lifetime count of tree links and node cuts performed | O(1) |

## Implementation Details

- **Root list as a circular doubly linked list** — every tree's root lives in a circular list pointed to by `minNode`, enabling O(1) insertion, melding, and minimum lookup.
- **Lazy consolidation** — `deleteMin` is the only operation that triggers `consolidate()`, which links same-rank trees together (the classic binomial-heap-style merge) until every rank in the root list is unique.
- **Cascading cuts** — `decreaseKey` calls `detachNode`, which recursively cuts a node from its parent and promotes it to the root list whenever the heap property is violated, marking/unmarking nodes per the standard Fibonacci heap cascading-cut rule.
- **Potential function** — `potential()` computes `t + 2m` (trees + 2 × marked nodes), the textbook accounting tool used to prove the amortized bounds above.
- **Rank/degree bookkeeping** — `countersRep()` returns a histogram of tree ranks in the root list, useful for inspecting heap shape after a sequence of operations.
- **Diagnostics built in** — `printHeap()` renders the forest hierarchically for debugging, and `totalLinks()` / `totalCuts()` track operation counts across the heap's lifetime.

## Project Structure

```
FibonacciHeap.java   # Single-file implementation: FibonacciHeap + nested HeapNode class
```

The heap is implemented in package `src` as a self-contained class with a public nested `HeapNode` (exposing `key`, `info`, `rank`, `mark`, and the `child`/`parent`/`next`/`prev` pointers), so it can be dropped into any project and used as a building block for graph algorithms or other priority-queue-driven code.

## Usage

```java
FibonacciHeap heap = new FibonacciHeap();

HeapNode a = heap.insert(10, "task-a");
HeapNode b = heap.insert(5, "task-b");
heap.insert(20, "task-c");

heap.findMin().info;        // "task-b"
heap.decreaseKey(a, 8);     // a.key becomes 2, now the new minimum
heap.deleteMin();           // removes the current minimum, consolidates trees

heap.size();                // remaining node count
heap.numTrees();            // current number of root trees
```

## Background

This project implements the Fibonacci heap data structure as originally described by Fredman and Tarjan (1987), including amortized analysis via the potential method, lazy melding, and cascading cuts — concepts central to advanced data structures coursework and to optimizing classical graph algorithms (Dijkstra, Prim) from O(E log V) to O(E + V log V).
