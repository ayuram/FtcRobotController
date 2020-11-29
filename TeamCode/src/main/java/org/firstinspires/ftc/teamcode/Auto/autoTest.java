package org.firstinspires.ftc.teamcode.Auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Components.Robot;
import org.firstinspires.ftc.teamcode.PurePursuit.Coordinate;

@Autonomous
public class autoTest extends LinearOpMode {
    Robot robot;
    @Override
    public void runOpMode() throws InterruptedException{
        robot = new Robot(DcMotor.RunMode.RUN_WITHOUT_ENCODER, hardwareMap, 14, 24, 0,18, 18);
        robot.autoInit();
        telemetry.addData("orient", robot.position.returnOrientation());
        waitForStart();
        //robot.scan();
        //robot.launcher.findAngle(robot.hiGoal, robot.position);
//        robot.launcher.setFlyWheel(1);
//        robot.launcher.magazineShoot();
//        if(robot.discs == 4){
//            case4();
//        }
//        else if(robot.discs == 1){
//            case1();
//        }
//        else{
//            case0();
//        }
        //case4();
        foo();
        telemetry.addData("Done", "");
        telemetry.update();
        robot.position.stop();
    }
    public void foo(){
        robot.goToPosition(new Coordinate(28, 24), 0.4, 0, 2);

    }
}
