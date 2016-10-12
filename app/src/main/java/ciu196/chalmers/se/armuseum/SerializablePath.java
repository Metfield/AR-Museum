package ciu196.chalmers.se.armuseum;

import android.graphics.Path;
import android.graphics.Point;
import android.widget.AbsListView;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by johnpetersson on 2016-10-10.\
 *
 */
public class SerializablePath implements Serializable {

    private List<Point> points;
    public List<Point> getPoints() {return points;}

    public SerializablePath() {
        points = new LinkedList<>();
    }

    public void addPoint(Point point) {
        points.add(point);
    }

    public void reset() {
        points.clear();
    }

























//    private String name = "TestName";
//    private List<ChildClass> children;
//
//    public SerializablePath() {
//        children = new LinkedList<>();
//    }
//
//    public String getName() {
//        return name;
//    }
//    public List<ChildClass> getChildren() {return children;}
//
//    private class ChildClass implements Serializable {
//        private String childName = "ChildName";
//
//        public ChildClass() {
//
//        }
//
//        public String getChildName() {
//            return childName;
//        }
//    }

//    private List<Action> actions;
//
//    // Empty Database Constructor
//    public SerializablePath() {
//        actions = new LinkedList();
//    }
//
//    public List<Action> getActions() {
//        return actions;
//    }
//
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
//        in.defaultReadObject();
//
//        for (Action action : actions) {
//            action.perform(this);
//        }
//    }
//
//    @Override
//    public void lineTo(float x, float y) {
//        actions.add(new Line(x, y));
//        super.lineTo(x, y);
//    }
//
//    @Override
//    public void moveTo(float x, float y) {
//        actions.add(new Move(x, y));
//        super.moveTo(x, y);
//    }
//
//    @Override
//    public void reset() {
//        actions.clear();
//        super.reset();
//    }
//
//    private interface Action extends Serializable {
//        void perform(Path path);
//    }
//
//    private static final class Line implements Action {
//
//        private float x, y;
//
//        public Line() {}
//
//        public Line(float x, float y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        @Override
//        public void perform(Path path) {
//            path.lineTo(x, y);
//        }
//
//        public void setX(float x) {
//            this.x = x;
//        }
//
//        public void setY(float y) {
//            this.y = y;
//        }
//
//        public float getX() {
//            return x;
//        }
//
//        public float getY() {
//            return y;
//        }
//    }
//
//    private static final class Move implements Action {
//
//        private float x, y;
//
//        public Move() {
//        }
//
//        public Move(float x, float y) {
//            this.x = x;
//            this.y = y;
//        }
//
//        @Override
//        public void perform(Path path) {
//            path.moveTo(x, y);
//        }
//
//        public void setX(float x) {
//            this.x = x;
//        }
//
//        public void setY(float y) {
//            this.y = y;
//        }
//
//        public float getX() {
//            return x;
//        }
//
//        public float getY() {
//            return y;
//        }
//    }
}
