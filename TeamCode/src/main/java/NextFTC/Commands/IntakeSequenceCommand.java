package NextFTC.Commands;

import com.rowanmcalpin.nextftc.core.command.Command;
import com.rowanmcalpin.nextftc.core.command.groups.ParallelGroup;
import com.rowanmcalpin.nextftc.core.command.groups.SequentialGroup;
import com.rowanmcalpin.nextftc.core.command.utility.conditionals.PassiveConditionalCommand;
import com.rowanmcalpin.nextftc.core.command.utility.delays.Delay;
import com.rowanmcalpin.nextftc.core.command.utility.delays.WaitUntil;
import com.rowanmcalpin.nextftc.core.units.TimeSpan;
import com.rowanmcalpin.nextftc.ftc.gamepad.GamepadEx;

import NextFTC.Subsystem.ColorSubsystem;
import NextFTC.Subsystem.ExtendoSubsystem;
import NextFTC.Subsystem.IntakeMotorSubsystem;
import NextFTC.Subsystem.IntakeServoSubsystem;

public class IntakeSequenceCommand extends Command {

    private final GamepadEx gamepad;

    public IntakeSequenceCommand(GamepadEx gamepad) {
        this.gamepad = gamepad;
    }


    public void initialize() {
        new SequentialGroup(
                // Step 1: Reset
                ExtendoSubsystem.INSTANCE.retractCommand(0.4),
                IntakeMotorSubsystem.INSTANCE.stopCommand(),
                IntakeServoSubsystem.INSTANCE.close(),

                // Step 2: Wait for Dpad Up
                new WaitUntil(() -> gamepad.getDpadUp().getState()),

                // Step 3: Extend and wait
                ExtendoSubsystem.INSTANCE.extendCommand(0.4),
                new Delay(TimeSpan.fromSec(1)),

                // Step 4: Start intake + open
                new ParallelGroup(
                        IntakeMotorSubsystem.INSTANCE.intakeCommand(),
                        IntakeServoSubsystem.INSTANCE.open()
                ),

                // Step 5: Wait for color detection
                new WaitUntil(() -> {
                    ColorSubsystem.INSTANCE.periodic();  // Must manually call this
                    return ColorSubsystem.INSTANCE.isBlue() || ColorSubsystem.INSTANCE.isYellow();
                }),

                // Step 6: Stop intake, close servo, retract
                new ParallelGroup(
                        IntakeMotorSubsystem.INSTANCE.stopCommand(),
                        IntakeServoSubsystem.INSTANCE.close()
                ),
                ExtendoSubsystem.INSTANCE.retractCommand(0.4),

                // Step 7: Wait for outtake trigger
                new WaitUntil(() ->
                        gamepad.getRightBumper().getState() || gamepad.getLeftBumper().getState()
                ),

                // Step 8: Conditional outtake
                new PassiveConditionalCommand(
                        () -> gamepad.getRightBumper().getState(),
                        () -> new SequentialGroup(
                                IntakeMotorSubsystem.INSTANCE.outtakeCommand(),
                                new Delay(TimeSpan.fromMs(750))
                        ),
                        () -> new SequentialGroup(
                                IntakeMotorSubsystem.INSTANCE.intakeCommand(),
                                new Delay(TimeSpan.fromMs(1500))
                        )
                )
        ).invoke();
    }

    @Override
    public boolean isDone() {
        return true;  // Run once per trigger
    }
}

