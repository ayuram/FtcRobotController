package org.firstinspires.ftc.teamcode.Tele;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.gamepad.ButtonReader;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.gamepad.KeyReader;
import com.arcrobotics.ftclib.gamepad.ToggleButtonReader;
import com.arcrobotics.ftclib.gamepad.TriggerReader;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Components.Gunner;
import org.firstinspires.ftc.teamcode.Components.Intake;
import org.firstinspires.ftc.teamcode.Components.Magazine;
import org.firstinspires.ftc.teamcode.Components.OpModeType;
import org.firstinspires.ftc.teamcode.Components.Robot;
import org.firstinspires.ftc.teamcode.Components.Shooter;
import org.firstinspires.ftc.teamcode.Components.Turret;
import org.firstinspires.ftc.teamcode.Components.WobbleArm;
import org.firstinspires.ftc.teamcode.Components.localizer.T265;

import static org.firstinspires.ftc.teamcode.Components.Details.robotPose;

@TeleOp(name = "RedTele", group = "Tele")
@Config
public class RedTele extends OpMode {
    @Override
    public void stop() {
        T265.stopCam();
        super.stop();
    }

    @Override
    public void init() {
        robot = new Robot(this, OpModeType.TELE);
        wobbleArm = robot.wobbleArm;
        shooter = robot.shooter;
        magazine = shooter.magazine;
        intake = robot.intake;
        turret = shooter.turret;
        gunner = shooter.gunner;
        initializeButtons();
        shooter.setState(Shooter.State.CUSTOMVELO);
        telemetry.addData("Initialized", true);
        telemetry.update();
        robot.setPoseEstimate(new Pose2d());
    }

    @Override
    public void loop() {
        turret.setTarget(Robot.goal);
        shooter.setVelocity(velo);
        if (shieldButton.wasJustPressed()) {
            switch (intake.getState()) {
                case UP:
                    intake.setState(Intake.State.DOWN);
                    break;
                case DOWN:
                    intake.setState(Intake.State.UP);
                    break;
            }
        }
        if (magButton.wasJustPressed()) {
            if (gunner.getState() == Gunner.State.IDLE) {
                magazine.magMacro();
            } else {
                robot.actionQueue.add(() -> magazine.magMacro());
            }
            telemetry.addData("magazine macro", "Started");
            telemetry.update();
        }
        robot.intake.setPower(g2.gamepad.right_stick_y);
        if(reverseMode.wasJustPressed()) {
            switch (driveState) {
                case NORMAL:
                    driveState = DriveState.WOBBLE;
                    break;
                case WOBBLE:
                    driveState = DriveState.NORMAL;
                    break;
            }
        }
        switch (driveState) {
            case NORMAL:
                robot.setWeightedDrivePower(
                        new Pose2d(
                                g1.getLeftY(),
                                -g1.getLeftX(),
                                -g1.getRightX()
                        )
                );
                break;
            case WOBBLE:
                robot.setWeightedDrivePower(
                        new Pose2d(
                                -g1.getLeftY(),
                                g1.getLeftX(),
                                -g1.getRightX()
                        )
                );
                break;
        }
        wobble();
        safety();
        if (turretButton.getState()) {
            turret.setState(Turret.State.IDLE);
        }
        robot.update();
        for (KeyReader reader : readers) {
            reader.readValue();
        }
    }

    enum DriveState {
        NORMAL,
        WOBBLE
    }
    public static double velo = 4600;
    Robot robot;
    DriveState driveState = DriveState.NORMAL;
    WobbleArm wobbleArm;
    Shooter shooter;
    Turret turret;
    Gunner gunner;
    Magazine magazine;
    Intake intake;
    GamepadEx g1, g2;
    ToggleButtonReader turretButton;
    ButtonReader clawButton, wobbleButton, shieldButton, reverseMode, magButton;
    TriggerReader intakeButton;
    KeyReader[] readers;

    public void safety() {
        if (robotPose.getX() > 15) {
            turret.setState(Turret.State.IDLE);
        } else {
            switch (wobbleArm.getState()) {
                case DOWN:
                case UP:
                    turret.setState(Turret.State.TARGET_LOCK);
                    break;
                case MACRO:
                case MID:
                    turret.setState(Turret.State.IDLE);
                    break;
            } if (turret.getState() == Turret.State.TARGET_LOCK && turret.isIdle() && robotPose.getX() < 2 && Magazine.currentRings != 0 && magazine.getState() == Magazine.State.DOWN) {
                gunner.shoot();
            }
            turret.setState(Turret.State.TARGET_LOCK);
        }
    }

    public void wobble() {
        if (wobbleButton.wasJustPressed()) {
            switch (robot.wobbleArm.getState()) {
                case UP:
                    wobbleArm.dropMacro();
                    break;
                case MID:
                case DOWN:
                    wobbleArm.claw.grab();
                    wobbleArm.setState(WobbleArm.State.UP);
                    break;
            }
        }
        if (clawButton.wasJustPressed()) {
            switch (wobbleArm.claw.getState()) {
                case GRIP: {
                    if (wobbleArm.getState() != WobbleArm.State.UP) robot.wobbleArm.claw.release();
                    break;
                }
                case RELEASE: {
                    robot.wobbleArm.claw.grab();
                    break;
                }
            }
        }
        if (g2.gamepad.right_bumper) {
            robot.wobbleArm.pickUp();
        }
    }

    private void initializeButtons() {
        g1 = new GamepadEx(gamepad1);
        g2 = new GamepadEx(gamepad2);
        intakeButton = new TriggerReader(g2, GamepadKeys.Trigger.RIGHT_TRIGGER);
        reverseMode = new ToggleButtonReader(g1, GamepadKeys.Button.LEFT_BUMPER);
        turretButton = new ToggleButtonReader(g2, GamepadKeys.Button.LEFT_BUMPER);
        clawButton = new ToggleButtonReader(g2, GamepadKeys.Button.A);
        shieldButton = new ToggleButtonReader(g1, GamepadKeys.Button.DPAD_DOWN);
        wobbleButton = new ToggleButtonReader(g2, GamepadKeys.Button.B);
        magButton = new ToggleButtonReader(g2, GamepadKeys.Button.RIGHT_STICK_BUTTON);
        readers = new KeyReader[]{intakeButton, clawButton, shieldButton, wobbleButton, reverseMode, magButton};
    }
}