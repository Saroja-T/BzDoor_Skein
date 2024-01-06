package com.busydoor.app.customMethods;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

import com.busydoor.app.R;


public class MyCustomTextView extends AppCompatTextView {

    public MyCustomTextView(Context context) {
        this(context, null);
    }

    public MyCustomTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init(context, attrs);
    }

    public MyCustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context mContext, AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MyCustomTextView);

        String setFontType = typedArray.getString(R.styleable.MyCustomTextView_setTypeFace);

        if (!TextUtils.isEmpty(setFontType))
            setTypeface(Typeface.createFromAsset(mContext.getAssets(), setFontType));

        typedArray.recycle();

    }
}
