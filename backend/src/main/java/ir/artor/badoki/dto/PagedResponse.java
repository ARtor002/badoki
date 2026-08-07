package ir.artor.badoki.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public class PagedResponse<T> {

    private List<T> items;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PagedResponse() {}

    public static <T> PagedResponse<T> of(Page<?> source, List<T> items) {
        PagedResponse<T> r = new PagedResponse<>();
        r.items = items;
        r.total = source.getTotalElements();
        r.page = source.getNumber();
        r.size = source.getSize();
        r.totalPages = source.getTotalPages();
        return r;
    }

    public List<T> getItems() { return items; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalPages() { return totalPages; }
}
