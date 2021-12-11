package se.dykstrom.rxjava.swing.search;

import javax.swing.AbstractListModel;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import se.dykstrom.rxjava.swing.common.RxSwingUtils;

import static se.dykstrom.rxjava.swing.search.SearchAppUtils.log;

public class SearchAppView extends JFrame {

    private JTextField searchField;
    private JList<String> resultList;
    private JEditorPane documentEditor;

    public SearchAppView() {
        super("RX Search");
        initComponents();
    }

    private void initComponents() {
        searchField = new JTextField(20);
        JLabel searchLabel = new JLabel("Type here to search");
        searchLabel.setDisplayedMnemonic(KeyEvent.VK_T);
        searchLabel.setLabelFor(searchField);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // -------------------------------------------------------------------

        resultList = new JList<>();
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane resultScrollPane = new JScrollPane(resultList);
        JLabel resultLabel = new JLabel("Search results");
        resultLabel.setDisplayedMnemonic(KeyEvent.VK_S);
        resultLabel.setLabelFor(resultList);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(resultScrollPane, BorderLayout.CENTER);

        // -------------------------------------------------------------------

        JLabel documentLabel = new JLabel("Document view");
        documentEditor = new JEditorPane("text/html", null);
        documentEditor.setEditable(false);
        JScrollPane documentScrollPane = new JScrollPane(documentEditor);

        JPanel documentPanel = new JPanel(new BorderLayout());
        documentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        documentPanel.add(documentLabel, BorderLayout.NORTH);
        documentPanel.add(documentScrollPane, BorderLayout.CENTER);

        // -------------------------------------------------------------------

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        westPanel.add(searchPanel, BorderLayout.NORTH);
        westPanel.add(resultPanel, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        centerPanel.add(documentPanel, BorderLayout.CENTER);

        // -------------------------------------------------------------------

        setLayout(new BorderLayout());
        setMinimumSize(new Dimension(800, 700));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        add(westPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);

        pack();
    }

    // -----------------------------------------------------------------------

    /**
     * Sets the document in the document editor to the given text.
     */
    public void setDocument(String text) {
        try {
            documentEditor.setText(text);
            documentEditor.setCaretPosition(0);
        } catch (Exception e) {
            documentEditor.setText("");
            showError("Failed to render page", e);
        }
    }

    /**
     * Sets the list of search results (titles) to the given list.
     */
    public void setResults(List<String> searchResults) {
        List<String> list = new ArrayList<>(searchResults);
        resultList.setModel(new AbstractListModel<>() {
            public int getSize() {
                return list.size();
            }

            public String getElementAt(int i) {
                return list.get(i);
            }
        });
        resultList.ensureIndexIsVisible(0);
    }

    /**
     * Shows an error dialog to the user.
     */
    public void showError(String msg, Throwable throwable) {
        SwingUtilities.invokeLater(() -> {
            log(msg + ": " + throwable.getMessage(), throwable);
            JOptionPane.showMessageDialog(this, msg + ":\n" + throwable.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    /**
     * Returns an observable that emits the text strings that the user types into the search text field.
     * If the user empties the search text field, an empty string is emitted.
     */
    public Observable<String> typedSearchTextObs() {
        return RxSwingUtils.typedTextObs(searchField);
    }

    /**
     * Returns an observable that emits the titles the user selects in the list of search results.
     * If the user deselects a title, an empty string is emitted.
     */
    public Observable<String> selectedTitleObs() {
        return RxSwingUtils.selectedItemObs(resultList);
    }

    /**
     * Returns an Observable that emits the URLs of any links the user selects in the document editor pane.
     */
    public Observable<URL> selectedLinkObs() { return RxSwingUtils.selectedLinkObs(documentEditor); }
}
