package ir.empofdevs.khosro;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class ModeButtonView extends ConstraintLayout {

    public ModeButtonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_option, this, true);
    }
}
