package ut.com.entertainment.bamboo.plugins.buildmanagement.tasks;

import com.entertainment.bamboo.plugins.buildmanagement.tasks.utils.GroovyUtil;
import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dwang on 11/2/16.
 */
public class GroovyUtilTest extends TestCase {
    private Map<String, String> variables = new HashMap<String,String>();

    public void setUp(){
        System.out.println("Set up");
        variables.put("TestVariable", "test");
    }

    public void testEval() {
        boolean testCond=GroovyUtil.eval(variables, "TestVariable=='test'");
        assertTrue(testCond);
    }
}
