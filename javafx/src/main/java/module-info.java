module ir.mahdiparastesh.fortuna {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires kotlin.stdlib;

    opens ir.mahdiparastesh.fortuna to javafx.fxml;
    exports ir.mahdiparastesh.fortuna;
}
