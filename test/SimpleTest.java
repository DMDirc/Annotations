import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

@Factory
public class SimpleTest {

    public SimpleTest(String foo, String bar) {}

    public SimpleTest(String foo, String bar, @Unbound String baz) {}

}
