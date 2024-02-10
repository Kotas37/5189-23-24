package org.firstinspires.ftc.teamcode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.firstinspires.ftc.robotcore.internal.camera.calibration.CameraCalibration;
import org.firstinspires.ftc.vision.VisionProcessor;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class CameraPipeline implements VisionProcessor {
    public static double minimumValue = 100;
    public static double maximumValue = 255;
    public static double minimumBlueHue = 100;
    public static double maximumBlueHue = 115;
    public static double minimumRedLowHue = 0;
    public static double maximumRedLowHue = 25;
    public static double minimumRedHighHue = 160;
    public static double maximumRedHighHue = 255;

    Mat mat = new Mat();

    String color;

    String outStr = "right";

    static final Rect MID_RECTANGLE = new Rect(
            new Point(250, 400),
            new Point(360, 330)
    );

    static final Rect RIGHT_RECTANGLE = new Rect(
            new Point(260, 100),
            new Point(370, 10)
    );

    @Override
    public void init(int width, int height, CameraCalibration calibration) {

    }

    @Override
    public Object processFrame(Mat frame, long captureTimeNanos) {
        Imgproc.cvtColor(frame, mat , Imgproc.COLOR_RGB2HSV);

        Scalar minimumBlue = new Scalar(minimumBlueHue, minimumValue, minimumValue);
        Scalar maximumBlue = new Scalar(maximumBlueHue, maximumValue, maximumValue);
        Scalar minimumRedLow = new Scalar(minimumRedLowHue, minimumValue, minimumValue);
        Scalar maximumRedLow = new Scalar(maximumRedLowHue, maximumValue, maximumValue);
        Scalar minimumRedHigh = new Scalar(minimumRedHighHue, minimumValue, minimumValue);
        Scalar maximumRedHigh = new Scalar(maximumRedHighHue, maximumValue, maximumValue);

        if (color == "blue") {
            Core.inRange(mat, minimumBlue, maximumBlue, mat);
        } else {
            Mat mat1 = mat.clone();
            Mat mat2 = mat.clone();
            Core.inRange(mat1, minimumRedLow, maximumRedLow, mat1);
            Core.inRange(mat2, minimumRedHigh, maximumRedHigh, mat2);
            Core.bitwise_or(mat1, mat2, mat);
            mat1.release();
            mat2.release();
        }

        double leftBox = Core.sumElems(mat.submat(MID_RECTANGLE)).val[0];
        double rightBox = Core.sumElems(mat.submat(RIGHT_RECTANGLE)).val[0];

        double averagedMidBox = leftBox / MID_RECTANGLE.area() / 255;
        double averagedRightBox = rightBox / RIGHT_RECTANGLE.area() / 255; //Makes value [0,1]

        if(averagedMidBox > 0.6){
            outStr = "mid";
        }else if(averagedRightBox > 0.6){
            outStr = "right";
        }else{
            outStr = "left";
        }

//        mat.copyTo(frame);
        return null;
    }
    private android.graphics.Rect makeGraphicsRect(Rect rect, float scaleBmpPxToCanvasPx) {
        int left = Math.round(rect.x * scaleBmpPxToCanvasPx);
        int top = Math.round(rect.y * scaleBmpPxToCanvasPx);
        int right = left + Math.round(rect.width * scaleBmpPxToCanvasPx);
        int bottom = top + Math.round(rect.height * scaleBmpPxToCanvasPx);

        return new android.graphics.Rect(left, top, right, bottom);
    }

    @Override
    public void onDrawFrame(Canvas canvas, int onscreenWidth, int onscreenHeight, float scaleBmpPxToCanvasPx, float scaleCanvasDensity, Object userContext) {
        Paint rectPaint = new Paint();
        rectPaint.setColor(Color.RED);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(scaleCanvasDensity * 4);

        canvas.drawRect(makeGraphicsRect(MID_RECTANGLE, scaleBmpPxToCanvasPx), rectPaint);
        canvas.drawRect(makeGraphicsRect(RIGHT_RECTANGLE, scaleBmpPxToCanvasPx), rectPaint);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPropPosition(){
        return outStr;
    }
}
