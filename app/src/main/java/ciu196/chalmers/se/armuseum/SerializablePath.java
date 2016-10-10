package ciu196.chalmers.se.armuseum;

import android.graphics.Path;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnpetersson on 2016-10-10.\
 *
 * Code stolen from http://pastebin.com/CtJ5ibA7
 * and
 * http://stackoverflow.com/questions/4919740/how-to-serialize-an-object-of-android-graphics-path
 */
public class SerializablePath extends Path implements Serializable {

    private List<Action> actions = new LinkedList();

    // Empty Database Constructor
    public SerializablePath() {

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
        in.defaultReadObject();

        for (Action action : actions) {
            action.perform(this);
        }
    }

    @Override
    public void lineTo(float x, float y) {
        actions.add(new Line(x, y));
        super.lineTo(x, y);
    }

    @Override
    public void moveTo(float x, float y) {
        actions.add(new Move(x, y));
        super.moveTo(x, y);
    }

    @Override
    public void reset() {
        actions.clear();
        super.reset();
    }

    private interface Action extends Serializable {
        void perform(Path path);
    }

    private static final class Line implements Action {

        private float x, y;

        public Line() {}

        public Line(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void perform(Path path) {
            path.lineTo(x, y);
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    private static final class Move implements Action {

        private float x, y;

        public Move() {}

        public Move(float x, float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void perform(Path path) {
            path.moveTo(x, y);
        }

        public void setX(float x) {
            this.x = x;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    public List<Action> getActions() {
        return actions;
    }
}
