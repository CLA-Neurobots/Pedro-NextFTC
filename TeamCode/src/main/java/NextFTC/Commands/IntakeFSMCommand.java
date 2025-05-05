package NextFTC.Commands;

import com.qualcomm.robotcore.util.ElapsedTime;
import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.ftc.gamepad.GamepadEx;

import NextFTC.Subsystem.*;

public class IntakeFSMCommand extends Command {

    private enum IntakeState {
        INTAKE_START,
        INTAKE_EXTEND,
        INTAKE_WAITFORBLOCK,
        INTAKE_RETRACT,
        OUTAKE_HUMAIN,
        OUTAKE_BLOCK
    }

    private final GamepadEx gamepad;
    private IntakeState state = IntakeState.INTAKE_START;
    private final ElapsedTime timer = new ElapsedTime();

    public IntakeFSMCommand(GamepadEx gamepad) {
        this.gamepad = gamepad;
    }


    public void initialize() {
        state = IntakeState.INTAKE_START;
        timer.reset();
    }


    public void execute() {
        switch (state) {
            case INTAKE_START:
                ExtendoSubsystem.INSTANCE.retractCommand(0.4).invoke();
                IntakeMotorSubsystem.INSTANCE.stopCommand().invoke();
                IntakeServoSubsystem.INSTANCE.close().invoke();
                break;

            case INTAKE_EXTEND:
                ExtendoSubsystem.INSTANCE.extendCommand(0.4).invoke();
                if (timer.seconds() > 1) {
                    IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
                    IntakeServoSubsystem.INSTANCE.open().invoke();
                    timer.reset();
                    state = IntakeState.INTAKE_WAITFORBLOCK;
                }
                break;

            case INTAKE_WAITFORBLOCK:
                ColorSubsystem.INSTANCE.periodic();  // Update color
                if (ColorSubsystem.INSTANCE.isBlue() || ColorSubsystem.INSTANCE.isYellow()) {
                    IntakeMotorSubsystem.INSTANCE.stopCommand().invoke();
                    IntakeServoSubsystem.INSTANCE.close().invoke();
                    timer.reset();
                    state = IntakeState.INTAKE_RETRACT;
                }
                break;

            case INTAKE_RETRACT:
                ExtendoSubsystem.INSTANCE.retractCommand(0.4).invoke();
                if (gamepad.getRightBumper().getState()) {
                    timer.reset();
                    state = IntakeState.OUTAKE_HUMAIN;
                } else if (gamepad.getLeftBumper().getState()) {
                    timer.reset();
                    state = IntakeState.OUTAKE_BLOCK;
                }
                break;

            case OUTAKE_HUMAIN:
                if (timer.seconds() < 0.75) {
                    IntakeMotorSubsystem.INSTANCE.outtakeCommand().invoke();
                } else {
                    state = IntakeState.INTAKE_START;
                }
                break;

            case OUTAKE_BLOCK:
                if (timer.seconds() < 1.5) {
                    IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
                } else {
                    state = IntakeState.INTAKE_START;
                }
                break;
        }

        // Manual override to restart FSM
        if (gamepad.getDpadDown().getState() && state != IntakeState.INTAKE_START) {
            state = IntakeState.INTAKE_START;
        }

        // Optional: allow re-triggering extension
        if (gamepad.getDpadUp().getState()) {
            timer.reset();
            state = IntakeState.INTAKE_EXTEND;
        }
    }

    @Override
    public boolean isDone() {
        return false;
    }
}
