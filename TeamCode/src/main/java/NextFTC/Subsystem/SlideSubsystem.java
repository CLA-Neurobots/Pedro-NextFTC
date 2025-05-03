package NextFTC.Subsystem;


import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.core.control.controllers.PIDFController;
import com.rowanmcalpin.nextftc.core.control.controllers.feedforward.StaticFeedforward;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.RunToPosition;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.SetPower;

public class SlideSubsystem extends Subsystem {

    public static final SlideSubsystem INSTANCE = new SlideSubsystem();
    private SlideSubsystem() {}

    // Motors
    public MotorEx leftMotor;
    public MotorEx rightMotor;

    // Hardware names
    public String leftMotorName = "slidemotorleft";
    public String rightMotorName = "slidemotorright";

    // PIDF controller (shared)
    public PIDFController controller = new PIDFController(0.01, 0.0, 0.0, new StaticFeedforward(0.1));

    // Conversion: degrees → ticks (537.7 CPR for GoBILDA motor, 1:1 gearing)
    private final double ticksPerDegree = 537.7 / 360.0;

    // Slide targets (in degrees)
    public static final double GROUND = 0;
    public static final double LOW = 140.0;
    public static final double MEDIUM = 190.0;
    public static final double HIGH = 295.0;

    // Motor sync offset (right motor travels less to stay aligned)
    private final double rightMotorOffsetDegrees = 5.0;
//----------------------------------------------------------------------//
    public Command toGround() {
        return runBothMotors(GROUND);
    }

    public Command toLow() {
        return runBothMotors(LOW);
    }

    public Command toMedium() {
        return runBothMotors(MEDIUM);
    }

    public Command toHigh() {
        return runBothMotors(HIGH);
    }

    public Command manualPower(double power) {
        return new SetPower(leftMotor, power).and(new SetPower(rightMotor, power));
    }

    private Command runBothMotors(double targetDegrees) {
        double leftTarget = targetDegrees * ticksPerDegree;
        double rightTarget = (targetDegrees - rightMotorOffsetDegrees) * ticksPerDegree;

        return new RunToPosition(leftMotor, leftTarget, controller, this)
                .and(new RunToPosition(rightMotor, rightTarget, controller, this));
    }

    @Override
    public void initialize() {
        leftMotor = new MotorEx(leftMotorName);
        rightMotor = new MotorEx(rightMotorName);

        leftMotor.reverse();  // Your original code reversed left motor
    }
}
