package cn.jpush.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.yywl.projectT.ProjectMApplication;
import com.yywl.projectT.core.Keys;

@SpringBootTest(classes = ProjectMApplication.class)
@RunWith(SpringRunner.class)
public class MessageTest {
	@Autowired
	Keys keys;

	@Test
	public void sendMessage() {
	}
}
