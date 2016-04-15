package se.dykstrom.rxjava.swing.common;

import rx.Observable;
import rx.observables.SwingObservable;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.JTextComponent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.EventObject;

public final class RxSwingUtils {

    private RxSwingUtils() {
        // Hidden
    }

    /**
     * Returns an observable that emits the text strings that the user types into the given text field.
     * If the user empties the text field, an empty string is emitted.
     *
     * @param textField The text field to monitor.
     * @return An observable that emits text strings from the text field.
     */
    public static Observable<String> typedTextObs(JTextField textField) {
        return SwingObservable.fromKeyEvents(textField)
                .filter(event -> event.getID() == KeyEvent.KEY_RELEASED)
                .filter(event -> !event.isActionKey())
                .filter(event -> event.getKeyCode() != KeyEvent.VK_ENTER)
                .map(EventObject::getSource)
                .cast(JTextField.class)
                .map(JTextComponent::getText)
                .map(String::trim);
    }

    /**
     * Returns an observable that emits the string items the user selects in the given list.
     * If the user deselects all items, an empty string is emitted.
     *
     * @param list The list to monitor.
     * @return An observable that emits strings selected in the list.
     */
    public static Observable<String> selectedItemObs(JList<String> list) {
        return SwingObservable.fromListSelectionEvents(list.getSelectionModel())
                .filter(event -> !event.getValueIsAdjusting())
                .map(EventObject::getSource)
                .cast(ListSelectionModel.class)
                .map(ListSelectionModel::getMinSelectionIndex)
                .map(index -> (index != -1) ? list.getModel().getElementAt(index) : "");
    }

    /**
     * Returns an observable that emits the URLs of any links the user selects (activates) in the given editor pane.
     *
     * @param editorPane The editor pane to monitor.
     * @return An observable that emits link URLs selected in the editor pane.
     */
    public static Observable<URL> selectedLinkObs(JEditorPane editorPane) {
        return SwingObservables.fromHyperlinkEvents(editorPane)
                .filter(event -> event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                .map(HyperlinkEvent::getURL);
    }
}
