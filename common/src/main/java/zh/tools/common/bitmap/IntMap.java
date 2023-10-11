package zh.tools.common.bitmap;

import cn.hutool.bloomfilter.bitMap.BitMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class IntMap implements BitMap, Serializable {
    private static final long serialVersionUID = 1L;
    protected int[] ints;

    protected final Object t;
    protected final String field;

    public IntMap(Object t, String field) {
        this.t = t;
        this.field = field;
        this.ints = new int[93750000];
    }

    public IntMap(int size, Object t, String field) {
        this.ints = new int[size];
        this.t = t;
        this.field = field;
    }

    public void add(long i) {
        int r = (int) (i / 32L);
        int c = (int) (i & 31L);
        this.ints[r] |= 1 << c;
        setBindInt();
    }

    public boolean contains(long i) {
        int r = (int) (i / 32L);
        int c = (int) (i & 31L);
        return (this.ints[r] >>> c & 1) == 1;
    }

    public void remove(long i) {
        int r = (int) (i / 32L);
        int c = (int) (i & 31L);
        this.ints[r] &= ~(1 << c);
        setBindInt();
    }

    protected abstract void setBindInt();

    public List<Enum<?>> getValues(Enum<?>[] values) {
        return Arrays.stream(values).filter(anEnum -> contains(anEnum.ordinal())).collect(Collectors.toList());
    }
}
