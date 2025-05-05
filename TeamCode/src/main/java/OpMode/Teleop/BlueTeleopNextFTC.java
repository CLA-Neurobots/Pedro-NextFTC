package OpMode.Teleop;



import com.pedropathing.follower.Follower;
import com.pedropathing.localization.Pose;
import com.pedropathing.util.Constants;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.rowanmcalpin.nextftc.core.command.CommandManager;
import com.rowanmcalpin.nextftc.ftc.NextFTCOpMode;
import com.rowanmcalpin.nextftc.ftc.driving.MecanumDriverControlled;
import com.rowanmcalpin.nextftc.ftc.hardware.controllables.MotorEx;
import com.rowanmcalpin.nextftc.pedro.DriverControlled;

import NextFTC.Subsystem.*;
import pedroPathing.constants.FConstants;
import pedroPathing.constants.LConstants;

@TeleOp(name = "BlueTeleopNextFTC")
public class BlueTeleopNextFTC extends NextFTCOpMode {

    public BlueTeleopNextFTC() {
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

    public enum IntakeState {
        INTAKE_START,
        INTAKE_EXTEND,
        INTAKE_WAITFORBLOCK,
        INTAKE_RETRACT,
        OUTAKE_HUMAIN,
        OUTAKE_BLOCK
    }

    public MotorEx frontLeft, frontRight, backLeft, backRight;
    public MotorEx[] motors;
    public Command driverControlled;
    private IntakeState intakeState = IntakeState.INTAKE_START;
    private ElapsedTime intakeTimer = new ElapsedTime();
    private ElapsedTime loopTimer = new ElapsedTime();

    private Follower follower;
    private final Pose startPose = new Pose(0,0,Math.toRadians(-90));

    @Override
    public void onInit() {
        follower = new Follower(hardwareMap, FConstants.class, LConstants.class);
        follower.setStartingPose(startPose);
        follower.startTeleopDrive();

        motors = new MotorEx[] {frontLeft, frontRight, backLeft, backRight};
    }

    @Override
    public void onStartButtonPressed() {
        CommandManager.INSTANCE.scheduleCommand(new DriverControlled(gamepadManager.getGamepad1(), false));
        intakeTimer.reset();
    }

    @Override
    public void onUpdate() {
        // FSM Logic
        switch (intakeState) {
            case INTAKE_START:
                ExtendoSubsystem.INSTANCE.retractCommand(0.4).invoke();
                IntakeMotorSubsystem.INSTANCE.stopCommand().invoke();
                IntakeServoSubsystem.INSTANCE.close().invoke();

                break;

            case INTAKE_EXTEND:
                ExtendoSubsystem.INSTANCE.extendCommand(0.4).invoke();
                if (intakeTimer.seconds() > 1) {
                    IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
                    IntakeServoSubsystem.INSTANCE.open().invoke();
                    intakeTimer.reset();
                    intakeState = IntakeState.INTAKE_WAITFORBLOCK;
                }
                break;

            case INTAKE_WAITFORBLOCK:
                ColorSubsystem.INSTANCE.periodic();
                if (ColorSubsystem.INSTANCE.isBlue() || ColorSubsystem.INSTANCE.isYellow()) {
                    IntakeMotorSubsystem.INSTANCE.stopCommand().invoke();
                    IntakeServoSubsystem.INSTANCE.close().invoke();
                    intakeTimer.reset();
                    intakeState = IntakeState.INTAKE_RETRACT;
                }
                break;

            case INTAKE_RETRACT:
                ExtendoSubsystem.INSTANCE.retractCommand(0.4).invoke();
                if (gamepadManager.getGamepad1().getRightBumper().getState()) {
                    intakeTimer.reset();
                    intakeState = IntakeState.OUTAKE_HUMAIN;
                } else if (gamepadManager.getGamepad1().getLeftBumper().getState()) {
                    intakeTimer.reset();
                    intakeState = IntakeState.OUTAKE_BLOCK;
                }
                if (gamepadManager.getGamepad1().getDpadUp().getState()) {
                    intakeState = IntakeState.INTAKE_EXTEND;
                }
                break;

            case OUTAKE_HUMAIN:
                if (intakeTimer.seconds() < 0.75) {
                    IntakeMotorSubsystem.INSTANCE.outtakeCommand().invoke();
                } else {
                    intakeState = IntakeState.INTAKE_START;
                }
                break;

            case OUTAKE_BLOCK:
                if (intakeTimer.seconds() < 1.5) {
                    IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
                } else {
                    intakeState = IntakeState.INTAKE_START;
                }
                break;
        }

        if (gamepadManager.getGamepad1().getDpadDown().getState() && intakeState != IntakeState.INTAKE_START) {
            intakeState = IntakeState.INTAKE_START;
        }

        // Slide + bucket
        if (SlideSubsystem.INSTANCE.rightMotor.getCurrentPosition() > SlideSubsystem.HIGH * SlideSubsystem.INSTANCE.controller.getTarget()) {
            if (gamepadManager.getGamepad1().getRightTrigger().getValue() > 0.1) {
                BucketSubsystem.INSTANCE.dump().invoke();
            } else {
                BucketSubsystem.INSTANCE.transfer().invoke();
            }
        } else {
            BucketSubsystem.INSTANCE.transfer().invoke();
        }




        // Manual slide control
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

        //Manual Intake State Control
        if (gamepadManager.getGamepad1().getDpadUp().getState()) {
            intakeTimer.reset();
            intakeState = IntakeState.INTAKE_EXTEND;
        }
        if (gamepadManager.getGamepad1().getRightBumper().getState()) {
            IntakeMotorSubsystem.INSTANCE.outtakeCommand().invoke();
        }
        if (gamepadManager.getGamepad1().getLeftBumper().getState()) {
            IntakeMotorSubsystem.INSTANCE.intakeCommand().invoke();
        }

        // Telemetry
        telemetry.addData("Loop Time (ms)", loopTimer.milliseconds());
        telemetry.addData("Slide Position Left", SlideSubsystem.INSTANCE.leftMotor.getCurrentPosition());
        telemetry.addData("Slide Position Right", SlideSubsystem.INSTANCE.rightMotor.getCurrentPosition());
        telemetry.addData("Slide Target", SlideSubsystem.HIGH * SlideSubsystem.INSTANCE.controller.getTarget());
        telemetry.addData("Detected Color", ColorSubsystem.INSTANCE.getDetectedColor());
        telemetry.addData("Intake State", intakeState);
        telemetry.update();
    }
}
