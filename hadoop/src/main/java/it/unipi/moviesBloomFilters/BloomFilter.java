package it.unipi.moviesBloomFilters;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.BitSet;

import org.apache.hadoop.io.Writable;
import org.apache.hadoop.util.hash.MurmurHash;

public class BloomFilter implements Writable, Serializable {
    private BitSet bits;
    private int m;
    private int k; // k hash function Murmur Hash

    public BloomFilter() {
    }

    /**
     * instantiate a new empty Bloom filter
     * @param m is the vector size
     * @param k is the number of hash functions to exploit for the Bloom filter
     */
    public BloomFilter(int m, int k){
        this.m = m;
        this.k = k;
        this.bits = new BitSet(m);
    }

    public BloomFilter(int m, int k, BitSet bits){
        this.m = m;
        this.k = k;
        this.bits = bits;
    }

    public BloomFilter(BloomFilter bf){
        this.m = bf.getM();
        this.k = bf.getK();
        this.bits = (BitSet) bf.getBits().clone();
    }

    /**
     * adds an item to the Bloom filter
     * @param item
     */
    public void add(String item) {
        // iterates over the given number of hash functions (k), and
        // for each it sets one bit in the Bloom filter bits array
        for (int i = 0; i < this.k; i++) {
            //String id = item.replace("t", "0");
            int digestIndex = (Math.abs(MurmurHash.getInstance().hash(item.getBytes(), i)) % this.m);
            //System.out.println("MovieId: " +  item + " | Hash [" + i + "] to index " +  digestIndex);
            this.bits.set(digestIndex, true);
        }
    }

    /**
     * checks if the given item is supposed to be in set for the Bloom Filter
     * @param item the item to check
     * @return true if the item is supposed to be in set, else false
     */
    public boolean check(String item) {
        for (int i = 0; i < this.k; i++) {
            //String id = item.replace("t", "0");
            int digestIndex = (Math.abs(MurmurHash.getInstance().hash(item.getBytes(), i)) % this.m);
            if (!this.bits.get(digestIndex)) {
                return false;
            }
        }
        return true;
    }

    public void copy(BloomFilter copied){
        this.m = copied.getM();
        this.k = copied.getK();
        this.bits = (BitSet) copied.getBits().clone();
    }

    public void or(BloomFilter b) {
        this.bits.or(b.getBits());
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        long[] longs = this.bits.toLongArray();
        dataOutput.writeInt(m);
        dataOutput.writeInt(k);
        dataOutput.writeInt(longs.length);
        for (int i = 0; i < longs.length; i++) {
            dataOutput.writeLong(longs[i]);
        }

    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        this.m = dataInput.readInt();
        this.k = dataInput.readInt();
        long[] longs = new long[dataInput.readInt()];
        for (int i = 0; i < longs.length; i++) {
            longs[i] = dataInput.readLong();
        }
        this.bits = BitSet.valueOf(longs);
    }

    public BitSet getBits() {
        return bits;
    }

    public void setParameters(int m, int k){
        this.m = m;
        this.k = k;
        this.bits = new BitSet(m);
    }

    public void setBits(BitSet bits) {
        this.bits = bits;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    @Override
    public String toString() {
        return "BloomFilter{" +
                "bits=" + bits +
                ", m=" + m +
                ", k=" + k +
                '}';
    }

    public boolean isInitialized() {
        return this.k > 0 && this.m > 0;
    }

    public void reset(int m, int k) {
        if (this.m == m) {
            this.bits.clear();
        } else {
            this.m = m;
            this.k = k;
            this.bits = new BitSet(m);
        }
    }
}