package org.firstinspires.ftc.teamcode.modules.capstone;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImpl;

import org.firstinspires.ftc.teamcode.util.opmode.ModuleTest;

@TeleOp
public class TestOpModeClaw extends ModuleTest {
    Servo claw;
    boolean open=true;

    @Override
    public void init() {
        claw = hardwareMap.servo.get("capstoneClaw");
    }

    @Override
    public void loop() {
        telemetry.addData("Claw: ",open);
        telemetry.update();
        if(gamepad2.b){
            claw.setPosition(0.5);
            open=true;
        }else if (gamepad1.a){
            claw.setPosition(0.15);
            open=false;
        }
    }
}
