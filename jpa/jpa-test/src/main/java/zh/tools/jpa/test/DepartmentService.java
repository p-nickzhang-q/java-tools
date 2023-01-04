package zh.tools.jpa.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zh.tools.jpa.BaseFilterService;
import zh.tools.jpa.BaseRepositorySupport;

@Service
public class DepartmentService extends BaseFilterService<Department, Long> {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public BaseRepositorySupport<Department, Long> repository() {
        return departmentRepository;
    }
}