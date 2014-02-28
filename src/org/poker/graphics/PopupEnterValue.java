package org.poker.graphics;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextBox;

public class PopupEnterValue extends DialogBox {
  public interface ValueEntered {
    void setValue(int value);
  }
  
  final TextBox txtValue;
  Button btnEnter;
  
  public PopupEnterValue(String text, final ValueEntered valueEntered) {
    super(false, true);
    setText(text);
    txtValue = new TextBox();
    btnEnter = new Button("Buy-in");
    btnEnter.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        valueEntered.setValue(Integer.parseInt(txtValue.getText()));
      }
    });
    HorizontalPanel panel = new HorizontalPanel();
    panel.add(txtValue);
    panel.add(btnEnter);
    setWidget(panel);
  }
  
  @Override
  public void center() {
    super.center();
    txtValue.setFocus(true);
    //btnEnter.setFocus(true);
  }

}
