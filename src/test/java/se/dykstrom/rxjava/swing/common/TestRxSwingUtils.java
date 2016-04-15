package se.dykstrom.rxjava.swing.common;

import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static se.dykstrom.rxjava.common.utils.Utils.printRun;

public class TestRxSwingUtils {

    @Test
    public void testTypedTextObs() throws Exception {
        printRun("testTypedTextObs", () -> {
            MockedTextField textField = new MockedTextField();
            Observable<String> textObs = RxSwingUtils.typedTextObs(textField);

            CountDownLatch latch = new CountDownLatch(1);
            TestSubscriber<String> subscriber = new TestSubscriber<>();
            textObs.subscribe(subscriber);

            // Generate some key events
            SwingUtilities.invokeLater(() -> {
                textField.setText("a");
                textField.createKeyEvent(KeyEvent.KEY_PRESSED, 'a');
                textField.createKeyEvent(KeyEvent.KEY_RELEASED, 'a');
                textField.setText("ab");
                textField.createKeyEvent(KeyEvent.KEY_PRESSED, 'b');
                textField.createKeyEvent(KeyEvent.KEY_RELEASED, 'b');
                textField.setText("");
                textField.createKeyEvent(KeyEvent.KEY_PRESSED, (char) KeyEvent.VK_BACK_SPACE);
                textField.createKeyEvent(KeyEvent.KEY_RELEASED, (char) KeyEvent.VK_BACK_SPACE);
                latch.countDown();
            });

            await(latch, 1000, TimeUnit.MILLISECONDS);

            Collection<String> expected = Arrays.asList("a", "ab", "");
            Collection<String> actual = subscriber.getOnNextEvents();
            assertEquals(expected, actual);
        });
    }

    @Test
    public void testSelectedItemObs() throws Exception {
        printRun("testSelectedItemObs", () -> {
            JList<String> list = new JList<>(new String[]{"a", "b", "c"});
            Observable<String> itemObs = RxSwingUtils.selectedItemObs(list);

            CountDownLatch latch = new CountDownLatch(1);
            TestSubscriber<String> subscriber = new TestSubscriber<>();
            itemObs.subscribe(subscriber);

            // Generate some list selection events
            SwingUtilities.invokeLater(() -> {
                list.getSelectionModel().setSelectionInterval(0, 0);
                list.getSelectionModel().setSelectionInterval(1, 1);
                list.getSelectionModel().clearSelection();
                list.getSelectionModel().setSelectionInterval(0, 0);
                list.getSelectionModel().setSelectionInterval(2, 2);
                latch.countDown();
            });

            await(latch, 1000, TimeUnit.MILLISECONDS);

            Collection<String> expected = Arrays.asList("a", "b", "", "a", "c");
            Collection<String> actual = subscriber.getOnNextEvents();
            assertEquals(expected, actual);
        });
    }

    @Test
    public void testSelectedLinkObs() throws Exception {
        URL url0 = new URL("http://www.google.com");
        URL url1 = new URL("http://www.dn.se");

        printRun("testSelectedLinkObs", () -> {
            JEditorPane editorPane = new JEditorPane("text/html", "");
            Observable<URL> linkObs = RxSwingUtils.selectedLinkObs(editorPane);

            CountDownLatch latch = new CountDownLatch(1);
            TestSubscriber<URL> subscriber = new TestSubscriber<>();
            linkObs.subscribe(subscriber);

            // Generate some list selection events
            SwingUtilities.invokeLater(() -> {
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.ENTERED, url0));
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.ACTIVATED, url0));
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.EXITED, url0));
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.ENTERED, url1));
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.ACTIVATED, url1));
                editorPane.fireHyperlinkUpdate(new HyperlinkEvent(editorPane, EventType.EXITED, url1));
                latch.countDown();
            });

            await(latch, 1000, TimeUnit.MILLISECONDS);

            Collection<URL> expected = Arrays.asList(url0, url1);
            Collection<URL> actual = subscriber.getOnNextEvents();
            assertEquals(expected, actual);
        });
    }

    private static void await(CountDownLatch latch, int timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

    private static class MockedTextField extends JTextField {

        /**
         * Creates a new key event and processes it using {@link #processKeyEvent(KeyEvent)}.
         */
        public void createKeyEvent(int id, char keyCode) {
            processKeyEvent(new KeyEvent(this, id, System.currentTimeMillis(), 0, keyCode, keyCode));
        }
    }
}
