/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package info.justaway.display;

import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.display.BitmapDisplayer;

public class FadeInRoundedBitmapDisplayer implements BitmapDisplayer {

    private final int roundPixels;

    public FadeInRoundedBitmapDisplayer(int roundPixels) {
        this.roundPixels = roundPixels;
    }

    @Override
    public Bitmap display(Bitmap bitmap, ImageView imageView, LoadedFrom loadedFrom) {
        Bitmap roundedBitmap;
        try {
            roundedBitmap = transform(bitmap, roundPixels);
        } catch (OutOfMemoryError e) {
            roundedBitmap = bitmap;
        }
        imageView.setImageBitmap(roundedBitmap);
        return roundedBitmap;
    }

    private static Bitmap transform(Bitmap source, int radius) {
        int margin = 0;
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawRoundRect(new RectF(margin, margin, source.getWidth() - margin, source.getHeight() - margin), radius, radius, paint);

        return output;
    }
}