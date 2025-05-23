package NextFTC.Subsystem;

import com.qualcomm.robotcore.hardware.Servo;
import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.core.command.groups.SequentialGroup;
import com.rowanmcalpin.nextftc.core.command.utility.delays.WaitUntil;
import com.rowanmcalpin.nextftc.core.control.controllers.PIDFController;
import com.rowanmcalpin.nextftc.core.control.controllers.feedforward.StaticFeedforward;
import com.rowanmcalpin.nextftc.ftc.OpModeData;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.HoldPosition;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.SetPower;

public class ExtendoSubsystem extends Subsystem {
    private MotorEx motor;
    public String name = "extendoMotor";
    public static final ExtendoSubsystem INSTANCE = new ExtendoSubsystem();
    public PIDFController controller = new PIDFController(0.01, 0.0, 0.0, new StaticFeedforward(0.1));

    private double velocityThreshold = 1;



    @Override
    public void initialize() {
        motor = new MotorEx(name);

    }



    public Command extendCommand(double power) {
        return new SequentialGroup(
                new SetPower(motor, power,this),
                new WaitUntil(() -> Math.abs(motor.getVelocity()) < velocityThreshold)
        );
    }

    public Command retractCommand(double power) {
        return new SequentialGroup(
                new SetPower(motor, -power,this),
                new WaitUntil(() -> Math.abs(motor.getVelocity()) < velocityThreshold)

        );
    }

    @Override
    public Command getDefaultCommand() {
        return new HoldPosition(motor, controller); // Safety: hold position by default
    }
}
