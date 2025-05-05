package OpMode.Teleop;

import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.rowanmcalpin.nextftc.core.command.CommandManager;
import com.rowanmcalpin.nextftc.ftc.NextFTCOpMode;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.pedro.DriverControlled;

import NextFTC.Commands.IntakeSequenceCommand;
import NextFTC.Subsystem.*;

import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

@TeleOp(name = "BlueTeleopSimple")
public class BlueTeleopSimple extends NextFTCOpMode {

    public BlueTeleopSimple() {
        super(
                SlideSubsystem.INSTANCE,
                BucketSubsystem.INSTANCE,
                ClawSubsystem.INSTANCE,
                ColorSubsystem.INSTANCE,
                ExtendoSubsystem.INSTANCE,
                HandSubsystem.INSTANCE,
                IntakeMotorSubsystem.INSTANCE,
                IntakeServoSubsystem.INSTANCE
        );
    }

    public MotorEx[] motors;
    private Follower follower;
    private final Pose startPose = new Pose(0, 0, Math.toRadians(-90));
    private final ElapsedTime loopTimer = new ElapsedTime();

    @Override
    public void onInit() {
        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(startPose);
        follower.startTeleopDrive();
    }

    @Override
    public void onStartButtonPressed() {
        CommandManager.INSTANCE.scheduleCommand(new DriverControlled(gamepadManager.getGamepad1(), false));
        CommandManager.INSTANCE.scheduleCommand(new IntakeSequenceCommand(gamepadManager.getGamepad1()));
    }

    @Override
    public void onUpdate() {
        // Manual slide presets
        if (gamepadManager.getGamepad1().getY().getState()) {
            SlideSubsystem.INSTANCE.toHigh().invoke();
        }
        if (gamepadManager.getGamepad1().getA().getState()) {
            SlideSubsystem.INSTANCE.toGround().invoke();
        }
        if (gamepadManager.getGamepad1().getB().getState()) {
            SlideSubsystem.INSTANCE.toLow().invoke();
        }
        if (gamepadManager.getGamepad1().getX().getState()) {
            SlideSubsystem.INSTANCE.toMedium().invoke();
        }

        // Bucket logic based on slide height
        double slideRightPos = SlideSubsystem.INSTANCE.rightMotor.getCurrentPosition();
        double slideHighTargetTicks = SlideSubsystem.HIGH * SlideSubsystem.INSTANCE.controller.getTarget();

        if (slideRightPos > slideHighTargetTicks) {
            if (gamepadManager.getGamepad1().getRightTrigger().getValue() > 0.1) {
                BucketSubsystem.INSTANCE.dump().invoke();
            } else {
                BucketSubsystem.INSTANCE.transfer().invoke();
            }
        } else {
            BucketSubsystem.INSTANCE.transfer().invoke();
        }

        // Manual trigger to restart intake sequence
        if (gamepadManager.getGamepad1().getDpadUp().getState()) {
            CommandManager.INSTANCE.scheduleCommand(new IntakeSequenceCommand(gamepadManager.getGamepad1()));
        }

        // Manual intake overrides (still possible while FSM is idle)
        if (gamepadManager.getGamepad1().getRightBumper().getState()) {
            IntakeMotorSubsystem.INSTANCE.outtakeCommand().invoke();
        }
        if (gamepadManager.getGamepad1().getLeftBumper().getState()) {
            IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
        }

        // Telemetry
        telemetry.addData("Loop Time (ms)", loopTimer.milliseconds());
        loopTimer.reset();
        telemetry.addData("Slide Position Left", SlideSubsystem.INSTANCE.leftMotor.getCurrentPosition());
        telemetry.addData("Slide Position Right", SlideSubsystem.INSTANCE.rightMotor.getCurrentPosition());
        telemetry.addData("Slide Target", SlideSubsystem.HIGH * SlideSubsystem.INSTANCE.controller.getTarget());
        telemetry.addData("Detected Color", ColorSubsystem.INSTANCE.getDetectedColor());
        telemetry.update();
    }
}
