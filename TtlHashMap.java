import static java.util.Collections.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TtlHashMap<K, V> implements Map<K, V> {

 private final HashMap<K, V> store = new HashMap<>();
 private final HashMap<K, Long> timestamps = new HashMap<>();
 private final HashMap<K, CleanerTrhead> ttl = new HashMap<>();

 public void putTtl(K key, TimeUnit ttlUnit, long ttlValue) {
 CleanerTrhead ct = new CleanerTrhead(key,ttlUnit,ttlValue);
 ct.start();
 ttl.put(key, ct);
 }

 @Override
 public V get(Object key) {
 V value = this.store.get(key);
 return value;
 }

 @Override
 public V put(K key, V value) {
 timestamps.put(key, System.nanoTime());
 return store.put(key, value);
 }

 @Override
 public int size() {
 return store.size();
 }

 @Override
 public boolean isEmpty() {
 return store.isEmpty();
 }

 @Override
 public boolean containsKey(Object key) {
 return store.containsKey(key);
 }

 @Override
 public boolean containsValue(Object value) {
 return store.containsValue(value);
 }

 @Override
 public V remove(Object key) {
 timestamps.remove(key);
 ttl.get(key).terminate();
 ttl.remove(key);
 return store.remove(key);
 }

 @Override
 public void putAll(Map<? extends K, ? extends V> m) {
 for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
 this.put(e.getKey(), e.getValue());
 }
 }

 @Override
 public void clear() {
 for(K key : ttl.keySet()){
 ttl.get(key).terminate();
 }
 ttl.clear();
 timestamps.clear();
 store.clear();
 }

 @Override
 public Set<K> keySet() {
 return unmodifiableSet(store.keySet());
 }

 @Override
 public Collection<V> values() {
 return unmodifiableCollection(store.values());
 }

 @Override
 public Set<java.util.Map.Entry<K, V>> entrySet() {
 return unmodifiableSet(store.entrySet());
 }

 class CleanerTrhead extends Thread{

 private boolean exit = false;
 private Object key;
 private TimeUnit ttlUnit;
 private long ttlValue;

 public CleanerTrhead(Object k, TimeUnit unit, long value){
 key=k;
 ttlUnit=unit;
 ttlValue=ttlUnit.toNanos(value);
 }

 @Override
 public void run() {
 while (!exit) {
 Object value = store.get(key);

 if (value != null && expired(key)){
 remove(key);
 exit = true;
 }
 try {
 Thread.sleep(5000);
 } catch (InterruptedException e) {
 e.printStackTrace();
 }
 }
 }

 public boolean expired(Object key) {
 if(ttlValue==0){
 return false;
 }
 return (System.nanoTime() - timestamps.get(key)) > ttlValue;
 }

 public void terminate(){
 exit = true;
 }

 }
}