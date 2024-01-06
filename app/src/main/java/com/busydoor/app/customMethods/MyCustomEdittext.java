package com.busydoor.app.customMethods;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import com.busydoor.app.R;


public class MyCustomEdittext extends AppCompatEditText {

    private String setFontType;
    public MyCustomEdittext(Context context) {
        super(context);
    }

    public MyCustomEdittext(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MyCustomEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context mContext, AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.MyCustomEdittext);

        String setFontType = typedArray.getString(R.styleable.MyCustomEdittext_setFontType);

        if (!TextUtils.isEmpty(setFontType))
            setTypeface(Typeface.createFromAsset(mContext.getAssets(), setFontType));

        typedArray.recycle();
    }
}
