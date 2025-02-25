package com.siyecaoi.yy.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.siyecaoi.yy.utils.fresco.IDownloadResult;
import com.siyecaoi.yy.utils.fresco.StreamTool;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.memory.PooledByteBuffer;
import com.facebook.common.memory.PooledByteBufferInputStream;
import com.facebook.common.references.CloseableReference;
import com.facebook.common.util.UriUtil;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.siyecaoi.yy.view.FrameAnimation;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

/**
 * Created by Administrator on 2018/1/23.
 */

public class ImageUtils {

    private static int[] getRes(Context context,int res) {
        TypedArray typedArray = context.getResources().obtainTypedArray(res);
        int len = typedArray.length();
        int[] resId = new int[len];
        for (int i = 0; i < len; i++) {
            resId[i] = typedArray.getResourceId(i, -1);
        }
        typedArray.recycle();
        return resId;
    }

    //加载帧动画
    public static FrameAnimation loadFrameAnimation(ImageView image, int resId,boolean repeat,int frameDuration, FrameAnimation.AnimationListener callback) {
        FrameAnimation animation = new FrameAnimation(image, getRes(image.getContext(), resId), frameDuration, repeat);
        if (callback!=null)
            animation.setAnimationListener(callback);
        return animation;
    }

    //加载本地动图
    public static void loadDrawable(SimpleDraweeView draweeView, int resId) {
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setOldController(draweeView.getController())
                .setAutoPlayAnimations(true)
                .build();
        draweeView.setController(controller);
    }

    //加载本地动图
    public static void loadGif(ImageView draweeView, int resId,Context context) {
        Glide.with(context).load(resId).listener(new RequestListener() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Object resource, Object model, Target target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                if (resource instanceof GifDrawable) {
                    //加载一次
                    ((GifDrawable)resource).setLoopCount(3);
                }
                return false;
            }
        }).into(draweeView);
    }

    //加载本地静态图片
    public static void loadDrawableStatic(SimpleDraweeView draweeView, int resId) {
        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resId))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setOldController(draweeView.getController())
                .build();
        draweeView.setController(controller);
    }

    public static void loadUri(ImageView draweeView, Uri uri) {
        draweeView.setImageURI(uri);
    }

    public static void loadUri(SimpleDraweeView draweeView, String uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .build();
        draweeView.setController(controller);
//        draweeView.setImageURI(uri);
    }

    /**
     * 以高斯模糊显示。
     *
     * @param draweeView View。
     * @param url        url.
     * @param iterations 迭代次数，越大越魔化。
     * @param blurRadius 模糊图半径，必须大于0，越大越模糊。
     */
    public static void showUrlBlur(SimpleDraweeView draweeView, String url, int iterations, int blurRadius) {
        try {
            Uri uri = Uri.parse(url);
            ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setPostprocessor(new IterativeBoxBlurPostProcessor(iterations, blurRadius))
                    .build();
            AbstractDraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();
            draweeView.setController(controller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载图片成bitmap。
     *
     * @param imageUrl 图片地址。
     */
    public static void loadToBitmap(String imageUrl, BaseBitmapDataSubscriber mDataSubscriber, Context context) {
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(imageUrl))
                .setProgressiveRenderingEnabled(true)
                .build();

        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeline.fetchDecodedImage
                (imageRequest, context);
        dataSource.subscribe(mDataSubscriber, CallerThreadExecutor.getInstance());
    }


    /**
     * 转换为bitmap
     *
     * @param activity
     * @param uri
     * @return
     */
    public static Bitmap getBitmapFromUri(Activity activity, Uri uri) {
        try {
            // 读取uri所在的图片
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
            return bitmap;
        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            Log.e("[Android]", "目录为：" + uri);
            e.printStackTrace();
            return null;
        }
    }

    public static Uri getUriforBitmap(Activity activity, Bitmap bitmap) {
        try {
            // 读取uri所在的图片
            Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(activity.getContentResolver(), bitmap, null, null));
            return uri;
        } catch (Exception e) {
            Log.e("[Android]", e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 从网络下载图片
     * 1、根据提供的图片URL，获取图片数据流
     * 2、将得到的数据流写入指定路径的本地文件
     *
     * @param url            URL
     * @param loadFileResult LoadFileResult
     */
    public static void downloadImage(Context context, String url, final IDownloadResult loadFileResult) {
        if (TextUtils.isEmpty(url)) {
            return;
        }

        Uri uri = Uri.parse(url);
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequestBuilder builder = ImageRequestBuilder.newBuilderWithSource(uri);
        ImageRequest imageRequest = builder.build();

        // 获取未解码的图片数据
        DataSource<CloseableReference<PooledByteBuffer>> dataSource = imagePipeline.fetchEncodedImage(imageRequest, context);
        dataSource.subscribe(new BaseDataSubscriber<CloseableReference<PooledByteBuffer>>() {
            @Override
            public void onNewResultImpl(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                if (!dataSource.isFinished() || loadFileResult == null) {
                    return;
                }

                CloseableReference<PooledByteBuffer> imageReference = dataSource.getResult();
                if (imageReference != null) {
                    final CloseableReference<PooledByteBuffer> closeableReference = imageReference.clone();
                    try {
                        PooledByteBuffer pooledByteBuffer = closeableReference.get();
                        InputStream inputStream = new PooledByteBufferInputStream(pooledByteBuffer);
                        String photoPath = loadFileResult.getFilePath();
                        byte[] data = StreamTool.read(inputStream);
                        StreamTool.write(photoPath, data);
                        loadFileResult.onResult(photoPath);
                    } catch (IOException e) {
                        loadFileResult.onResult(null);
                        e.printStackTrace();
                    } finally {
                        imageReference.close();
                        closeableReference.close();
                    }
                }
            }

            @Override
            public void onProgressUpdate(DataSource<CloseableReference<PooledByteBuffer>> dataSource) {
                int progress = (int) (dataSource.getProgress() * 100);
                if (loadFileResult != null) {
                    loadFileResult.onProgress(progress);
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                if (loadFileResult != null) {
                    loadFileResult.onResult(null);
                }

                Throwable throwable = dataSource.getFailureCause();
                if (throwable != null) {
                    Log.e("ImageLoader", "onFailureImpl = " + throwable.toString());
                }
            }
        }, Executors.newSingleThreadExecutor());
    }
}
