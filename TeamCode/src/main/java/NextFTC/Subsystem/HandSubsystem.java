package NextFTC.Subsystem;

import com.qualcomm.robotcore.hardware.Servo;
import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.ftc.OpModeData;
import com.rowanmcalpin.nextftc.ftc.hardware.ServoToPosition;

public class HandSubsystem extends Subsystem {

    public static final HandSubsystem INSTANCE = new HandSubsystem();
    private HandSubsystem() {}

    public Servo servo;
    public String name = "hand_servo";

    private final double openPos = 0.0;
    private final double closedPos = 1.0;

    public Command open() {
        return new ServoToPosition(servo, openPos, this);
    }

    public Command close() {
        return new ServoToPosition(servo, closedPos, this);
    }

    @Override
    public void initialize() {
        servo = OpModeData.INSTANCE.getHardwareMap().get(Servo.class, name);
        servo.scaleRange(0.4, 0.79);  // Same scaling, adjust if needed
        close();  // Default position
    }
}

