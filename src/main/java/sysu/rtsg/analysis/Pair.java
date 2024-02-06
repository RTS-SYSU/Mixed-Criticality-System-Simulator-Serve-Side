package sysu.rtsg.analysis;

import java.util.Objects;

public class Pair<K, V> {
    private K key;
    private V value;


    public Pair() {
        // 这里你可以初始化 first 和 second，或者保留它们为 null
    }


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Pair{" + "key=" + key + ", value=" + value + '}';
    }

    // 可选：重写 equals 和 hashCode，如果您计划将 Pair 对象用作 HashMap 的键或在集合中使用
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) &&
                Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}

