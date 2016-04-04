package se.dykstrom.rxjava.swing.search;

public class SearchApp {
    public static void main(String[] args) {
        SearchAppView view = new SearchAppView();
        new SearchAppController(view);
        view.setLocationRelativeTo(null);
        view.setVisible(true);
    }
}
