package zh.tools.test.mybatisplus;

import org.springframework.stereotype.Service;
import zh.tools.mybatisplus.CommonService;

@Service
public class UserService extends CommonService<UserMapper, User, Long> {
}
