module se.viktor.fancontrollerlinux {
    requires javafx.controls;
    requires javafx.fxml;


    opens se.viktor.fancontrollerlinux to javafx.fxml;
    exports se.viktor.fancontrollerlinux;
}