package NextFTC.Subsystem;


import com.rowanmcalpin.nextftc.core.Subsystem;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.SetPower;


public class IntakeMotorSubsystem extends Subsystem {

    public static final IntakeMotorSubsystem INSTANCE = new IntakeMotorSubsystem();
    private IntakeMotorSubsystem() { }


    public MotorEx motor;
    public String name = "intakeMotor";


    public Command intakeCommand() {
        return new SetPower(motor,-1);
    }

    public Command outtakeCommand() {
        return new SetPower(motor , 0.7);
    }

    public Command slowOuttakeCommand() {
        return new SetPower(motor , 0.3);
    }

    public Command stopCommand() {
        return new SetPower(motor, 0);

    }

    @Override
    public void initialize() {
        motor = new MotorEx(name);
    }
}

