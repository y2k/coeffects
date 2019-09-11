package io.y2k.coeffects

import kotlinx.atomicfu.atomic

class Store<S>(initState: S) {

    private val state = atomic(initState)

    fun <T> dispatch(f: (S) -> Pair<S, T>): T {
        while (true) {
            val cur = state.value
            val (upd, r) = f(cur)
            if (state.compareAndSet(cur, upd)) return r
        }
    }
}

fun <S, T> Store<S>.read(f: (S) -> T) = dispatch { it to f(it) }

fun <S> Store<S>.update(f: (S) -> S) = dispatch { f(it) to Unit }
