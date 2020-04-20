package no.uib.inf273.extra

fun <K, V> MutableMap<K, V>.mapValuesInPlace(op: (Map.Entry<K, V>) -> V) {
    for (entry in this.entries) {
        entry.setValue(op(entry))
    }
}
