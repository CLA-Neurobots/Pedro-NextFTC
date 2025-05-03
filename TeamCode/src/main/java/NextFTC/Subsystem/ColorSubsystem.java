package NextFTC.Subsystem;


import static com.rowanmcalpin.nextftc.ftc.OpModeData.hardwareMap;

import android.graphics.Color;

import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.rowanmcalpin.nextftc.core.Subsystem;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class ColorSubsystem extends Subsystem {

    public static final ColorSubsystem INSTANCE = new ColorSubsystem();
    private ColorSubsystem() {}

    private RevColorSensorV3 colorSensor;

    private String detectedColor = "None";
    private boolean objectDetected = false;

    @Override
    public void initialize() {
        colorSensor = hardwareMap.get(RevColorSensorV3.class, "colorSensor");
    }

    @Override
    public void periodic() {
        double distance = colorSensor.getDistance(DistanceUnit.MM);
        objectDetected = distance < 20;

        NormalizedRGBA colors = colorSensor.getNormalizedColors();
        float[] hsv = new float[3];

        Color.RGBToHSV(
                (int) (colors.red * 255),
                (int) (colors.green * 255),
                (int) (colors.blue * 255),
                hsv
        );

        detectedColor = determineColor(hsv[0], hsv[1], hsv[2]);
    }

    private String determineColor(float hue, float saturation, float value) {
        if (hue >= 20 && hue < 55) {
            return "Red";
        } else if (hue >= 200 && hue < 300) {
            return "Blue";
        } else if (hue >= 60 && hue < 100) {
            return "Yellow";
        }
        return "None";
    }

    public String getDetectedColor() {
        return detectedColor;
    }

    public boolean isObjectDetected() {
        return objectDetected;
    }

    public boolean isBlue() {
        return "Blue".equals(detectedColor);
    }

    public boolean isYellow() {
        return "Yellow".equals(detectedColor);
    }

    public boolean isRed() {
        return "Red".equals(detectedColor);
    }
}

