package org.firstinspires.ftc.teamcode.teleop;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.gamepad.ButtonReader;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.gamepad.KeyReader;
import com.arcrobotics.ftclib.gamepad.ToggleButtonReader;
import com.arcrobotics.ftclib.gamepad.TriggerReader;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.drive.Robot;
import org.firstinspires.ftc.teamcode.modules.capstone.Capstone;
import org.firstinspires.ftc.teamcode.modules.carousel.Carousel;
import org.firstinspires.ftc.teamcode.modules.deposit.Deposit;
import org.firstinspires.ftc.teamcode.modules.deposit.Platform;
import org.firstinspires.ftc.teamcode.modules.intake.Intake;
import org.firstinspires.ftc.teamcode.util.field.Balance;
import org.firstinspires.ftc.teamcode.util.field.OpModeType;

import static org.firstinspires.ftc.teamcode.util.field.Context.balance;

@TeleOp
public class DriverPractice extends LinearOpMode {
    enum Mode {
        DRIVING,
        ENDGAME
    }
    Robot robot;
    Intake intake;
    Deposit deposit;
    Carousel carousel;
    Capstone capstone;

    Mode mode = Mode.DRIVING;
    boolean toggleMode;

    GamepadEx primary;
    GamepadEx secondary;
    KeyReader[] keyReaders;
    TriggerReader intakeButton, ninjaMode, liftButton, softDump;
    ButtonReader levelIncrement, levelDecrement, dumpButton, tippedToward, tippedAway, capHorizontalInc,
            capVerticalInc, capHorizontalDec, capVerticalDec, capRetract, intakeCounterBalance;
    ToggleButtonReader depositLift;

    Deposit.State defaultDepositState = Deposit.State.LEVEL3;

    @Override
    public void runOpMode() throws InterruptedException {
        robot = new Robot(this, OpModeType.TELE);
        Deposit.allowLift = true;
        intake = robot.intake;
        deposit = robot.deposit;
        carousel = robot.carousel;
        capstone = robot.capstone;
        primary = new GamepadEx(gamepad1);
        secondary = new GamepadEx(gamepad2);
        keyReaders = new KeyReader[] {
                capHorizontalInc = new ButtonReader(primary, GamepadKeys.Button.DPAD_RIGHT),
                capHorizontalDec = new ButtonReader(primary, GamepadKeys.Button.DPAD_LEFT),
                capVerticalInc = new ButtonReader(primary, GamepadKeys.Button.DPAD_UP),
                capVerticalDec = new ButtonReader(primary, GamepadKeys.Button.DPAD_DOWN),
                intakeButton = new TriggerReader(secondary, GamepadKeys.Trigger.RIGHT_TRIGGER),
                ninjaMode = new TriggerReader(primary, GamepadKeys.Trigger.LEFT_TRIGGER),
                levelIncrement = new ButtonReader(secondary, GamepadKeys.Button.DPAD_UP),
                levelDecrement = new ButtonReader(secondary, GamepadKeys.Button.DPAD_DOWN),
                liftButton = new TriggerReader(secondary, GamepadKeys.Trigger.LEFT_TRIGGER),
                tippedAway = new ButtonReader(secondary, GamepadKeys.Button.LEFT_BUMPER),
                tippedToward = new ButtonReader(secondary, GamepadKeys.Button.RIGHT_BUMPER),
                depositLift = new ToggleButtonReader(primary, GamepadKeys.Button.LEFT_BUMPER),
                dumpButton = new ButtonReader(primary, GamepadKeys.Button.RIGHT_BUMPER),
                capRetract = new ButtonReader(primary, GamepadKeys.Button.X),
                intakeCounterBalance = new ButtonReader(secondary, GamepadKeys.Button.A),
                softDump = new TriggerReader(primary, GamepadKeys.Trigger.RIGHT_TRIGGER)
        };
        Deposit.allowLift = false;
        waitForStart();
        while (opModeIsActive()) {
            robot.update();
            for (KeyReader reader : keyReaders) {
                reader.readValue();
            }
            Pose2d drivePower = new Pose2d(
                    -gamepad1.left_stick_y,
                    0,
                    -gamepad1.right_stick_x
            );
            if (intake.getContainsBlock() && intake.getState() == Intake.State.OUT) {
                gamepad1.rumble(500);
                gamepad2.rumble(500);
            }
            if (depositLift.wasJustPressed()) {
                Deposit.allowLift = !Deposit.allowLift;
            }
            if (intakeCounterBalance.wasJustPressed()) {
                Platform.shouldCounterBalance = !Platform.shouldCounterBalance;
            }
            if (ninjaMode.isDown()) drivePower = drivePower.times(0.60);
            if (gamepad1.touchpad) {
                if (!toggleMode) {
                    if (mode == Mode.DRIVING) {
                        capstone.setTape(0);
                        capstone.active();
                        mode = Mode.ENDGAME;
                    } else {
                        capstone.retract();
                        mode = Mode.DRIVING;
                    }
                }
                toggleMode = true;
            } else {
                toggleMode = false;
            }
            if (capRetract.wasJustPressed()) {
                if (capstone.getState() == Capstone.State.ACTIVE) {
                    capstone.retract();
                } else {
                     capstone.idle();
                }
            }
            if (mode == Mode.ENDGAME) {
                drivePower = new Pose2d(
                        -gamepad2.left_stick_y,
                        0,
                        0
                ).div(8);
                setCapstone();
            } else {
                capstone.setTape(-Math.abs(gamepad2.left_stick_y));
            }
            robot.setWeightedDrivePower(drivePower);
            setIntake();
            setDeposit();
            setCarousel();
            if (tippedAway.isDown() && tippedToward.isDown()) {
                balance = Balance.BALANCED;
            } else if (tippedAway.isDown()) {
                balance = Balance.AWAY;
            } else if (tippedToward.isDown()) {
                balance = Balance.TOWARD;
            }
        }
    }

    void setCapstone() {
        capstone.setTape(gamepad1.right_trigger - gamepad1.left_trigger);
        capstone.setVerticalTurret(gamepad1.left_stick_y);
        capstone.setHorizontalTurret(gamepad1.left_stick_x);
        carousel.setPower(gamepad2.right_stick_y);
        if (capHorizontalInc.wasJustPressed()) {
            capstone.incrementHorizontal(1);
        } else if (capHorizontalDec.wasJustPressed()) {
            capstone.incrementHorizontal(-1);
        }
        if (capVerticalInc.wasJustPressed()) {
            capstone.incrementVertical(1);
        } else if (capVerticalDec.wasJustPressed()) {
            capstone.incrementVertical(-1);
        }
    }

    void setIntake() {
        intake.setPower(gamepad2.right_trigger + gamepad2.left_trigger);
    }

    void setDeposit() {
        if (levelIncrement.wasJustPressed()) {
            switch (defaultDepositState) {
                case LEVEL2:
                    defaultDepositState = Deposit.State.LEVEL3;
                    break;
                case LEVEL1:
                    defaultDepositState = Deposit.State.LEVEL2;
                    break;
            }
            deposit.setState(defaultDepositState);
        } else if (levelDecrement.wasJustPressed()) {
            switch (defaultDepositState) {
                case LEVEL3:
                    defaultDepositState = Deposit.State.LEVEL2;
                    break;
                case LEVEL2:
                    defaultDepositState = Deposit.State.LEVEL1;
                    break;
            }
            deposit.setState(defaultDepositState);
        }

        if (dumpButton.wasJustPressed()) {
            if (Platform.isLoaded) {
                deposit.dump();
            } else {
                Platform.isLoaded = true;
            }
        }
        if (gamepad1.right_trigger > 0.5) {
            deposit.softDump();
        }
    }

    void setCarousel() {
        carousel.setPower(gamepad2.right_stick_y);
    }
}
