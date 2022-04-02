package org.firstinspires.ftc.teamcode.modules.capstone;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.modules.Module;
import org.firstinspires.ftc.teamcode.modules.StateBuilder;

@Config
public class Capstone extends Module<Capstone.State> {
    public static double servoIncrementHorizontal = 0.0003, servoIncrementVertical = -0.00008;
    public static double horizontalTolerance = 0, verticalTolerance = 0;
    public static double servoIncrementHorizontalLarge = 0.01, servoIncrementVerticalLarge = 0.03;
    private double horizontalPos = 0.5, verticalPos = 0.45;
    public static double passivePower = 0.0;
    private CRServo tape;
    private Servo verticalTurret, horizontalTurret;
    public static double verticalPosDef = 0.38, horizontalPosDef = 0.0;
    private double lastTimeStamp = System.currentTimeMillis();
    private double verticalInc, horizontalInc;
    public static double vUpperLimit = 0.8, vLowerLimit = 0.1;
    public static double hUpperLimit = 1.0, hLowerLimit = 0.0;

    @Override
    public boolean isTransitioningState() {
        return false;
    }

    public enum State implements StateBuilder {
        IDLE,
        AUTORETRACT,
        ACTIVE,
        ;

        @Override
        public Double getTimeOut() {
            return null;
        }
    }

    /**
     * @param hardwareMap  instance of the hardware map provided by the OpMode
     */
    public Capstone(HardwareMap hardwareMap) {
        super(hardwareMap, State.IDLE);
    }

    public void internalInit() {
        tape = hardwareMap.crservo.get("tape");
        tape.setDirection(DcMotorSimple.Direction.REVERSE);
        horizontalTurret = hardwareMap.servo.get("hTurret");
        verticalTurret = hardwareMap.servo.get("vTurret");
        //setActuators(horizontalTurret, verticalTurret);
    }

    double power;

    @Override
    protected void internalUpdate() {
        double millisSinceLastUpdate = System.currentTimeMillis() - lastTimeStamp;
        verticalPos = Range.clip(verticalPos + (verticalInc * millisSinceLastUpdate), vLowerLimit, vUpperLimit);
        horizontalPos = Range.clip(horizontalPos + (horizontalInc * millisSinceLastUpdate), hLowerLimit, hUpperLimit);
        switch (getState()) {
            case IDLE:
                tape.setPower(passivePower);
                verticalTurret.setPosition(verticalPosDef);
                horizontalTurret.setPosition(horizontalPosDef);
                break;
            case ACTIVE:
                tape.setPower(power);
                verticalTurret.setPosition(verticalPos);
                horizontalTurret.setPosition(horizontalPos);
                break;
            case AUTORETRACT:
                tape.setPower(-1);
                verticalTurret.setPosition(verticalPosDef);
                if (getSecondsSpentInState() > 2.5) {
                    horizontalTurret.setPosition(horizontalPosDef);
                }
                break;
        }
        lastTimeStamp = System.currentTimeMillis();
        verticalInc = 0;
        horizontalInc = 0;
    }

    public void setHorizontalTurret(double pwr) {
        if (Math.abs(pwr) > horizontalTolerance) horizontalInc = servoIncrementHorizontal * pwr;

    }
    public void incrementHorizontal(double pwr) {
        if (Math.abs(pwr) > horizontalTolerance) horizontalPos += servoIncrementHorizontalLarge * pwr;
    }
    public void incrementVertical(double pwr) {
        if (Math.abs(pwr) > verticalTolerance) verticalPos += servoIncrementVerticalLarge * pwr;
    }
    public void setVerticalTurret(double pwr) {
        if (Math.abs(pwr) > verticalTolerance) verticalInc = servoIncrementVertical * pwr;
    }
    public void setTape(double pwr) {
        power = pwr;
    }
    public boolean isDoingInternalWork() {
        return false;
    }

    public void retract() {
        setState(State.AUTORETRACT);
    }

    public void idle() {
        setState(State.IDLE);
    }

    public void active() {
        setState(State.ACTIVE);
    }
}
