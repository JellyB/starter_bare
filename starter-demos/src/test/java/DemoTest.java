
import com.huatu.springboot.demo.App;
import com.huatu.springboot.demo.TestBean;
import com.huatu.springboot.demo.UserBean;
import com.huatu.tiku.springboot.users.support.SessionRedisTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hanchao
 * @date 2017/8/22 14:03
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,classes = App.class)
public class DemoTest {
    @Autowired
    private UserBean userBean;

    @Value("${user-sessions.enabled:error}")
    private String name;

    @Autowired
    private TestBean testBean;

    @Autowired
    private SessionRedisTemplate sessionRedisTemplate;

    @Test
    public void test(){
        System.out.println(name);
        System.out.println(userBean);
        System.out.println(testBean);
        sessionRedisTemplate.hset("79f7715a0139433788f78cca440b5980","nick","中文测试");
        System.out.println(sessionRedisTemplate.hget("79f7715a0139433788f78cca440b5980","nick"));
    }
}
