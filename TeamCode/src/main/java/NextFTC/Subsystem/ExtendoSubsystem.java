package NextFTC.Subsystem;


import com.qualcomm.robotcore.hardware.DcMotor;
import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.SetPower;


public class ExtendoSubsystem extends Subsystem {

    public static final ExtendoSubsystem INSTANCE = new ExtendoSubsystem();
    private ExtendoSubsystem() {}

    private MotorEx motor;
    public String name = "extendoMotor";
    private double holdPosition = 0;
    private boolean hasZeroed = false;

    private final double velocityThreshold = 5.0;
    private final double retractPower = -0.3;
    private final double extendPower = 0.5;

    @Override
    public void initialize() {
        motor = new MotorEx(name);
        motor.reverse();
    }

    @Override
    public void periodic() {
        if (hasZeroed) {
            double current = motor.getCurrentPosition();
            if (Math.abs(current - holdPosition) > 10) {
                motor.setPower(0.1 * Math.signum(holdPosition - current));
            } else {
                motor.setPower(0);
            }
        }
    }

    public Command zeroRetractCommand() {
        return run(() -> motor.setPower(-0.3))
                .until(() -> Math.abs(motor.getVelocity()) < 5.0)
                .andThen(runOnce(() -> {
                    motor.setPower(0);
                    motor.resetEncoder();
                    holdPosition = 0;
                    hasZeroed = true;
                }));
    }



    public Command extendCommand() {
        return new Command() {
            @Override
            public void initialize() {
                motor.setPower(extendPower);
            }

            @Override
            public void execute() {
                if (Math.abs(motor.getVelocity()) < velocityThreshold) {
                    motor.setPower(0);
                    holdPosition = motor.getCurrentPosition(); // Set new hold point
                    hasZeroed = true;
                }
            }
        };
    }

    public Command retractCommand() {
        return new Command() {
            @Override
            public void initialize() {
                motor.setPower(retractPower);
            }

            @Override
            public void execute() {
                if (Math.abs(motor.getVelocity()) < velocityThreshold) {
                    motor.setPower(0);
                    holdPosition = motor.getCurrentPosition(); // Should be ≈ 0
                    hasZeroed = true;
                }
            }
        };
    }

    public boolean isAtHoldPosition() {
        return Math.abs(motor.getCurrentPosition() - holdPosition) < 10;
    }

    public boolean isZeroed() {
        return hasZeroed;
    }
}
