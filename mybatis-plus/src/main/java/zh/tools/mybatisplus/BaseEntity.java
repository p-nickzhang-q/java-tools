package zh.tools.mybatisplus;

public interface BaseEntity<ID> {
    ID getId();

    void setId(ID id);
}
