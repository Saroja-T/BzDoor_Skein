package com.busydoor.app.customMethods;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.busydoor.app.R;


public class MyCustomCheckbox extends AppCompatCheckBox {

    private String setFontType;

    public MyCustomCheckbox(Context context) {
        super(context);
    }

    public MyCustomCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyCustomCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context mContext, AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MyCustomTextView);

        setFontType = typedArray.getString(R.styleable.MyCustomTextView_setTypeFace);
        if (!TextUtils.isEmpty(setFontType))
            setTypeface(Typeface.createFromAsset(mContext.getAssets(), setFontType));
        typedArray.recycle();
    }
}
