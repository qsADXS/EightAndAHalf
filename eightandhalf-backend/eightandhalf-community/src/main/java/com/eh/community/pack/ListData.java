package com.eh.community.pack;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ListData<T> {

    private List<T> data;

    private int total;

    public static <E> List<E> SubList(int limit, int offset, List<E> l) {
        if (limit * offset >= l.size()) {
            return null;
        } else if (limit * (offset + 1) >= l.size()) {
            return l.subList(limit * offset, l.size());
        }
        return l.subList(limit * offset, limit * (offset + 1) );
    }

}
