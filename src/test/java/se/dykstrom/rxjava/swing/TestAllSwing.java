package se.dykstrom.rxjava.swing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import se.dykstrom.rxjava.swing.common.TestRxSwingUtils;
import se.dykstrom.rxjava.swing.common.operators.TestOnSubscribeFromUrl;
import se.dykstrom.rxjava.swing.search.TestSearchAppController;
import se.dykstrom.rxjava.swing.search.TestSearchAppUtils;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        TestOnSubscribeFromUrl.class,
        TestRxSwingUtils.class,
        TestSearchAppController.class,
        TestSearchAppUtils.class
})
public class TestAllSwing {
    // Empty
}
