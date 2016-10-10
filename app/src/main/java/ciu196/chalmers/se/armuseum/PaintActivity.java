package ciu196.chalmers.se.armuseum;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class PaintActivity extends AppCompatActivity {

    private DrawingView drawingView;

    private Button drawButton;
    private Button eraseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        drawingView = (DrawingView) findViewById(R.id.drawing);

        drawButton = (Button) findViewById(R.id.button_draw);
        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setErasing(false);
            }
        });

        eraseButton = (Button) findViewById(R.id.button_erase);
        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.setErasing(true);
            }
        });
    }


}
