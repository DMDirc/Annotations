import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;

import java.util.List;

@Factory
public class SimpleTest {

    public SimpleTest(String foo, String bar, @SuppressWarnings("Foo") List<String> stuffs, @Unbound @Deprecated int meh) {}

    public SimpleTest(String foo, String bar, @Unbound String baz) {}

}
