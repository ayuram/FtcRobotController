package org.firstinspires.ftc.teamcode.Components;

import android.util.Log;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.control.PIDFController;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.arcrobotics.ftclib.util.InterpLUT;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

//http://192.168.43.1:8080/dash
@Config
public class Shooter {
    public ColorRangeSensor colorRangeSensor;
    public static PIDCoefficients MOTOR_VELO_PID = new PIDCoefficients(0.0036, 0, 0);
    public static PIDCoefficients ANGLE_PID = new PIDCoefficients(0.0002, 0, 0);
    public static double kV = 0.00052428571428572;//1 / TuningController.rpmToTicksPerSecond(TuningController.MOTOR_MAX_RPM);
    public static double kA = 0.0003;
    public static double kStatic = 0;
    public static double sleepTime = 120;

    double lastTargetVelo = 0.0;
    double lastKv = kV;
    double lastKa = kA;
    double lastKstatic = kStatic;
    VelocityPIDFController veloController = new VelocityPIDFController(MOTOR_VELO_PID, kV, kA, kStatic);
    PIDFController angleControl = new PIDFController(ANGLE_PID);

    private final ElapsedTime veloTimer = new ElapsedTime();
    public DcMotorEx flywheel, flywheel1, turret;
    public Servo gunner, flap, mag;
    public Servo rightIntakeHolder, leftIntakeHolder;
    public double targetVelo;
    private final InterpLUT veloRegression;

    public Shooter(HardwareMap map, Robot robot){
        veloRegression = new InterpLUT();
        setVelocityController();
        colorRangeSensor = map.get(ColorRangeSensor.class, "range");
        flywheel = map.get(DcMotorEx.class, "fw");
        flywheel1 = map.get(DcMotorEx.class, "fw1");
        flywheel.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        flywheel.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        flywheel1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        gunner = map.get(Servo.class, "mag");
        flap = map.get(Servo.class, "flap");
        mag = map.get(Servo.class, "tilt");
        leftIntakeHolder = map.get(Servo.class,"wallL");
        rightIntakeHolder = map.get(Servo.class,"wallR");
        turret = map.get(DcMotorEx.class, "turret");
        turret.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        singleRound();
        magDown();
        if(Robot.opModeType == OpModeType.auto){
            wingsIn();
        }
        else if(Robot.opModeType == OpModeType.tele) {
            wingsVert();
            flapDown();
        }
        for (LynxModule module : map.getAll(LynxModule.class)) {
            module.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }
        veloTimer.reset();
    }

    public void update(){
        veloController.setTargetVelocity(targetVelo);
        veloController.setTargetAcceleration((targetVelo - lastTargetVelo) / veloTimer.seconds());
        veloTimer.reset();
        lastTargetVelo = targetVelo;
        double motorPos = flywheel.getCurrentPosition();
        double motorVelo = flywheel.getVelocity();
        double power = veloController.update(motorPos, motorVelo);
        if (targetVelo == 0) {
            flywheel.setPower(0);
            flywheel1.setPower(0);
        } else {
            flywheel.setPower(power);
            flywheel1.setPower(power);
        }
        if(lastKv != kV || lastKa != kA || lastKstatic != kStatic) {
            lastKv = kV;
            lastKa = kA;
            lastKstatic = kStatic;
            veloController = new VelocityPIDFController(MOTOR_VELO_PID, kV, kA, kStatic);
        }
        if(getVelocity() > 100) Log.println(Log.INFO, "Velocity: ", getVelocity() + "");
    }
    public int getRings(){
        double range = colorRangeSensor.getDistance(DistanceUnit.INCH);
            if (range < 2.6) {
                return 3;
            } else if (range < 4.1) {
                return 2;
            } else if (range < 7) {
                return 1;
            } else {
                return 0;
            }
    }
    public double getDistance(){
        return colorRangeSensor.getDistance(DistanceUnit.INCH);
    }
    public void setVelocity(double v){
        targetVelo = v;
    }
    public void setVelocity(Vector2d vector2d){
        targetVelo = veloRegression.get(vector2d.distTo(Robot.goal));
    }
    public void setLauncherVelocity(double v){
        targetVelo = v;
        if(v > 0){
            magUp();
        }
        else {
            magDown();
        }
    }
    public double getVelocity(){
        return flywheel.getVelocity();
    }
    public double getTargetVelo(){
        return targetVelo;
    }
    public double getError(){
        return getTargetVelo() - getVelocity();
    }
    public void wingsOut() {
        leftIntakeHolder.setPosition(0.96);
        rightIntakeHolder.setPosition(0.18);
    }
    public void wingsIn() {
        leftIntakeHolder.setPosition(.3);
        rightIntakeHolder.setPosition(.84);
    }
    public void wingsMid() {
        leftIntakeHolder.setPosition(.85);
        rightIntakeHolder.setPosition(0.3);
    }
    public void wingsVert(){
        leftIntakeHolder.setPosition(0.6);
        rightIntakeHolder.setPosition(0.6);
    }
    public void leftOut() {
        leftIntakeHolder.setPosition(.96);
        rightIntakeHolder.setPosition(0.84);
    }
    public void safeLeftOut(){
        leftIntakeHolder.setPosition(.75);
        rightIntakeHolder.setPosition(.64);
    }
    public void rightOut() {
        leftIntakeHolder.setPosition(.3);
        rightIntakeHolder.setPosition(0.18);
    }
    public void unlockIntake(){
        rightIntakeHolder.setPosition(0.4);
    }
    public void magUp(){
        mag.setPosition(0.75);
    }
    public void magDown(){
        mag.setPosition(0.56);
    }
    public void magazineShoot(){
        customShoot(sleepTime, getRings());
    }
    public void customShoot(double sleep, int rounds){
        for(int i = 0; i < rounds; i++){
            singleRound();
            Log.println(Log.INFO, "Shot Number: ", i + "");
            if(i != rounds-1 || Robot.opModeType == OpModeType.auto)sleep((long)sleep);
            if(i == rounds - 2){
                sleep(90);
            }
        }
    }
    public void safeShoot(){
        customShoot(300, getRings());
    }
    public void tripleShot(){
        customShoot(sleepTime, 3);
    }
    public void singleRound(){
        gunner.setPosition(0.34);
        sleep(145);
        Log.println(Log.INFO,"Shot", "Single Round");
        gunner.setPosition(0.48);
    }
    public void flapUp(){
        flap.setPosition(0.43);
    }
    public void flapDown(){
        flap.setPosition(0.35);
    }
    public final void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    private void setVelocityController(){
        veloRegression.add(0,1500);
        veloRegression.add(75,1450);
        veloRegression.add(77.5,1440);
        veloRegression.add(78, 1420);
        veloRegression.add(79, 1410);
        veloRegression.add(80,1400);
        veloRegression.add(85,1380);
        veloRegression.add(90, 1350);
        veloRegression.add(95,1230);
        veloRegression.add(100,1220);
        veloRegression.add(102, 1210);
        veloRegression.add(105,1220);
        veloRegression.add(110,1220);
        //tbc
        veloRegression.add(115,1190);
        veloRegression.add(120,1190);
        veloRegression.add(125,1210);
        veloRegression.add(132.5,1220);
        veloRegression.add(200, 1400);
        veloRegression.add(1000,1400);
        veloRegression.createLUT();
    }
}