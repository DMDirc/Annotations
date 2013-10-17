import com.dmdirc.util.annotations.factory.Factory;
import com.dmdirc.util.annotations.factory.Unbound;
import java.io.IOException;

import java.util.List;
import javax.lang.model.element.Modifier;

@Factory(inject=true, providers=true, singleton=true, name="Flub", modifiers = {Modifier.ABSTRACT})
public class SimpleTest {

    public SimpleTest(String foo, String bar, @SuppressWarnings("Foo") List<String> stuffs, @Unbound @Deprecated int meh) {}

    public SimpleTest(String foo, String bar, @Unbound String baz) throws IOException {}

}
