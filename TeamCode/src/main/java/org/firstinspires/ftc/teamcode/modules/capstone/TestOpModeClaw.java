package org.firstinspires.ftc.teamcode.modules.capstone;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.util.opmode.ModuleTest;

@TeleOp
public class TestOpModeClaw extends ModuleTest {
    Capstone slides;

    @Override
    public void init() {
        slides = new Capstone(hardwareMap);
        init(slides);
        slides.arm.pickup();
    }

    @Override
    public void loop() {
        update();
        if(gamepad1.a){
            slides.cap();
        }
        telemetry.addData("Seconds", slides.elapsedTime.seconds());
    }
}
