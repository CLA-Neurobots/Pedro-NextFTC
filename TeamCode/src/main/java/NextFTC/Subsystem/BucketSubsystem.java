package NextFTC.Subsystem;

import com.qualcomm.robotcore.hardware.Servo;
import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.ftc.OpModeData;
import com.rowanmcalpin.nextftc.ftc.hardware.ServoToPosition;

public class BucketSubsystem extends Subsystem {

    public static final BucketSubsystem INSTANCE = new BucketSubsystem();
    private BucketSubsystem() {}

    public Servo servo;
    public String name = "bucket_servo";

    private final double dumpPos = 0.83;
    private final double transferPos = 0.2;

    public Command dump() {
        return new ServoToPosition(servo, dumpPos, this);
    }

    public Command transfer() {
        return new ServoToPosition(servo, transferPos, this);
    }

    @Override
    public void initialize() {
        servo = OpModeData.INSTANCE.getHardwareMap().get(Servo.class, name);
        servo.scaleRange(0.4, 0.79);
        transfer();  // Default
    }
}
