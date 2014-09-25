/*
 */
package it.cnr.iit.retrail.demo;

import it.cnr.iit.retrail.commons.PepSession;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.slf4j.LoggerFactory;

/**
 * Main View Controller.
 */
public class MainViewController extends AnchorPane implements Initializable {

    @FXML
    BorderPane user1;
    @FXML
    BorderPane user2;
    @FXML
    BorderPane user3;
    @FXML
    Label errorMessage;

    static final org.slf4j.Logger log = LoggerFactory.getLogger(MainViewController.class);

    private void addListeners(final BorderPane userView) throws Exception {
        final String userId = userView.getId();
        final User user = User.getInstance(userId);
        updateUserView(userView);
        userView.getLeft().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                try {
                    if (!user.leave()) {
                        showError("User " + userId + " is not allowed to leave");
                    } else if (user.getStatus() == PepSession.Status.REVOKED) {
                        showMessage("User " + userId + " jas left the room");
                        playSound("/META-INF/gui/exit.wav");
                    } else {
                        showMessage("User " + userId + " has left the entrance door");
                        playSound("/META-INF/gui/walkAway.wav");
                    }
                    updateUserView(userView);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        });
        userView.getRight().setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                try {
                    if (user.getStatus() == PepSession.Status.TRY) {
                        if (!user.enterRoom()) {
                            showError("User " + userId + " is not allowed to enter the room");
                            playSound("/META-INF/gui/denied.wav");
                        } else {
                            showMessage("User " + userId + " entered the room");
                            playSound("/META-INF/gui/entrance.wav");
                        }
                    } else if (!user.goToDoor()) {
                        showError("User " + userId + " is not allowed to stand at the door");
                        playSound("/META-INF/gui/tryFail.wav");
                    } else {
                        showMessage("User " + userId + " standing in front of the door");
                        playSound("/META-INF/gui/tryOk.wav");
                    }
                    updateUserView(userView);
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            showMessage("Welcome!");
            addListeners(user1);
            addListeners(user2);
            addListeners(user3);
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }
    }

    public static void playSound(final String name) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Clip clip = AudioSystem.getClip();
                    AudioInputStream inputStream = AudioSystem.getAudioInputStream(
                            Main.class.getResourceAsStream(name));
                    clip.open(inputStream);
                    clip.start();
                    inputStream.close();
                } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                    log.error(e.getMessage());
                }
            }
        }).start();
    }

    private void updateUserView(BorderPane userView) throws Exception {
        String name;
        int x;
        User user = User.getInstance(userView.getId());
        log.info("updating {}, view {}={}", user, userView.getId(), userView);
        ImageView icon = (ImageView) userView.getCenter();
        ImageView leftArrow = (ImageView) userView.getLeft();
        ImageView rightArrow = (ImageView) userView.getRight();
        Label label = (Label) userView.getBottom();
        label.setTextAlignment(TextAlignment.CENTER);
        label.setAlignment(Pos.CENTER);
        switch (user.getStatus()) {
            default:
                name = "/META-INF/gui/userGray.png";
                x = 0;
                leftArrow.setVisible(false);
                rightArrow.setVisible(true);
                break;
            case TRY:
                name = "/META-INF/gui/userBlue.png";
                x = 250;
                leftArrow.setVisible(true);
                rightArrow.setVisible(true);
                break;
            case ONGOING:
                name = "/META-INF/gui/userGreen.png";
                x = 520;
                leftArrow.setVisible(true);
                rightArrow.setVisible(false);

                break;
            case REVOKED:
                name = "/META-INF/gui/userRed.png";
                x = 450;
                leftArrow.setVisible(true);
                rightArrow.setVisible(false);
                break;
        }
        InputStream is = getClass().getResourceAsStream(name);
        Image image = new Image(is);
        is.close();
        icon.setImage(image);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), userView);
        tt.setToX(x);
        tt.play();
        label.setText(user.getCustomId());
    }

    public void onUserMustLeaveRoom(final String userId) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    showError("User " + userId + " must leave room immediately!");
                    playSound("/META-INF/gui/revoked.wav");
                    if (user1.getId().equals(userId)) {
                        updateUserView(user1);
                    } else if (user2.getId().equals(userId)) {
                        updateUserView(user2);
                    } else {
                        updateUserView(user2);
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
            }
        });
    }

    private void showError(String error) {
        errorMessage.setTextFill(Color.RED);
        errorMessage.setText(error);
    }

    private void showMessage(String message) {
        errorMessage.setTextFill(Color.WHITE);
        errorMessage.setText(message);
    }
}
